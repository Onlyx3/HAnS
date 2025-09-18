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
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.SlowOperations;
import featracer.data.RecommendationData;
import featracer.ui.RecommendationDialogCardWizard;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Utility {


    public static RecommendationData translateFeatracerLocation(Project project, String location, List<String> features) {
        String[] split = location.split("::");
        if(split.length != 2) return null;

        String fileName = new File(split[0]).getName();
        String lines = split[1];

        String newLocation = fileName.substring(0, fileName.lastIndexOf('.')) + ":" + lines;
        return makeRecommendationData(project, newLocation, features);
    }


    public static RecommendationData makeRecommendationData(Project project, String location, List<String> features) {
        String[] split = location.split(":");
        if (split.length != 2) return null; // Wrong Format

        String className = split[0];
        String lineString = split[1];

        boolean isCodeBlock = false;
        int startLine;
        int endLine;

        try {

            if (lineString.contains("-")) {
                String[] lineSplit = lineString.split("-");
                if (lineSplit.length != 2) return null; //Wrong format
                isCodeBlock = true;
                startLine = Integer.parseInt(lineSplit[0]);
                endLine = Integer.parseInt(lineSplit[1]);
            } else {
                startLine = Integer.parseInt(lineString);
                endLine = startLine;
            }
        } catch (NumberFormatException e) {
            return null; // Wrong Format
        }

        if(startLine < 1) return null;
        if(isCodeBlock && endLine < startLine) return null;

        if(!isCodeBlock) {
            //Logic to find and return single lines
            PsiElement line = ApplicationManager.getApplication().runReadAction(
                    (Computable<PsiElement>) () -> {
                        PsiClass psiClass = findPsiClassbyName(project, className);
                        if (psiClass == null || psiClass.getContainingFile() == null) return null;
                        PsiFile  psiFile = psiClass.getContainingFile();
                        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                        if (document == null || startLine > document.getLineCount()) return null;

                        return psiFile.findElementAt(document.getLineStartOffset(startLine));
                    }
            );

            return new RecommendationData(line, features, false, null);

        } else { // Logic for Code Blocks
            PsiElement[] elements = ApplicationManager.getApplication().runReadAction(
                    (Computable<PsiElement[]>) () -> {
                        PsiClass psiClass = findPsiClassbyName(project, className);
                        if (psiClass == null || psiClass.getContainingFile() == null) return null;
                        PsiFile  psiFile = psiClass.getContainingFile();
                        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                        if (document == null || startLine > document.getLineCount()) return null;
                        PsiElement startElem =  psiFile.findElementAt(document.getLineStartOffset(startLine));
                        PsiElement endElem =  psiFile.findElementAt(document.getLineStartOffset(endLine));
                        return new PsiElement[]{startElem, endElem};
            }
            );
            return new RecommendationData(elements[0], features, true, elements[1]);
        }


    }

    private static PsiClass findPsiClassbyName(Project project, String className) {
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);

        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, searchScope);
        if(psiClass != null) return psiClass;

        PsiClass[] classes = PsiShortNamesCache.getInstance(project).getClassesByName(className, searchScope);
        if(classes.length == 1) return classes[0];
        else return null; // Return null when class not found or ambiguous
    }

    /**
     *
     * @param project
     * @param location String in the format "package.classname:codeline"
     * @return PsiElement with specified location, null if not found
     */
    @Deprecated
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

                    return psiFile.findElementAt(document.getLineStartOffset(lineNumber));

                }
        );
       /* GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        PsiFile[] psiFile = FilenameIndex.getFilesByName(project, className, searchScope); // find non deprecated way to do this

        if (psiFile.length == 0) return null;*/
    }

    public static void checkAndInvokeRecommendationWizard(Project project, List<RecommendationData> list) {
        if(!list.isEmpty()) new RecommendationDialogCardWizard(project, list).show();
        else System.out.print("No new recommendations found :)");
    }

    public static String getLatestCommitHash(CheckinProjectPanel panel) {
        Project project = panel.getProject();
        Collection<VirtualFile> commitedFiles = panel.getVirtualFiles();

        ProjectLevelVcsManagerImpl vcsManager = ProjectLevelVcsManagerImpl.getInstanceImpl(project);
        GitRepositoryManager  repositoryManager = GitRepositoryManager.getInstance(project);

       for(VirtualFile commitedFile : commitedFiles) {
           GitRepository repo = repositoryManager.getRepositoryForFileQuick(commitedFile);
           if(repo != null) {
               System.out.println("Found commit hash");
               return repo.getCurrentRevision();
           }
       }
       System.out.println("No commit hash found");
       return null;
    }

    private static void addFeatureTest() { //does nothing just for test
        int i = 1 + 1;
        return;
    }
}
