package featracer.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import featracer.data.RecommendationData;
import featracer.ui.RecommendationDialogCardWizard;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Utility {


    public static RecommendationData translateFeatracerLocation(Project project, String location, List<String> features) {
        String[] split = location.split("::");
        if(split.length != 2) return null;

        String fileName = new File(split[0]).getName();
        String lines = split[1];

      //  String newLocation = fileName.substring(0, fileName.lastIndexOf('.')) + ":" + lines;
        String newLocation = fileName + ":" + lines;
        return makeRecommendationData(project, newLocation, features);
    }

    private static RecommendationData makeRecommendationData(Project project, String location, List<String> features) {
        System.out.println("Making recommendation data for " + location);
        String[] split = location.split(":");
        if(split.length != 2) {
            System.out.println("Skip: Incorrect format for " + location);
            return null;
        }
        String className = split[0];
        String lineString = split[1];

        boolean isCodeBlock;
        int startLine_1;
        int endLine_1;

        try {
            if(lineString.contains("-")) {
                String[] lineSplit = lineString.split("-");
                if (lineSplit.length != 2) {
                    System.out.println("Skip: Line range not in correct format for " + location);
                    return null;
                }
                startLine_1 = Integer.parseInt(lineSplit[0]);
                endLine_1 = Integer.parseInt(lineSplit[1]);
                isCodeBlock = (startLine_1 != endLine_1);
            } else {
                startLine_1 = Integer.parseInt(lineString);
                endLine_1 = startLine_1;
                isCodeBlock = false;
            }
        } catch (NumberFormatException e) {
            return null;
        }

        if(startLine_1 < 1 || endLine_1 < startLine_1) {
            return null;
        }

        return ApplicationManager.getApplication().runReadAction((Computable<RecommendationData>) () -> {
                    PsiFile psiFile = findPsiFileByClassName(project, className);
                    if (psiFile == null) {
                        System.out.println("Skip: PsiFile not found for " + className);
                        return null;
                    }

                    Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                    if (document == null) {
                        System.out.println("Skip: Document not found for " + className);
                        return null;
                    }

                    int lineCount = document.getLineCount();
                    if (lineCount == 0 || startLine_1 > lineCount || endLine_1 > lineCount) {
                        System.out.println("Skip: Location not within bounds of document");
                        return null; //reminder: start/endLine_1 is 1-base, doc is 0
                    }

                    //find stuff
                    int startLine_0 = startLine_1 - 1;
                    int endLine_0 = endLine_1 - 1;

                    int startLineStart = document.getLineStartOffset(startLine_0);
                    int startLineEnd = document.getLineEndOffset(startLine_0);
                    PsiElement startElement = null;

                    for (int o = startLineStart; o < Math.min(startLineEnd, document.getTextLength()); o++) {
                        PsiElement e = psiFile.findElementAt(o);
                        if (e == null) continue;
                        int eStart = e.getTextRange().getStartOffset();
                        if (eStart >= startLineStart && eStart < startLineEnd) {
                            startElement = e;
                            break;
                        }
                    }

                    if (startElement == null) startElement = psiFile.findElementAt(startLineStart);
                    if (startElement == null) {
                        System.out.println("Skip: Element not found for " + className);
                        return null;
                    }

                    if (!isCodeBlock) {
                        return new RecommendationData(startElement, features, false, null);
                    } else {
                        int endLineStart = document.getLineStartOffset(endLine_0);
                        int endLineEnd = document.getLineEndOffset(endLine_0);
                        PsiElement endElement = null;

                        for (int o = Math.min(endLineEnd - 1, document.getTextLength() - 1); o >= endLineStart; o--) {
                            PsiElement e = psiFile.findElementAt(o);
                            if (e == null) continue;
                            int eEnd = e.getTextRange().getEndOffset();
                            if (eEnd <= endLineEnd && eEnd > endLineStart) {
                                endElement = e;
                                break;
                            }
                        }
                        if (endElement == null) endElement = psiFile.findElementAt(endLineStart);
                        if (endElement == null) {
                            System.out.println("Skip: Element not found for " + className);
                            return null;
                        }
                        if(endElement == startElement) return new RecommendationData(startElement, features, false, null);
                        return new RecommendationData(startElement, features, true, endElement);
                    }
                });


            /*
            PsiElement startElement = psiFile.findElementAt(document.getLineStartOffset(startLine_0));
            if(startElement == null) {
                System.out.println("Skip: Start PsiElement not found for " + className);
                return null;
            }

            if(!isCodeBlock) {
                return new RecommendationData(startElement, features, false, null);
            } else {
                PsiElement endElement = psiFile.findElementAt(document.getLineStartOffset(endLine_0)); // TODO:
                if(endElement == null) {
                    System.out.println("Skip: End PsiElement not found for " + className);
                    return null;
                }
                return new RecommendationData(startElement, features, true, endElement);
            }
        });*/
    }

/*
    private static RecommendationData makeRecommendationData(Project project, String location, List<String> features) {
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
                if(lineSplit[0].equals(lineSplit[1])) {
                    startLine = Integer.parseInt(lineSplit[0]);
                    endLine = startLine;
                } else {
                    isCodeBlock = true;
                    startLine = Integer.parseInt(lineSplit[0]);
                    endLine = Integer.parseInt(lineSplit[1]);
                }
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
                        System.out.println("Attempting to find PsiFile for " + className);
                        PsiFile psiFile = findPsiFileByClassName(project, className);
                        System.out.println("PsiFile for " + className + " found: " + psiFile);
                        if(psiFile == null) return null;
                        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                        if (document == null || startLine >= document.getLineCount()) return null;

                        return psiFile.findElementAt(document.getLineStartOffset(startLine));
                    }
            );
            if(line == null) return null;
            System.out.println("For the line " + className + ":" + startLine + "-" + endLine + " we got the following startelem: " + line.toString() + " " + line.getTextOffset());
            return new RecommendationData(line, features, false, null);

        } else { // Logic for Code Blocks
            PsiElement[] elements = ApplicationManager.getApplication().runReadAction(
                    (Computable<PsiElement[]>) () -> {
                        System.out.println("Attempting to find PsiFile for " + className);
                        PsiFile psiFile = findPsiFileByClassName(project, className);
                        if(psiFile == null) return null;
                        System.out.println("PsiFile for " + className + " found: " + psiFile.getClass());
                        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
                        if (document == null || startLine >= document.getLineCount() || endLine >= document.getLineCount()) return null;
                        PsiElement startElem =  psiFile.findElementAt(document.getLineStartOffset(startLine));
                        PsiElement endElem =  psiFile.findElementAt(document.getLineStartOffset(endLine));
                        return new PsiElement[]{startElem, endElem};
            }
            );
            if(elements == null || elements[0] == null || elements[1] == null) return null;
            System.out.println("For the block " + className + ":" + startLine + "-" + endLine + " we got the following startelem: " + elements[0].toString() + " " + elements[0].getTextOffset());
            return new RecommendationData(elements[0], features, true, elements[1]);
        }


    }*/

    private static PsiFile findPsiFileByClassName(Project project, String className) {
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
        Collection<VirtualFile> virtualFiles = FilenameIndex.getVirtualFilesByName(className, searchScope);
        System.out.println("Found " + virtualFiles.size() + " files for " + className);

        if(virtualFiles.isEmpty()) return null;

        Set<String> paths = new HashSet<>();
        VirtualFile rf = null;

        for(VirtualFile virtualFile : virtualFiles) {
            String path = virtualFile.getCanonicalPath();
            if(path != null) {
                paths.add(path);
                rf = virtualFile;
            }
        }

        if(paths.size() == 1) {
            return PsiManager.getInstance(project).findFile(rf);
        }
        return null;
    }





    public static void checkAndInvokeRecommendationWizard(Project project, List<RecommendationData> list) {
        //if(list != null && !list.isEmpty()) new RecommendationDialogCardWizard(project, list).show();
        //else System.out.print("No new recommendations found :)");
        new RecommendationDialogCardWizard(project, list).show();
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
}
