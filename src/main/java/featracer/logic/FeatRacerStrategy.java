package featracer.logic;

import com.intellij.openapi.project.Project;
import featracer.data.FeatRacerStateService;
import featracer.data.RecommendationData;
import featracer.util.Utility;
import org.jetbrains.annotations.NotNull;
import se.gu.api.FeatRacerAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeatRacerStrategy implements ClassifierStrategy{

    private Project project;

    public FeatRacerStrategy(Project project) {
        this.project = project;
    }

    @Override
    public List<RecommendationData> initializeProject(String projectPath, int startCommit, String analysisDirPath, String allowedFileExtensions) {

        FeatRacerAPI featRacerAPI = new FeatRacerAPI();
        Map<String, List<String>> result;
        try {
            result = featRacerAPI.initializeProject(projectPath, startCommit, analysisDirPath, allowedFileExtensions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return getRecommendationData(result);
    }

    @Override
    public List<RecommendationData> invoke(String commitHash) {

        FeatRacerAPI featRacerAPI = new FeatRacerAPI();
        FeatRacerStateService  featRacerStateService = FeatRacerStateService.getInstance(project);
        Map<String, List<String>> result;
        try {
            result = featRacerAPI.invokeFeatRacer(project.getProjectFilePath(),
                    featRacerStateService.analysisDirPath,
                    featRacerStateService.allowedFileExtensions,
                    commitHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return getRecommendationData(result);
    }


    @NotNull
    private List<RecommendationData> getRecommendationData(Map<String, List<String>> result) {
        List<RecommendationData> recommendations = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            RecommendationData rec = Utility.translateFeatracerLocation(project, entry.getKey(), entry.getValue());
            if (rec != null) recommendations.add(rec);
        }

        return recommendations;
    }

    @Override
    public void update() {

    }
}
