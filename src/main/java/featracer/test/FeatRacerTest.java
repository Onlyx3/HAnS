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
        RecommendationData testBlock = Utility.translateFeatracerLocation(project, "test/testing/FeatRacerTest.java::67-68", testfeatures);
        RecommendationData testLine = Utility.translateFeatracerLocation(project, "ihopethisisirrelevant/FeatRacerTest.java::70", testfeatures);


        List<RecommendationData> list = new ArrayList<>();
        list.add(testLine);
        list.add(testBlock);
    //    list.add(outOfBounds);
    //    list.add(classpathinvalid);
     //   list.add(wrongFormat);

        Utility.checkAndInvokeRecommendationWizard(project, list);
    }


    private void testFunction() {

        int x = 1337;
        int y = 42;

        process();

    }

    private void process() {
        //do nothing
    }
}
