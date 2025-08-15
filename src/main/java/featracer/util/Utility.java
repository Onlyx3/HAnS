package featracer.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import featracer.data.RecommendationData;
import featracer.ui.RecommendationDialogCardWizard;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Utility {


    /**
     *
     * @param project
     * @param location String in the format "package.classname:codeline"
     * @return PsiElement with specified location, null if not found
     */
    public static PsiElement findPsiElementFromLocation(Project project, String location) {
        // Format Input
        String[] split = location.split(":");
        if (split.length != 2) return null;

        String className = split[0];
        int lineNumber = Integer.parseInt(split[1]);
        if (lineNumber < 1) return null;
        return ApplicationManager.getApplication().runReadAction(
                (Computable<PsiElement>) () -> {
                    JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
                    GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
                    PsiClass psiClass = psiFacade.findClass(className, searchScope);

                    if (psiClass == null || psiClass.getContainingFile() == null) return null;
                    PsiFile psiFile = psiClass.getContainingFile();

                    Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                    if (document == null) return null;

                    if (lineNumber > document.getLineCount()) return null;

                    return psiFile.findElementAt(document.getLineStartOffset(lineNumber - 1));

                }
        );
       /* GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        PsiFile[] psiFile = FilenameIndex.getFilesByName(project, className, searchScope); // find non deprecated way to do this

        if (psiFile.length == 0) return null;*/
    }

    public static void checkAndInvokeRecommendationWizard(Project project, List<RecommendationData> list) {
        if(!list.isEmpty()) new RecommendationDialogCardWizard(project, list).show();
    }

    public static String getLatestCommitHash(CheckinProjectPanel panel) {
        Project project = panel.getProject();
        Collection<VirtualFile> commitedFiles = panel.getVirtualFiles();

        ProjectLevelVcsManagerImpl vcsManager = ProjectLevelVcsManagerImpl.getInstanceImpl(project);
        GitRepositoryManager  repositoryManager = GitRepositoryManager.getInstance(project);

       for(VirtualFile commitedFile : commitedFiles) {
           VcsRoot root = vcsManager.getVcsRootObjectFor(commitedFile);
           if(root != null) {
               GitRepository repo = repositoryManager.getRepositoryForRoot(commitedFile);
               if(repo != null) {
                   return repo.getCurrentRevision();
               }
           }
       }
       return null;
    }
}
