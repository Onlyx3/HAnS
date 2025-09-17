package featracer.ui;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import featracer.data.FeatRacerStateService;
import featracer.data.RecommendationData;
import featracer.logic.ClassifierManager;
import featracer.util.Utility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class InitializationDialog extends DialogWrapper {

    private final Project project;
    // This class currently displays a test of Elements that can be used
    //private final ComboBox<String> testComboBox = new ComboBox<>(new String[]{"Test1", "Test2", "Test3"});
    //private final TextFieldWithBrowseButton testTextField = new TextFieldWithBrowseButton();
   // private final JBCheckBox extraFeatureCheckBox = new JBCheckBox("Whats this?");

    private final JBIntSpinner intSpinner = new JBIntSpinner(1, 1, Integer.MAX_VALUE);
    private final TextFieldWithBrowseButton analysisDir = new TextFieldWithBrowseButton();
    private final JBTextField allowedFileExtensions = new JBTextField();


    public InitializationDialog(@Nullable Project project) {
        super(project);

        this.project = project;
        setTitle("FeatRacer Initialization");
        setOKButtonText("Initialize");

        allowedFileExtensions.setText(".js,.c,.cpp,.h,.cc,.y,.py,.java");

        analysisDir.addBrowseFolderListener("Analysis Folder", null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor());
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Allowed file extensions:", allowedFileExtensions)
                .addLabeledComponent("Starting commit number", intSpinner, 1, false)
                .addLabeledComponent("Analysis directory", analysisDir, 1, false)
           //     .addLabeledComponent("Testing1", testComboBox, 1, false)
             //   .addLabeledComponent("TestingFile", testTextField, 1, false)
            //    .addComponent(extraFeatureCheckBox, 1)
                .getPanel();
    }

    @Override
    protected void doOKAction() {
        int startingCommit = this.intSpinner.getNumber();
        super.doOKAction(); //Closes the dialog

        String projectPath = project.getBasePath();
        String analysisPath = Paths.get(PathManager.getSystemPath(), "featracer-analysis", project.getName()).toString();

        System.out.println("AnalysisPath: "  + analysisPath);


        FeatRacerStateService state = FeatRacerStateService.getInstance(project);
        state.allowedFileExtensions = allowedFileExtensions.getText();
        state.analysisDirPath = analysisPath;//analysisDir.getText();


        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Running FeatRacer..."){
            List<RecommendationData> recommendations;

            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setFraction(0.0);
                recommendations = ClassifierManager
                        .getInstance(project)
                        .getStrategy()
                        .initializeProject(projectPath,
                                startingCommit,
                                analysisPath, //analysisDir.getText()
                                allowedFileExtensions.getText());
                indicator.setFraction(1.0);
            }
            public void onSuccess() {
                state.isInitialized = true;
                Utility.checkAndInvokeRecommendationWizard(project, recommendations);
            }
        });

    }

}
