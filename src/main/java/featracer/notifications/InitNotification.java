package featracer.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import featracer.ui.InitializationDialog;
import org.jetbrains.annotations.NotNull;


public class InitNotification {

    public static void show(@NotNull Project project) {
        Notification notification = NotificationGroupManager.getInstance()
                .getNotificationGroup("featracer.initialization")
                .createNotification("FeatRacer needs initialization with this project", NotificationType.INFORMATION);

        notification.addAction(new AnAction("Configure") {
            public void actionPerformed(@NotNull AnActionEvent e) {
                InitializationDialog dialog = new InitializationDialog(project);
                if(dialog.showAndGet()) {
                    notification.expire();
                }
            }
        });

        notification.notify(project);
    }
}
