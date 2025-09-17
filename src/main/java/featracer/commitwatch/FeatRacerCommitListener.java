package featracer.commitwatch;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import featracer.commitwatch.pubsub.CommitListener;
import featracer.data.CommitInfo;
import featracer.data.FeatRacerStateService;
import featracer.data.RecommendationData;
import featracer.logic.ClassifierManager;
import featracer.logic.ClassifierStrategy;
import featracer.util.Utility;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FeatRacerCommitListener implements CommitListener {
    @Override
    public void onCommit(CommitInfo info) {
        System.out.print("A new commit was recognized. Attempting to invoke FeatRacer");
        Project project = info.getPanel().getProject();

        if(!FeatRacerStateService.getInstance(project).isInitialized) return;

        ClassifierStrategy strategy = ClassifierManager.getInstance(project).getStrategy();

        String commitHash = Utility.getLatestCommitHash(info.getPanel());
        if (commitHash == null) return;

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Running FeatRacer..."){
            List<RecommendationData> recommendations;

            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setFraction(0.0);
                recommendations = strategy.invoke(commitHash);
                indicator.setFraction(1.0);
            }
            public void onSuccess() {
                Utility.checkAndInvokeRecommendationWizard(project, recommendations);
            }
        });
       // List<RecommendationData> recommendations = strategy.invoke(commitHash);
       // Utility.checkAndInvokeRecommendationWizard(project, recommendations);
    }
}
