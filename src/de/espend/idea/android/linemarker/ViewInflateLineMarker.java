package de.espend.idea.android.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.search.GlobalSearchScope;
import de.espend.idea.android.AndroidView;
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
                AndroidView androidView = AndroidViewUtil.getAndroidView((PsiMethodCallExpression) psiElement);
                if(androidView != null) {
                    attachLineIcon(androidView, psiElement, result);
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
