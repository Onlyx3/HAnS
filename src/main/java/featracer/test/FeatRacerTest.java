package featracer.test;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import featracer.data.RecommendationData;
import featracer.util.Utility;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FeatRacerTest {

    public static void show(@NotNull Project project) {
        System.out.println(project.getName());
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
        if(project.getName().equalsIgnoreCase("Snake1")) {
            warmup(project);
        } else if(project.getName().equalsIgnoreCase("Snake2")) {
            task3(project);
        }
    }

    public static void warmup(@NotNull Project project) {
        List<String> features = new ArrayList<>();
        features.add("Snake");
        features.add("Tail");
        features.add("Position");
        RecommendationData first = Utility.translateFeatracerLocation(project,"ThreadsController.java::174-192", features);

        List<String> features2 = new ArrayList<>();
        features2.add("Food");
        RecommendationData second = Utility.translateFeatracerLocation(project, "SquareToLightUp.java::5-5", features2);

        List<String> features3 = new ArrayList<>();
        features3.add("Controls");
        features3.add("Move");
        RecommendationData third = Utility.translateFeatracerLocation(project, "KeyboardListener.java::9-31", features3);

        List<RecommendationData> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.add(third);

        Utility.checkAndInvokeRecommendationWizard(project, list);

    }

    public static void task3(@NotNull Project project) {
        List<String> features1 = new ArrayList<>();
        features1.add("Position");
        features1.add("Move");
        RecommendationData first = Utility.translateFeatracerLocation(project,"ThreadsController.java::24-29", features1); //Block 1 correct

        RecommendationData second = Utility.translateFeatracerLocation(project, "ThreadsController.java::31-32", new ArrayList<>()); // Block do themselves

        List<String> features3 = new ArrayList<>();
        features3.add("Move");
        features3.add("Collision");
        features3.add("Tail");
        RecommendationData third = Utility.translateFeatracerLocation(project, "ThreadsController.java::37-43",features3); //Block reject

        RecommendationData fourth = Utility.translateFeatracerLocation(project, "Window.java::65-65", new ArrayList<>()); //Line do themselves reject

        // Line accept
        List<String> features5 = new ArrayList<>();
        features5.add("Controls");
        RecommendationData fifth = Utility.translateFeatracerLocation(project, "Window.java::68-68", features5);

        List<RecommendationData> list = new ArrayList<>();
        list.add(first);
        list.add(second);
        list.add(third);
        list.add(fourth);
        list.add(fifth);

        Utility.checkAndInvokeRecommendationWizard(project, list);
    }
/*
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
        RecommendationData testBlock = Utility.translateFeatracerLocation(project, "test/testing/FeatRacerTest.java::60-72", testfeatures);
        RecommendationData testLine = Utility.translateFeatracerLocation(project, "ihopethisisirrelevant/FeatRacerTest.java::64-64", testfeatures);


        List<RecommendationData> list = new ArrayList<>();
        list.add(testBlock);
        list.add(testLine);
    //    list.add(outOfBounds);
    //    list.add(classpathinvalid);
     //   list.add(wrongFormat);

        Utility.checkAndInvokeRecommendationWizard(project, list);
    }

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