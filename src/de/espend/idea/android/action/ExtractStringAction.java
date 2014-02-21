package de.espend.idea.android.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import icons.AndroidIcons;
import org.jetbrains.android.intentions.AndroidAddStringResourceAction;
import org.jetbrains.annotations.Nullable;

public class ExtractStringAction extends DumbAwareAction {

    public ExtractStringAction() {
        super("Extract String resource", "Extract string resource (string.xml)", AndroidIcons.Android);
    }

    @Override
    public void update(AnActionEvent event) {
        PsiElement psiElement = getPsiElement(event.getData(PlatformDataKeys.PSI_FILE), event.getData(PlatformDataKeys.EDITOR));
        event.getPresentation().setVisible(psiElement instanceof PsiLiteralExpression);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {

        final PsiFile psiFile = event.getData(PlatformDataKeys.PSI_FILE);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR);
        final Project project = event.getData(PlatformDataKeys.PROJECT);

        if(project == null) {
            return;
        }

        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        new AndroidAddStringResourceAction().invoke(project, editor, psiFile);
                    }
                });

            }
        }, "Extract string resource", "Android Studio Plugin");

    }

    @Nullable
    protected static PsiElement getPsiElement(@Nullable PsiFile file, @Nullable Editor editor) {

        if(file == null || editor == null) {
            return null;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        return element != null ? element.getParent() : null;
    }

}
