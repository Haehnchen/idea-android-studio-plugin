package de.espend.idea.android.action.generator;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethodCallExpression;
import de.espend.idea.android.action.write.InflateLocalVariableAction;
import org.jetbrains.annotations.NotNull;

public class ActivityViewMethodVariable extends AbstractActivityViewAction {

    @Override
    public void generate(PsiMethodCallExpression psiMethodCallExpression, PsiFile xmlFile, Editor editor, @NotNull PsiFile file) {
        new InflateLocalVariableAction(psiMethodCallExpression, xmlFile).invoke(file.getProject(), editor, file);
    }

}
