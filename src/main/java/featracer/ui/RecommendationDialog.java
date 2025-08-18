package featracer.ui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecommendationDialog extends DialogWrapper {

    private final Project project;
    private final PsiElement codeFragment;
    private final List<String> recommendations;
    private final List<JCheckBox> checkBoxes;

    private Editor codeEditor;

    private RangeMarker startAnnotation;
    private RangeMarker endAnnotation;

    public RecommendationDialog(@Nullable Project project, @NotNull PsiElement psiElement, @NotNull List<String> recommendations) {
        super(project);
        this.project = project;
        this.codeFragment = psiElement;
        this.recommendations = recommendations.size() > 8 ? recommendations.subList(0, 8) : recommendations;
        checkBoxes = new ArrayList<>();

        setTitle("Recommendation Dialog");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        //checkboxes
        JPanel checkBoxPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        for(String s : recommendations){
            JCheckBox checkBox = new JCheckBox(s);
            checkBox.addActionListener(e -> checkBoxStateChange());
            checkBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }
        panel.add(checkBoxPanel, BorderLayout.NORTH);

        //Editor
        PsiFile psiFile = codeFragment.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) return new JLabel("Document not found");

        codeEditor = EditorFactory.getInstance().createEditor(document, project);

        //Scroll to element in editor
        int offset = codeFragment.getTextOffset();
        codeEditor.getCaretModel().moveToOffset(offset);

        //Editor settings
        codeEditor.getSettings().setLineNumbersShown(true);
        codeEditor.getSettings().setIndentGuidesShown(true);

        panel.add(codeEditor.getComponent(), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(800, 600));

        return panel;
    }

    @Override
    protected void dispose() {
        if (codeEditor != null && !codeEditor.isDisposed()) EditorFactory.getInstance().releaseEditor(codeEditor);
        if(startAnnotation != null) startAnnotation.dispose();
        if(endAnnotation != null)  endAnnotation.dispose();

        super.dispose();
    }

    private void checkBoxStateChange() {
        List<String> selections = checkBoxes.stream().filter(JCheckBox::isSelected).map(AbstractButton::getText).collect(Collectors.toList());
        WriteCommandAction.runWriteCommandAction(project, () -> {
           if(selections.isEmpty()) removeAnnotations();
           else updateAnnotations(selections);
        });

    }

    private void updateAnnotations(List<String> features) {
        String featureString = String.join(", ", features);
        boolean isMultiline = codeFragment instanceof PsiCodeBlock;

        if (startAnnotation == null || !startAnnotation.isValid()) {
            initializeAnnotations(featureString, isMultiline);
            return;
        }
        Document document = codeEditor.getDocument();
        String updatedAnnotation = isMultiline ? String.format("//&begin[%s]", featureString) : String.format("//&line[%s]", featureString);
        document.replaceString(startAnnotation.getStartOffset(), startAnnotation.getEndOffset(), updatedAnnotation);

        if(isMultiline) {
            if(endAnnotation == null || !endAnnotation.isValid()) throw new IllegalStateException();
            String updatedAnnotationEnd = String.format("//&end[%s]", featureString);
            document.replaceString(endAnnotation.getStartOffset(), endAnnotation.getEndOffset(), updatedAnnotationEnd);
        }
    }

    private void initializeAnnotations(String featureString, boolean isMultiline){
        Document document = codeEditor.getDocument();
        int startOffset = codeFragment.getTextRange().getStartOffset();
        int endOffset = codeFragment.getTextRange().getEndOffset();

        if(isMultiline) {
            String start = String.format("//&begin[%s]\n", featureString);
            String end = String.format("//&end[%s]", featureString);
            document.insertString(endOffset, end);
            endAnnotation = document.createRangeMarker(endOffset, endOffset + end.length());
            document.insertString(startOffset, start);
            startAnnotation = document.createRangeMarker(startOffset, startOffset + start.length());
        } else {
            String text  = String.format("//&line[%s]", featureString);
            document.insertString(startOffset, text);
            startAnnotation = document.createRangeMarker(startOffset, startOffset + text.length());
        }
        startAnnotation.setGreedyToLeft(true);
        if(endAnnotation != null) endAnnotation.setGreedyToRight(true);
    }

    private void removeAnnotations() {
        Document document = codeEditor.getDocument();

        if(endAnnotation != null && endAnnotation.isValid()) {
            document.deleteString(endAnnotation.getStartOffset(), endAnnotation.getEndOffset());
            endAnnotation.dispose();
            endAnnotation = null;
        }
        if(startAnnotation != null && startAnnotation.isValid()) {
            document.deleteString(startAnnotation.getStartOffset(), startAnnotation.getEndOffset());
            startAnnotation.dispose();
            startAnnotation = null;
        }
    }
}
