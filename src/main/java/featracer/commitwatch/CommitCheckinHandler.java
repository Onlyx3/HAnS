package featracer.commitwatch;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.checkin.CheckinHandler;


public class CommitCheckinHandler extends CheckinHandler {

    private final CheckinProjectPanel panel;

    public CommitCheckinHandler(CheckinProjectPanel panel) {
        this.panel = panel;
    }

    @Override
    public void checkinSuccessful() {
        CommitInfo info = new CommitInfo(panel);
        CommitWatcher watcher = panel.getProject().getService(CommitWatcher.class);

        watcher.notifyListeners(info);
    }
}
