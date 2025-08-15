package featracer.data;

import com.intellij.psi.PsiElement;

import java.util.List;

public class RecommendationData {

    private final PsiElement element;
    private final List<String> features;

    public RecommendationData(PsiElement element, List<String> features) {
        this.element = element;
        this.features = features;
    }

    public PsiElement getElement() {
        return element;
    }

    public List<String> getFeatures() {
        return features;
    }
}
