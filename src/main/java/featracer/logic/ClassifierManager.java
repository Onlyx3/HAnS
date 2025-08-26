package featracer.logic;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class ClassifierManager {

    private ClassifierStrategy strategy;

    public void setStrategy(ClassifierStrategy strategy) {
        this.strategy = strategy;
    }

    public ClassifierStrategy getStrategy() {
        return strategy;
    }

    public static ClassifierManager getInstance(@NotNull Project project) {
        return project.getService(ClassifierManager.class);
    }
}
