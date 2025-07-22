package featracer.data;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service(Service.Level.PROJECT)
@State(
        name = "featracer.data.FeatRacerStateService",
        storages = @Storage("state.xml")
)
public class FeatRacerStateService implements PersistentStateComponent<FeatRacerStateService> {

    public boolean isInitialized = false;


    public static FeatRacerStateService getInstance(@NotNull Project project) {
        return project.getService(FeatRacerStateService.class);
    }

    @Override
    public @Nullable FeatRacerStateService getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull FeatRacerStateService featRacerStateService) {
        XmlSerializerUtil.copyBean(featRacerStateService, this);
    }
}
