package de.espend.idea.android.action.generator;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import de.espend.idea.android.annotator.InflateViewAnnotator;
import de.espend.idea.android.utils.AndroidUtils;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

abstract public class AbstractActivityViewAction extends BaseGenerateAction {

    public AbstractActivityViewAction() {
        super(null);
    }

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = file.findElementAt(offset);

        if(!PlatformPatterns.psiElement().inside(PsiMethodCallExpression.class).accepts(psiElement)) {
            return false;
        }

        PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.getParentOfType(psiElement, PsiMethodCallExpression.class);
        if(psiMethodCallExpression == null) {
            return false;
        }

        PsiMethod psiMethod = psiMethodCallExpression.resolveMethod();
        if(psiMethod == null) {
            return false;
        }

        return "setContentView".equals(psiMethod.getName());
    }

    @Override
    public void actionPerformedImpl(@NotNull final Project project, final Editor editor) {
        PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
        if(file == null) {
            return;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = file.findElementAt(offset);
        if(psiElement == null) {
            return;
        }

        PsiMethodCallExpression psiMethodCallExpression = PsiTreeUtil.getParentOfType(psiElement, PsiMethodCallExpression.class);
        if(psiMethodCallExpression == null) {
            return;
        }

        PsiFile xmlFile = matchInflate(psiMethodCallExpression);
        generate(psiMethodCallExpression, xmlFile, editor, file);
    }

    @Nullable
    public static PsiFile matchInflate(PsiMethodCallExpression psiMethodCallExpression) {

        PsiExpression[] psiExpressions = psiMethodCallExpression.getArgumentList().getExpressions();
        if(psiExpressions.length == 0) {
            return null;
        }

        return AndroidUtils.findXmlResource((PsiReferenceExpression) psiExpressions[0]);
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setIcon(AndroidIcons.AndroidToolWindow);
    }

    abstract public void generate(PsiMethodCallExpression psiMethodCallExpression, PsiFile xmlFile, Editor editor, @NotNull PsiFile file);

}
