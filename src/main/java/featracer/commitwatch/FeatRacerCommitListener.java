package featracer.commitwatch;

import com.intellij.openapi.project.Project;
import featracer.commitwatch.pubsub.CommitListener;
import featracer.data.CommitInfo;
import featracer.data.FeatRacerStateService;
import featracer.data.RecommendationData;
import featracer.logic.ClassifierManager;
import featracer.logic.ClassifierStrategy;
import featracer.util.Utility;

import java.util.List;

public class FeatRacerCommitListener implements CommitListener {
    @Override
    public void onCommit(CommitInfo info) {
        Project project = info.getPanel().getProject();

        if(!FeatRacerStateService.getInstance(project).isInitialized) return;

        ClassifierStrategy strategy = ClassifierManager.getInstance(project).getStrategy();

        String commitHash = Utility.getLatestCommitHash(info.getPanel());
        if (commitHash == null) return;

        List<RecommendationData> recommendations = strategy.invoke(commitHash);
        Utility.checkAndInvokeRecommendationWizard(project, recommendations);
    }
}
