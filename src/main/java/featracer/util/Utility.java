package featracer.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

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
}
