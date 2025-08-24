package featracer.data;

import com.intellij.psi.PsiElement;

import java.util.List;

public class RecommendationData {

    private final PsiElement element;
    private final List<String> features;

    private final PsiElement elementEnd;
    private final boolean isCodeBlock;

    public RecommendationData(PsiElement element, List<String> features, boolean isCodeBlock,  PsiElement elementEnd) {
        this.element = element;
        this.features = features;
        this.elementEnd = elementEnd;
        this.isCodeBlock = isCodeBlock;
    }

    public PsiElement getElement() {
        return element;
    }

    public PsiElement getElementEnd() {return elementEnd;}

    public boolean isCodeBlock() {return isCodeBlock;}

    public List<String> getFeatures() {
        return features;
    }
}
