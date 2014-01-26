package de.espend.idea.android.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

public class JavaPsiUtil {

    @Nullable
    public static PsiClass getClass(Project project, String qualifiedClassName) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        return psiFacade.findClass(qualifiedClassName, GlobalSearchScope.allScope(project));
    }

    public static boolean isInstanceOf(PsiClass instance, PsiClass interfaceExtendsClass) {

        String className = interfaceExtendsClass.getQualifiedName();
        if(className == null) {
            return true;
        }

        if(className.equals(instance.getQualifiedName())) {
            return true;
        }

        for(PsiClassType psiClassType: PsiClassImplUtil.getExtendsListTypes(instance)) {
            PsiClass resolve = psiClassType.resolve();
            if(resolve != null) {
                if(className.equals(resolve.getQualifiedName())) {
                    return true;
                }
            }
        }

        for(PsiClass psiInterface: PsiClassImplUtil.getInterfaces(instance)) {
            if(className.equals(psiInterface.getQualifiedName())) {
                return true;
            }
        }

        return false;
    }
}
