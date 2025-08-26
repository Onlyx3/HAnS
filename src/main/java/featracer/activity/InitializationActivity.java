package featracer.activity;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import featracer.commitwatch.FeatRacerCommitListener;
import featracer.commitwatch.pubsub.CommitWatcher;
import featracer.data.FeatRacerStateService;
import featracer.logic.ClassifierManager;
import featracer.logic.FeatRacerStrategy;
import featracer.notifications.InitNotification;
import featracer.test.FeatRacerTest;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InitializationActivity implements ProjectActivity {
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {

        //TODO: Set Strategy to FeatRacer
        ClassifierManager.getInstance(project).setStrategy(new FeatRacerStrategy(project));

        FeatRacerStateService state = FeatRacerStateService.getInstance(project);

        if(!state.isInitialized) {
            InitNotification.show(project);
        }

        //Register Listener
        project.getService(CommitWatcher.class).registerListener(new FeatRacerCommitListener());

        // Test
        FeatRacerTest.show(project);

        return null;
    }
}
