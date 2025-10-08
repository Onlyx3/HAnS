package featracer.ui;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationDialogPanel extends JPanel implements Disposable {
    private final Project project;
    private final List<String> recommendations;

    private final List<JCheckBox> checkBoxes;
    private final JTextField textField;
    private Editor codeEditor;
    private RangeHighlighter rangeHighlighter;

    private final boolean isCodeBlock;
    private final SmartPsiElementPointer<PsiElement> startFragmentPointer;
    private final SmartPsiElementPointer<PsiElement> endFragmentPointer;
    private RangeMarker annotationStartMarker;
    private RangeMarker annotationEndMarker;

    public RecommendationDialogPanel(@Nullable Project project, @NotNull PsiElement psiElement, @Nullable List<String> recommendations, @Nullable PsiElement psiElementEnd, @NotNull boolean isCodeBlock) {
        super(new BorderLayout());
        this.project = project;
        this.isCodeBlock = isCodeBlock;

        // Limit to 8 recommendations
        if(recommendations != null) this.recommendations = recommendations.size() > 8 ? recommendations.subList(0, 8) : recommendations;
        else this.recommendations = new ArrayList<>();

        //pointers
        SmartPointerManager smartPointerManager = SmartPointerManager.getInstance(project);
        this.startFragmentPointer = smartPointerManager.createSmartPsiElementPointer(psiElement);
        this.endFragmentPointer = psiElementEnd != null ? smartPointerManager.createSmartPsiElementPointer(psiElementEnd) : null;

        // --------UI---------
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        checkBoxes = new ArrayList<>();
        if(recommendations != null && !recommendations.isEmpty()) {
            JPanel checkBoxPanel = new JPanel();
            checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
            for(String s : this.recommendations) {
                JCheckBox checkBox = new JCheckBox(s);
                checkBox.addActionListener(e -> updateAnnotation());
                checkBoxes.add(checkBox);
                checkBoxPanel.add(checkBox);
            }
            controlPanel.add(checkBoxPanel);
        }

        // custom feature textfield
        textField = new JTextField(15);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent documentEvent) {updateAnnotation();}
            @Override public void removeUpdate(DocumentEvent documentEvent) {updateAnnotation();}
            @Override public void changedUpdate(DocumentEvent documentEvent) {updateAnnotation();}
        });

        JPanel textFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textFieldPanel.add(new JLabel("Extra Features:"));
        textFieldPanel.add(textField);
        controlPanel.add(textFieldPanel);
        add(controlPanel, BorderLayout.EAST);

        // Editor
        PsiElement codeFragment = getElement(startFragmentPointer);
        if(codeFragment == null) return;

        Document document = codeFragment.getContainingFile().getViewProvider().getDocument();
        if(document == null) {
            add(new JLabel("Document not found"),  BorderLayout.CENTER);
        }

        codeEditor = EditorFactory.getInstance().createViewer(document, project);
        codeEditor.getSettings().setLineNumbersShown(true);
        codeEditor.getSettings().setLineNumbersShown(true);
        add(codeEditor.getComponent(), BorderLayout.CENTER);
        setPreferredSize(new Dimension(1200, 900));

        moveCodeEditor();

    }

    @Override
    public void dispose() {
        if (codeEditor != null && !codeEditor.isDisposed()) EditorFactory.getInstance().releaseEditor(codeEditor);
        if(annotationStartMarker != null) annotationStartMarker.dispose();
        if(annotationEndMarker != null)  annotationEndMarker.dispose();
    }

    public void moveCodeEditor() {
        if(codeEditor == null || codeEditor.isDisposed()) return;

        MarkupModel markupModel = codeEditor.getMarkupModel();
        if(rangeHighlighter != null) markupModel.removeHighlighter(rangeHighlighter);

        PsiElement startElement = getElement(startFragmentPointer);
        if(startElement == null) return;

        int startOffset = startElement.getTextRange().getStartOffset();
        int endOffset;

        if(isCodeBlock) {
            PsiElement endElement = getElement(endFragmentPointer);
            if(endElement == null) return;
            endOffset = endElement.getTextRange().getEndOffset();
        } else {
            endOffset = startElement.getTextRange().getEndOffset();
        }

        if(endOffset < startOffset) return;

        TextAttributes textAttributes = new TextAttributes();
        textAttributes.setBackgroundColor(new JBColor(new Color(0, 255, 255, 30), new Color(0, 255 ,255, 30)));
        rangeHighlighter = markupModel.addRangeHighlighter(
                startOffset,
                endOffset,
                HighlighterLayer.SELECTION - 1,
                textAttributes,
                HighlighterTargetArea.LINES_IN_RANGE
        );

        codeEditor.getCaretModel().moveToOffset(startOffset);
        codeEditor.getScrollingModel().scrollToCaret(ScrollType.CENTER_UP);
    }

    private void initializeAnnotations(String featureString) {
        if(isCodeBlock) initializeBlock(featureString);
        else initializeLine(featureString);
    }

    private void initializeLine(String featureString) {
        PsiElement codeFragment = getElement(startFragmentPointer);
        if(codeFragment == null) return;

        Document document = codeEditor.getDocument();
        int lineNum = document.getLineNumber(codeFragment.getTextRange().getStartOffset());
        int insertOffset = document.getLineEndOffset(lineNum);

        String text = String.format(" //&line[%s]", featureString);
        document.insertString(insertOffset, text);
        annotationStartMarker = document.createRangeMarker(insertOffset, insertOffset + text.length());
        annotationStartMarker.setGreedyToLeft(true);
    }

    private void initializeBlock(String featureString) {
        PsiElement startElement = getElement(startFragmentPointer);
        PsiElement endElement = getElement(endFragmentPointer);
        if(startElement == null || endElement == null) return;

        Document document = codeEditor.getDocument();

        int startLine = document.getLineNumber(startElement.getTextOffset());
        int endLine = document.getLineNumber(endElement.getTextOffset());
        int startInsertOffset = document.getLineStartOffset(startLine);
        int endInsertOffset = document.getLineEndOffset(endLine);

        String startText = String.format("\t//&begin[%s]\n", featureString);
        String endText = String.format("\n\t//&end[%s]", featureString);

        document.insertString(endInsertOffset, endText);
        annotationEndMarker = document.createRangeMarker(endInsertOffset, endInsertOffset + endText.length());

        document.insertString(startInsertOffset, startText);
        annotationStartMarker = document.createRangeMarker(startInsertOffset, startInsertOffset+ startText.length());

        annotationStartMarker.setGreedyToLeft(true);
        annotationEndMarker.setGreedyToLeft(true);
    }

    private void removeAnnotations() {
        Document document = codeEditor.getDocument();

        if(annotationEndMarker != null && annotationEndMarker.isValid()) {
            document.deleteString(annotationEndMarker.getStartOffset(), annotationEndMarker.getEndOffset());
            annotationEndMarker.dispose();
            annotationEndMarker = null;
        }
        if(annotationStartMarker != null && annotationStartMarker.isValid()) {
            document.deleteString(annotationStartMarker.getStartOffset(), annotationStartMarker.getEndOffset());
            annotationStartMarker.dispose();
            annotationStartMarker = null;
        }
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
            boolean startExists = annotationStartMarker != null && annotationStartMarker.isValid();

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

        if(annotationEndMarker != null && annotationEndMarker.isValid()) {
            document.replaceString(annotationEndMarker.getStartOffset(), annotationEndMarker.getEndOffset(), end);
        }
        if(annotationStartMarker != null && annotationStartMarker.isValid()) {
            document.replaceString(annotationStartMarker.getStartOffset(), annotationStartMarker.getEndOffset(), begin);
        }
    }

    private void updateExistingLine(String content) {
        Document document = codeEditor.getDocument();
        String line = " //&line[" + content + "]";
        if(annotationStartMarker != null && annotationStartMarker.isValid()) {
            document.replaceString(annotationStartMarker.getStartOffset(), annotationStartMarker.getEndOffset(), line);
        }
    }

    // this gets the first element
    public PsiElement getElement() {
        if(startFragmentPointer == null) return null;
        PsiElement element = startFragmentPointer.getElement();
        if(element == null || !element.isValid()) return null;
        return element;
    }

    public PsiElement getElement(SmartPsiElementPointer<? extends PsiElement> psiElementPointer) {
        if(psiElementPointer == null) return null;
        PsiElement psiElement = psiElementPointer.getElement();
        if(psiElement == null || !psiElement.isValid()) return null;
        return psiElement;
    }
}
