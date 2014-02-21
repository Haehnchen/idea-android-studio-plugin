package de.espend.idea.android.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ConstantFunction;
import de.espend.idea.android.RelatedPopupGotoLineMarker;
import de.espend.idea.android.utils.AndroidUtils;
import icons.AndroidIcons;
import org.jetbrains.android.AndroidLineMarkerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FragmentRelatedFileLineMarker implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

        for(PsiElement psiElement : elements) {

            List<GotoRelatedItem> gotoRelatedItems = new ArrayList<GotoRelatedItem>();
            List<PsiFile> psiFiles = new ArrayList<PsiFile>();

            // android studio provide line marker with xml targets only on root classes not on class inside classes like fragments
            // we support all of them :)
            if(psiElement instanceof PsiIdentifier && psiElement.getParent() instanceof PsiClass && !(psiElement.getParent().getParent() instanceof PsiFile)) {

                // simple hack activity provide this on core
                if(isFragmentClass((PsiClass) psiElement.getParent())) {
                    Collection<PsiMethodCallExpression> PsiMethodCallExpressions = PsiTreeUtil.collectElementsOfType(psiElement.getParent(), PsiMethodCallExpression.class);
                    for(PsiMethodCallExpression methodCallExpression: PsiMethodCallExpressions) {
                        PsiMethod psiMethod = methodCallExpression.resolveMethod();
                        if(psiMethod != null && psiMethod.getName().equals("inflate")) {
                            PsiExpression[] expressions = methodCallExpression.getArgumentList().getExpressions();
                            if(expressions.length > 0 && expressions[0] instanceof PsiReferenceExpression) {
                                PsiFile xmlFile = AndroidUtils.findXmlResource((PsiReferenceExpression) expressions[0]);
                                if(xmlFile != null && !psiFiles.contains(xmlFile)) {
                                    psiFiles.add(xmlFile);
                                    gotoRelatedItems.add(new GotoRelatedItem(xmlFile));
                                }
                            }
                        }
                    }
                }

            }

            if(gotoRelatedItems.size() > 0) {
                result.add(new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextOffset(), AndroidIcons.AndroidToolWindow, 6, new ConstantFunction<PsiElement, String>("Related Files"), new RelatedPopupGotoLineMarker.NavigationHandler(gotoRelatedItems)));
            }

        }

    }

    private boolean isFragmentClass(PsiClass psiClass) {

        PsiReferenceList extendsList = psiClass.getExtendsList();
        if(extendsList == null) {
            return false;
        }

        // @TODO: replace this one with instance check
        PsiClassType[] tests = extendsList.getReferencedTypes();
        for(PsiClassType psiClassType: tests) {
            if(psiClassType.getClassName().contains("Activity")) {
                return false;
            }
            if(psiClassType.getClassName().contains("Fragment")) {
                return true;
            }
        }

        return false;
    }

}
