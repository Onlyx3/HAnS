package featracer.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import featracer.data.RecommendationData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RecommendationDialogCardWizard extends DialogWrapper {

    private final Project project;
    private final List<RecommendationData> recommendations;

    private int current = 0;
    private JPanel panel;
    private CardLayout cardLayout;
    private JButton previousButton;
    private JButton nextButton;

    public RecommendationDialogCardWizard(@Nullable Project project, @NotNull List<RecommendationData> recommendations) {
        super(project, true);
        this.project = project;
        this.recommendations = recommendations;
        setTitle("Recommendations");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        cardLayout = new CardLayout();
        panel = new JPanel(cardLayout);

        for(int i = 0 ; i < recommendations.size(); i++) {
            RecommendationData rec = recommendations.get(i);
            JComponent panelStep = new RecommendationDialog(project, rec.getElement(), rec.getFeatures()).createCenterPanel();
            if(panelStep == null) continue;
            panel.add(panelStep, String.valueOf(i));
        }

        cardLayout.show(panel, "0");
        return panel;
    }

    @NotNull
    @Override
    protected JComponent createSouthPanel() {
        previousButton = new JButton("Previous");
        nextButton = new JButton("Next");

        previousButton.addActionListener(e -> previous());
        nextButton.addActionListener(e -> next());

        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelButtons.add(previousButton);
        panelButtons.add(nextButton);
        
        updateButton();
        return panelButtons;
    }

    private void previous() {
        if(current > 0) {
            current--;
            cardLayout.show(panel, String.valueOf(current));
            updateButton();
        }
    }
    private void next() {
        if(current < recommendations.size() - 1) {
            current++;
            cardLayout.show(panel, String.valueOf(current));
            updateButton();
        } else {
            close(0);
        }

    }
    private void updateButton() {
        previousButton.setEnabled(current > 0);

        if(current == recommendations.size() - 1) {
            nextButton.setText("Done");
        } else nextButton.setText("Next");
    }
}
