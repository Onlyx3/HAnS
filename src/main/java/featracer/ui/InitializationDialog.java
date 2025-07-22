package featracer.ui;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class InitializationDialog extends DialogWrapper {

    // This class currently displays a test of Elements that can be used
    private final ComboBox<String> testComboBox = new ComboBox<>(new String[]{"Test1", "Test2", "Test3"});
    private final TextFieldWithBrowseButton testTextField = new TextFieldWithBrowseButton();
    private final JBCheckBox extraFeatureCheckBox = new JBCheckBox("Whats this?");


    public InitializationDialog(@Nullable Project project) {
        super(project);
        setTitle("FeatRacer Initialization");
        setOKButtonText("Initialize");

        testTextField.addBrowseFolderListener("Select File", null, project, FileChooserDescriptorFactory.createSingleFolderDescriptor());
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return FormBuilder.createFormBuilder()
                .addLabeledComponent("Testing1", testComboBox, 1, false)
                .addLabeledComponent("TestingFile", testTextField, 1, false)
                .addComponent(extraFeatureCheckBox, 1).getPanel();
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

}
