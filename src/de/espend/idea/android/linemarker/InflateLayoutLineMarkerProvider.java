package de.espend.idea.android.linemarker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.*;
import de.espend.idea.android.utils.AndroidUtils;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class InflateLayoutLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
        for(PsiElement psiElement : elements) {

            if(psiElement instanceof PsiMethodCallExpression) {
                PsiMethod psiMethod = ((PsiCallExpression) psiElement).resolveMethod();
                if(psiMethod != null && ("inflate".equals(psiMethod.getName()) || "setContentView".equals(psiMethod.getName()))) {
                    PsiExpressionList psiExpressionList = ((PsiCallExpression) psiElement).getArgumentList();
                    if(psiExpressionList != null) {
                        PsiExpression[] psiExpressions = psiExpressionList.getExpressions();
                        if(psiExpressions.length > 0 && psiExpressions[0].getText().startsWith("R.layout")) {
                            String layoutText = psiExpressions[0].getText();

                            PsiFile xmlFile = AndroidUtils.findXmlResource(psiElement.getProject(), layoutText);
                            if(xmlFile != null) {

                                NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(AndroidIcons.Views.Fragment).
                                    setTooltipText(xmlFile.getName()).
                                    setTargets(xmlFile);

                                result.add(builder.createLineMarkerInfo(psiElement));
                            }

                        }
                    }
                }
            }

        }
    }
}

