package de.espend.idea.android.utils;


import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import de.espend.idea.android.AndroidView;
import de.espend.idea.android.annotator.InflateViewAnnotator;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AndroidViewUtil {

    public static Icon getCoreIconWithExtends(AndroidView view, PsiClass psiClass) {

        Field[] declaredFields = AndroidIcons.Views.class.getDeclaredFields();

        // direct class
        if(view.getName().equals(psiClass.getQualifiedName())) {
            String viewName = view.getName();
            Icon icon = getCoreIconName(declaredFields, viewName);
            if(icon != null) {
                return icon;
            }
        }

        // extended
        for(PsiClassType classType : PsiClassImplUtil.getExtendsListTypes(psiClass)) {
            PsiClass resolve = classType.resolve();
            if(resolve != null) {
                String viewName = resolve.getQualifiedName();
                Icon icon = getCoreIconName(declaredFields, viewName);
                if(icon != null) {
                    return icon;
                }
            }
        }

        return null;
    }

    @Nullable
    private static Icon getCoreIconName(Field[] declaredFields, String viewName) {

        if(viewName.contains(".")) {
            viewName = viewName.substring(viewName.lastIndexOf(".") +1 );
            for(Field field: declaredFields) {
                if(field.getName().equalsIgnoreCase(viewName) && java.lang.reflect.Modifier.isStatic(field.getModifiers()) && field.getType() == Icon.class) {
                    try {
                        return (Icon) field.get(null);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }

    @Nullable
    public static AndroidView getAndroidView(PsiMethodCallExpression psiMethodCallExpression) {

        PsiExpression[] psiExpressions = psiMethodCallExpression.getArgumentList().getExpressions();
        if(psiExpressions.length > 0) {
            String viewId = psiExpressions[0].getText();

            PsiMethod psiMethod = psiMethodCallExpression.resolveMethod();
            if(psiMethod != null && psiMethod.getName().equals("findViewById")) {

                // methodExpression direct catch!?
                PsiElement methodExpression = psiMethodCallExpression.getFirstChild();
                if(methodExpression instanceof PsiReferenceExpression) {
                    PsiExpression psiExpression = ((PsiReferenceExpression) methodExpression).getQualifierExpression();
                    if(psiExpression != null) {
                        PsiReference psiLocalVariableReference = psiExpression.getReference();
                        if(psiLocalVariableReference != null) {
                            PsiElement psiLocalVariable = psiLocalVariableReference.resolve();
                            if(psiLocalVariable instanceof PsiLocalVariable) {
                                InflateViewAnnotator.InflateContainer inflateContainer = InflateViewAnnotator.matchInflate((PsiLocalVariable) psiLocalVariable);
                                if(inflateContainer != null) {
                                    PsiFile psiFile = inflateContainer.getXmlFile();
                                    return AndroidUtils.getViewType(psiFile, viewId);
                                }
                            }
                        }
                    }
                }
            }
        }

        return  null;


    }

    public static List<PsiFile> findLayoutFilesInsideMethod(PsiMethod psiMethod) {
        final List<PsiFile> xmlFiles = new ArrayList<PsiFile>();

        PsiTreeUtil.processElements(psiMethod, new PsiElementProcessor() {

            @Override
            public boolean execute(@NotNull PsiElement element) {

                if (element instanceof PsiMethodCallExpression) {
                    PsiMethod psiMethodResolved = ((PsiMethodCallExpression) element).resolveMethod();
                    if (psiMethodResolved != null) {
                        String methodName = psiMethodResolved.getName();
                        // @TODO: implement instance check
                        if ("setContentView".equals(methodName)) {
                            PsiExpression[] expressions = ((PsiMethodCallExpression) element).getArgumentList().getExpressions();
                            if (expressions.length > 0 && expressions[0] instanceof PsiReferenceExpression) {
                                PsiFile xmlFile = AndroidUtils.findXmlResource((PsiReferenceExpression) expressions[0]);
                                if (xmlFile != null) {
                                    xmlFiles.add(xmlFile);
                                }
                            }

                        }

                    }

                }

                return true;
            }
        });


        return xmlFiles;
    }

    @NotNull
    public static Icon getViewIcon(Project project, AndroidView androidView) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        PsiClass psiClass = psiFacade.findClass(androidView.getName(), GlobalSearchScope.allScope(project));
        if(psiClass == null) {
            return AndroidIcons.Views.View;
        }

        Icon icon = AndroidViewUtil.getCoreIconWithExtends(androidView, psiClass);

        return icon != null ? icon : AndroidIcons.Views.View;
    }

}
