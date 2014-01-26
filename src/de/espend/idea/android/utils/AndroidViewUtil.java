package de.espend.idea.android.utils;


import com.intellij.psi.*;
import com.intellij.psi.impl.PsiClassImplUtil;
import de.espend.idea.android.AndroidView;
import de.espend.idea.android.annotator.InflateViewAnnotator;
import icons.AndroidIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.Field;

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

}
