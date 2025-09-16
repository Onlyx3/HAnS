package featracer.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RecommendationDialogPanel extends JPanel implements Disposable {

    private final Project project;
    private final PsiElement codeFragment;
    private final List<String> recommendations;
    private final List<JCheckBox> checkBoxes;

    private JTextField textField;
    private Editor codeEditor;

    private RangeMarker startAnnotation;
    private RangeMarker endAnnotation;

    private boolean internalUpdate = false;

    private PsiElement codeFragmentEnd;
    private boolean isCodeBlock;
    private int blockStartOffset;
    private int blockEndOffset;

    private enum AnnotationSource {
        START, END
    }

    public RecommendationDialogPanel(@Nullable Project project, @NotNull PsiElement psiElement, @NotNull List<String> recommendations, @Nullable PsiElement psiElementEnd, @NotNull boolean isCodeBlock) {
        super(new BorderLayout());
        this.project = project;
        this.codeFragment = psiElement;
        this.isCodeBlock = isCodeBlock;
        this.codeFragmentEnd = psiElementEnd;
        this.recommendations = recommendations.size() > 8 ? recommendations.subList(0, 8) : recommendations;
        checkBoxes = new ArrayList<>();

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        //Checkboxes
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        for(String s : this.recommendations){
            JCheckBox checkBox = new JCheckBox(s);
            checkBox.addActionListener(e -> updateAnnotation());
            checkBoxes.add(checkBox);
            checkBoxPanel.add(checkBox);
        }
        controlPanel.add(checkBoxPanel);

        // Textfield for own Features
        textField = new JTextField(15);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent documentEvent) {updateAnnotation();}
            @Override public void removeUpdate(DocumentEvent documentEvent) {updateAnnotation();}
            @Override public void changedUpdate(DocumentEvent documentEvent) {updateAnnotation();}
        });

        JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textFieldPanel.add(new JLabel("Additional:"));
        textFieldPanel.add(textField);
        controlPanel.add(textFieldPanel);

        add(controlPanel, BorderLayout.EAST);


        //Editor
        PsiFile psiFile = codeFragment.getContainingFile();
        Document document = psiFile.getViewProvider().getDocument();
      //  Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            add(new JLabel("Document not found"), BorderLayout.CENTER);
            return;
        }

        if(isCodeBlock){
            int startLine = document.getLineNumber(codeFragment.getTextRange().getStartOffset());
            int endLine = document.getLineNumber(codeFragmentEnd.getTextRange().getStartOffset());
            this.blockStartOffset = document.getLineStartOffset(startLine);
            this.blockEndOffset = document.getLineEndOffset(endLine);
        }

       // codeEditor = EditorFactory.getInstance().createEditor(document, project);
        codeEditor = EditorFactory.getInstance().createViewer(document, codeFragment.getProject());

        //Scroll to element in editor
        int offset = codeFragment.getTextOffset();
        codeEditor.getCaretModel().moveToOffset(offset);

        //Editor settings
        codeEditor.getSettings().setLineNumbersShown(true);
        codeEditor.getSettings().setIndentGuidesShown(true);

        add(codeEditor.getComponent(), BorderLayout.CENTER);
        setPreferredSize(new Dimension(1200,900));
    }


    @Override
    public void dispose() {
        if (codeEditor != null && !codeEditor.isDisposed()) EditorFactory.getInstance().releaseEditor(codeEditor);
        if(startAnnotation != null) startAnnotation.dispose();
        if(endAnnotation != null)  endAnnotation.dispose();
    }



    private void updateAnnotation() {
        List<String> selectedFeatures = checkBoxes.stream()
                .filter(JCheckBox::isSelected)
                .map(AbstractButton::getText)
                .toList();

        String userFeatures = textField.getText().trim();

        String checkBoxSelectedString = String.join(", ", selectedFeatures);
        String finalContent = checkBoxSelectedString.isEmpty() ?
                userFeatures : (userFeatures.isEmpty() ? checkBoxSelectedString : checkBoxSelectedString + ", " + userFeatures);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            boolean startExists = startAnnotation != null && startAnnotation.isValid();

            if(userFeatures.isEmpty() && checkBoxSelectedString.isEmpty()){
                if(startExists) removeAnnotations();
            } else {
                if(startExists) updateExistingAnnotations(finalContent);
                else initializeAnnotations(finalContent);
            }

        });
    }

    private void updateExistingAnnotations(String content) {
        if(isCodeBlock) updateExistingBlock(content);
        else updateExistingLine(content);
    }

    private void updateExistingBlock(String content) {
        Document document = codeEditor.getDocument();
        String begin = "\t//&begin[" + content + "]\n";
        String end = "\n\t//&end[" + content + "]";

        if(endAnnotation != null && endAnnotation.isValid()) {
            document.replaceString(endAnnotation.getStartOffset(), endAnnotation.getEndOffset(), end);
        }
        if(startAnnotation != null && startAnnotation.isValid()) {
            document.replaceString(startAnnotation.getStartOffset(), startAnnotation.getEndOffset(), begin);
        }
    }

    private void updateExistingLine(String content) {
        Document document = codeEditor.getDocument();
        String line = " //&line[" + content + "]";
        if(startAnnotation != null && startAnnotation.isValid()) {
            document.replaceString(startAnnotation.getStartOffset(), startAnnotation.getEndOffset(), line);
        }
    }

    private void initializeAnnotations(String featureString){
        if(isCodeBlock) initializeBlock(featureString);
        else initializeLine(featureString);
    }

    private void initializeLine(String featureString){
        Document document = codeEditor.getDocument();
        int startOffset = codeFragment.getTextRange().getStartOffset();

        String text = String.format(" //&line[%s]", featureString);
        document.insertString(startOffset, text);
        startAnnotation = document.createRangeMarker(startOffset, startOffset + text.length());
        startAnnotation.setGreedyToLeft(true);
    }

    private void initializeBlock(String featureString){
        Document document = codeEditor.getDocument();


        String start = String.format("\t//&begin[%s]\n", featureString);
        String end = String.format("\n\t//&end[%s]", featureString);

        document.insertString(blockEndOffset, end);
        endAnnotation = document.createRangeMarker(blockEndOffset, blockEndOffset + end.length());

        document.insertString(blockStartOffset, start);
        startAnnotation = document.createRangeMarker(blockStartOffset, blockStartOffset + start.length());

        startAnnotation.setGreedyToLeft(true);
        endAnnotation.setGreedyToRight(true);
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

    public PsiElement getElement() {
        return codeFragment;
    }
}
