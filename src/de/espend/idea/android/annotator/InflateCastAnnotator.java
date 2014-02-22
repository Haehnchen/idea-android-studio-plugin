package de.espend.idea.android.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import de.espend.idea.android.AndroidView;
import de.espend.idea.android.utils.AndroidUtils;
import de.espend.idea.android.utils.AndroidViewUtil;
import de.espend.idea.android.utils.JavaPsiUtil;
import org.jetbrains.annotations.NotNull;

public class InflateCastAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {

        // check cast:
        // this.newButton = (CheckBox) rootView.findViewById(R.id.newButton);
        // CheckBox newButton = (CheckBox) rootView.findViewById(R.id.newButton);
        if(psiElement instanceof PsiTypeElement) {
            PsiElement castExpression = psiElement.getParent();
            if(castExpression instanceof PsiTypeCastExpression) {
                PsiExpression psiMethodCallExpression = ((PsiTypeCastExpression) castExpression).getOperand();
                if(psiMethodCallExpression instanceof PsiMethodCallExpression) {
                    PsiJavaCodeReferenceElement psiJavaCodeReferenceElement = ((PsiTypeElement) psiElement).getInnermostComponentReferenceElement();
                    if(psiJavaCodeReferenceElement != null) {
                        PsiElement castClass = psiJavaCodeReferenceElement.resolve();
                        if(castClass instanceof PsiClass) {
                            AndroidView androidView = AndroidViewUtil.getAndroidView((PsiMethodCallExpression) psiMethodCallExpression);
                            if(androidView != null) {
                                PsiClass viewClass = JavaPsiUtil.getClass(psiElement.getProject(), androidView.getName());
                                if(viewClass != null) {
                                    if(!JavaPsiUtil.isInstanceOf(viewClass, (PsiClass) castClass)) {
                                        annotationHolder.createErrorAnnotation(psiElement, String.format("Wrong cast need %s", viewClass.getQualifiedName()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // activity layouts
            // @TODO: simplify, use view callback :)
            if(castExpression instanceof PsiTypeCastExpression) {
                PsiExpression psiMethodCallExpression = ((PsiTypeCastExpression) castExpression).getOperand();
                if(psiMethodCallExpression instanceof PsiMethodCallExpression) {
                    PsiJavaCodeReferenceElement psiJavaCodeReferenceElement = ((PsiTypeElement) psiElement).getInnermostComponentReferenceElement();
                    if(psiJavaCodeReferenceElement != null) {
                        PsiElement castClass = psiJavaCodeReferenceElement.resolve();
                        if(castClass instanceof PsiClass) {

                            PsiExpression[] psiExpressions = ((PsiMethodCallExpression) psiMethodCallExpression).getArgumentList().getExpressions();
                            if(psiExpressions.length > 0 && psiExpressions[0].getText().startsWith("R.id")) {
                                String viewId = psiExpressions[0].getText();

                                PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiMethodCallExpression, PsiMethod.class);
                                if(psiMethod != null) {
                                    for(PsiFile psiFile : AndroidViewUtil.findLayoutFilesInsideMethod(psiMethod)) {
                                        AndroidView androidView = AndroidUtils.getViewType(psiFile, viewId);
                                        if(androidView != null) {
                                            PsiClass viewClass = JavaPsiUtil.getClass(psiElement.getProject(), androidView.getName());
                                            if(viewClass != null) {
                                                if(!JavaPsiUtil.isInstanceOf(viewClass, (PsiClass) castClass)) {
                                                    annotationHolder.createErrorAnnotation(psiElement, String.format("Wrong cast need %s", viewClass.getQualifiedName()));
                                                }
                                            }
                                        }
                                    }
                                }

                            }

                        }
                    }
                }
            }

        }

    }

}

