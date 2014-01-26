package de.espend.idea.android.action.generator;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.psi.xml.XmlFile;
import com.sun.deploy.ui.JavaTrayIcon;
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

        return psiElement instanceof PsiIdentifierImpl && psiElement.getParent() instanceof PsiLocalVariable;
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

        InflateViewAnnotator.InflateContainer inflateContainer = InflateViewAnnotator.matchInflate(psiElement);
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
