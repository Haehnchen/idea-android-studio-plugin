package de.espend.idea.android.action.generator;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import de.espend.idea.android.annotator.InflateViewAnnotator;
import icons.AndroidIcons;
import org.jetbrains.annotations.NotNull;

abstract public class AbstractInflateViewAction extends BaseGenerateAction {

    public AbstractInflateViewAction() {
        super(null);
    }

    @Override
    protected boolean isValidForFile(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = file.findElementAt(offset);

        if(!PlatformPatterns.psiElement().inside(PsiLocalVariable.class).accepts(psiElement)) {
            return false;
        }

        PsiLocalVariable psiLocalVariable = PsiTreeUtil.getParentOfType(psiElement, PsiLocalVariable.class);
        return InflateViewAnnotator.matchInflate(psiLocalVariable) != null;
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

        PsiLocalVariable psiLocalVariable = PsiTreeUtil.getParentOfType(psiElement, PsiLocalVariable.class);
        InflateViewAnnotator.InflateContainer inflateContainer = InflateViewAnnotator.matchInflate(psiLocalVariable);
        if(inflateContainer == null) {
            return;
        }

        generate(inflateContainer, editor, file);
    }

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        event.getPresentation().setIcon(AndroidIcons.AndroidToolWindow);
    }

    abstract public void generate(InflateViewAnnotator.InflateContainer inflateContainer, Editor editor, @NotNull PsiFile file);

}
