package de.espend.idea.android.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import de.espend.idea.android.AndroidView;
import de.espend.idea.android.utils.AndroidUtils;
import de.espend.idea.android.utils.AndroidViewUtil;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.List;

public class ViewInflateLineMarker implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

        for(PsiElement psiElement : elements) {
            if(psiElement instanceof PsiMethodCallExpression) {
                attachFindViewByIdInflate((PsiMethodCallExpression) psiElement, result);
                attachFindViewByIdSetContentViews((PsiMethodCallExpression) psiElement, result);
            }
        }

    }
    private void attachFindViewByIdInflate(PsiMethodCallExpression psiMethodCallExpression, @NotNull Collection<LineMarkerInfo> result) {
        AndroidView androidView = AndroidViewUtil.getAndroidView(psiMethodCallExpression);
        if(androidView != null) {
            attachLineIcon(androidView, psiMethodCallExpression, result);
        }
    }

    private void attachFindViewByIdSetContentViews(PsiMethodCallExpression psiMethodCallExpression, @NotNull Collection<LineMarkerInfo> result) {

        PsiExpression[] psiExpressions = psiMethodCallExpression.getArgumentList().getExpressions();
        if(psiExpressions.length == 0) {
            return;
        }

        PsiMethod psiMethodResolved = psiMethodCallExpression.resolveMethod();
        if(psiMethodResolved == null) {
            return;
        }

        if("findViewById".equals(psiMethodResolved.getName())) {
            String viewId = psiExpressions[0].getText();

            PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiMethodCallExpression, PsiMethod.class);
            if(psiMethod != null) {
                for(PsiFile psiFile : AndroidViewUtil.findLayoutFilesInsideMethod(psiMethod)) {
                    AndroidView androidView = AndroidUtils.getViewType(psiFile, viewId);
                    if(androidView != null) {
                        attachLineIcon(androidView, psiMethodCallExpression, result);
                    }
                }
            }
        }

    }

    private void attachLineIcon(@NotNull AndroidView view, @NotNull PsiElement psiElement, @NotNull Collection<LineMarkerInfo> result) {

        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(psiElement.getProject());
        PsiClass psiClass = psiFacade.findClass(view.getName(), GlobalSearchScope.allScope(psiElement.getProject()));
        if(psiClass == null) {
            return;
        }

        Icon icon = AndroidViewUtil.getCoreIconWithExtends(view, psiClass);
        if(icon == null) {
            icon = AndroidIcons.Views.View;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(icon).
            setTooltipText(view.getName()).
            setTargets(view.getXmlTarget());

        result.add(builder.createLineMarkerInfo(psiElement));
    }

}
