package featracer.logic;

import featracer.data.RecommendationData;

import java.util.List;

public interface ClassifierStrategy {
    /**
     * This function is used for the initialization
     * @param projectPath The path for the project which needs to be initialized
     * @param startCommit Ignore commits before this number
     * @param analysisDirPath Path where analysis directory should be created as string
     * @param allowedFileExtensions separated by comma
     * @return List of Recommendations and locations. Can be used to create a new RecommendationDialogCardWizard
     */
    List<RecommendationData> initializeProject(String projectPath, int startCommit, String analysisDirPath, String allowedFileExtensions);


    List<RecommendationData> invoke(String commitHash);

    /**
     *
     */
    void update(); //TODO is this even necessary?
}
