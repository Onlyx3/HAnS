package featracer.data;

import com.intellij.openapi.vcs.CheckinProjectPanel;

public class CommitInfo {

    private final CheckinProjectPanel panel;

    public CommitInfo(CheckinProjectPanel panel) {
        this.panel = panel;
    }

    public CheckinProjectPanel getPanel() {
        return panel;
    }
}
