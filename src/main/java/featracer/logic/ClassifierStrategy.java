package featracer.logic;

public interface ClassifierStrategy {
    /**
     *
     * @param projectPath The path for the project which needs to be initialized
     * @param startCommit Start everything from startCommit onwards
     */
    void initializeProject(String projectPath, int startCommit, String analysisDirPath, String allowedFileExtensions);
    void invoke(); //TODO
    void update(); //TODO
}
