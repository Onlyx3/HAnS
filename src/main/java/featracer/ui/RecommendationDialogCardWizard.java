package featracer.ui;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import featracer.data.RecommendationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationDialogCardWizard extends DialogWrapper {

    private final Project project;
    private final List<RecommendationData> recommendations;
    private final List<RecommendationDialogPanel>  panels = new ArrayList<>();

    private int current = 0;
    private int panelCount = 0;
    private JPanel panel;
    private CardLayout cardLayout;
    private JButton previousButton;
    private JButton nextButton;

    public RecommendationDialogCardWizard(@Nullable Project project, @NotNull List<RecommendationData> recommendations) {
        super(project, true);
        this.project = project;
        this.recommendations = recommendations;
        setTitle("Recommendations");
        setModal(false);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        cardLayout = new CardLayout();
        panel = new JPanel(cardLayout);

        for(int i = 0 ; i < recommendations.size(); i++) {
            RecommendationData rec = recommendations.get(i);
            if(rec.getElement() == null || rec.getFeatures().isEmpty()) continue;
            //JComponent panelStep = new RecommendationDialogPanel(project, rec.getElement(), rec.getFeatures()).createCenterPanel();
            RecommendationDialogPanel panelStep = new RecommendationDialogPanel(project, rec.getElement(), rec.getFeatures(), rec.getElementEnd(), rec.isCodeBlock());
            panels.add(panelStep);
            panel.add(panelStep, String.valueOf(i));
            panelCount++;
        }
        if(!panels.isEmpty()) cardLayout.show(panel, "0");
        return panel;
    }

    @Override
    protected void dispose() {
        for(RecommendationDialogPanel panel : panels) {
            Disposer.dispose(panel);
        }
        super.dispose();
    }

    @NotNull
    @Override
    protected JComponent createSouthPanel() {
        previousButton = new JButton("Previous");
        nextButton = new JButton("Next");

        previousButton.addActionListener(e -> previous());
        nextButton.addActionListener(e -> next());

        JButton viewButton = new JButton("View in Editor");
        viewButton.addActionListener(e -> viewInEditorAction());

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelButtons.add(viewButton);
        panelButtons.add(previousButton);
        panelButtons.add(nextButton);
        
        updateButton();
        return panelButtons;
    }

    private void viewInEditorAction() {
        RecommendationDialogPanel panelStep = panels.get(current);
        PsiElement element = panelStep.getElement();
        if(element == null || !element.isValid()) return;

        PsiFile  file = element.getContainingFile();
        if(file == null) return;
        VirtualFile virtualFile = file.getVirtualFile();
        if(virtualFile == null) return;

        int offset = element.getTextRange().getStartOffset();
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile, offset);

        descriptor.navigate(true);
    }

    private void previous() {
        if(current > 0) {
            current--;
            cardLayout.show(panel, String.valueOf(current));
            updateButton();
        }
    }
    private void next() {
        if(current < panelCount - 1) {
            current++;
            cardLayout.show(panel, String.valueOf(current));
            updateButton();
        } else {
            close(0);
        }

    }
    private void updateButton() {
        previousButton.setEnabled(current > 0);

        if(current == panelCount - 1) {
            nextButton.setText("Done");
        } else nextButton.setText("Next");
    }
}
