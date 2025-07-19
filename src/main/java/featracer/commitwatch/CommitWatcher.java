package featracer.commitwatch;

import com.intellij.openapi.components.Service;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class CommitWatcher {

    private final List<CommitListener>  listeners = new ArrayList<>();

    public void registerListener(CommitListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners(CommitInfo info) {
        for (CommitListener listener : listeners) {
            listener.onCommit(info);
        }
    }


}
