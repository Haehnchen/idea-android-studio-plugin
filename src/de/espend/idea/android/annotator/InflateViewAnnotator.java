package de.espend.idea.android.annotator;

import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.util.PsiTreeUtil;
import de.espend.idea.android.action.write.InflateLocalVariableAction;
import de.espend.idea.android.action.write.InflateThisExpressionAction;
import de.espend.idea.android.utils.AndroidUtils;
import de.espend.idea.android.utils.JavaPsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InflateViewAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        InflateContainer inflateContainer = matchInflate(psiElement);
        if(inflateContainer == null) {
            return;
        }

        Annotation inflateLocal = annotationHolder.createWeakWarningAnnotation(psiElement, null);
        inflateLocal.setHighlightType(null);
        inflateLocal.registerFix(new InflateLocalVariableAction(inflateContainer.getPsiLocalVariable(), inflateContainer.getXmlFile()));

        Annotation inflateThis = annotationHolder.createWeakWarningAnnotation(psiElement, null);
        inflateThis.setHighlightType(null);
        inflateThis.registerFix(new InflateThisExpressionAction(inflateContainer.getPsiLocalVariable(), inflateContainer.getXmlFile()));

    }

    @Nullable
    public static InflateContainer matchInflate(@Nullable PsiElement psiElement) {
        if(!(psiElement instanceof PsiIdentifierImpl)) {
            return null;
        }

        // View "rootView" = inflater.inflate(R.layout.fragment_main, container, false);
        PsiElement psiLocalVariable = psiElement.getParent();
        if(psiLocalVariable instanceof PsiLocalVariable) {
            return matchInflate((PsiLocalVariable) psiLocalVariable);
        }

        return null;

    }

    @Nullable
    public static InflateContainer matchInflate(PsiLocalVariable psiLocalVariable) {
        PsiType psiType = psiLocalVariable.getType();
        if(psiType instanceof PsiClassReferenceType) {
            PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.findChildOfType(psiLocalVariable, PsiMethodCallExpression.class);
            if(psiMethodCallExpression != null) {
                PsiMethod psiMethod = psiMethodCallExpression.resolveMethod();

                // @TODO: replace "inflate"; resolve method and check nethod calls
                if(psiMethod != null && psiMethod.getName().equals("inflate")) {
                    PsiExpression[] expressions = psiMethodCallExpression.getArgumentList().getExpressions();
                    if(expressions.length > 0 && expressions[0] instanceof PsiReferenceExpression) {
                        PsiFile xmlFile = AndroidUtils.findXmlResource((PsiReferenceExpression) expressions[0]);
                        if(xmlFile != null) {
                            return new InflateContainer(xmlFile, ((PsiLocalVariable) psiLocalVariable));
                        }
                    }
                }
            }
        }

        return null;
    }

    public static class InflateContainer {

        final private PsiFile xmlFile;
        final private PsiLocalVariable psiLocalVariable;

        public InflateContainer(PsiFile xmlFile, PsiLocalVariable psiLocalVariable) {
            this.xmlFile = xmlFile;
            this.psiLocalVariable = psiLocalVariable;
        }

        public PsiLocalVariable getPsiLocalVariable() {
            return psiLocalVariable;
        }

        public PsiFile getXmlFile() {
            return xmlFile;
        }

    }




}

