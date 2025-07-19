package featracer.commitwatch;

public interface CommitListener {

    /**
     * Invoked after a commit has been commited
     *
     * @param info {@link CommitInfo} containing data for the commit
     */
    void onCommit(CommitInfo info);
}
