package featracer.test;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import featracer.data.FeatRacerStateService;
import featracer.data.RecommendationData;
import featracer.logic.ClassifierManager;
import featracer.util.Utility;
import org.jetbrains.annotations.NotNull;
import se.gu.api.FeatRacerAPI;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeatRacerTest {

    public static void show(@NotNull Project project) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("featracer.test")
                .createNotification("Test(FeatRacer", NotificationType.INFORMATION);

        notification.addAction(new AnAction("Show test dialog") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                runTest(project);
            }
        });

        notification.notify(project);
    }


    public static void runTest(@NotNull Project project) {
        List<String> testfeatures = new ArrayList<>();
        testfeatures.add("TestFeature");
        testfeatures.add("IceCream");
        testfeatures.add("Max Mustermann");
        testfeatures.add("Payment");
        testfeatures.add("Food");
        testfeatures.add("Coordinates");
        testfeatures.add("Puppies");
        testfeatures.add("Last");
        testfeatures.add("Bittebittezeigdashiernichtan");

    //    RecommendationData testBlock = Utility.makeRecommendationData(project, "featracer.test.FeatRacerTest:23-28", testfeatures);
    //    RecommendationData testLine = Utility.makeRecommendationData(project, "featracer.test.FeatRacerTest:37", testfeatures);
    //    RecommendationData outOfBounds = Utility.makeRecommendationData(project, "featracer.test.FeatRacerTest:2222",  testfeatures);
    //    RecommendationData classpathinvalid = Utility.makeRecommendationData(project, "thisdoesntexist:2", testfeatures);
        RecommendationData testBlock = Utility.translateFeatracerLocation(project, "test/testing/FeatRacerTest.java::60-109", testfeatures);
        RecommendationData testLine = Utility.translateFeatracerLocation(project, "ihopethisisirrelevant/FeatRacerTest.java::64-64", testfeatures);


        List<RecommendationData> list = new ArrayList<>();
        list.add(testBlock);
        list.add(testLine);
    //    list.add(outOfBounds);
    //    list.add(classpathinvalid);
     //   list.add(wrongFormat);

        Utility.checkAndInvokeRecommendationWizard(project, list);
    }
/*
    public static void runTest(@NotNull Project project) {
        int startingCommit = 1;
        String projectPath = project.getBasePath();
        String analysisPath = Paths.get(PathManager.getSystemPath(), "featracer-analysis", project.getName()).toString();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Running FeatRacer..."){
            List<RecommendationData> recommendations = new ArrayList<>();

            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setFraction(0.0);

                String projectGitPath = Paths.get(projectPath, ".git").toString();
                File gitDir = new File(projectGitPath);
                if(!gitDir.exists() || !gitDir.isDirectory()){
                    throw new RuntimeException("Project Git directory does not exist or is not a directory");
                }

                FeatRacerAPI featRacerAPI = new FeatRacerAPI();
                Map<String, List<String>> result;
                try {
                    result = featRacerAPI.testRecommendationAll(projectGitPath, startingCommit, analysisPath, ".js,.c,.cpp,.h,.cc,.y,.py,.java");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                for (Map.Entry<String, List<String>> entry : result.entrySet()) {
                    RecommendationData rec = Utility.translateFeatracerLocation(project, entry.getKey(), entry.getValue());
                    if (rec != null) recommendations.add(rec);
                }

            }
            public void onSuccess() {
                Utility.checkAndInvokeRecommendationWizard(project, recommendations);
            }
        });
    }*/
}