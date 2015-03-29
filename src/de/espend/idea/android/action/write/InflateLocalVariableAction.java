package de.espend.idea.android.action.write;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.DocumentUtil;
import com.intellij.util.IncorrectOperationException;
import de.espend.idea.android.AndroidView;
import de.espend.idea.android.utils.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InflateLocalVariableAction extends BaseIntentionAction {

    final private PsiFile xmlFile;
    final private PsiElement psiElement;

    @Nullable
    private String variableName = null;

    public InflateLocalVariableAction(PsiLocalVariable psiLocalVariable, PsiFile xmlFile) {
        this.xmlFile = xmlFile;
        this.psiElement = psiLocalVariable;
        this.variableName = psiLocalVariable.getName();
    }

    public InflateLocalVariableAction(PsiElement psiElement, PsiFile xmlFile) {
        this.xmlFile = xmlFile;
        this.psiElement = psiElement;
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return "Android Studio Prettify";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        DocumentUtil.writeInRunUndoTransparentAction(new Runnable() {
            @Override
            public void run() {
                List<AndroidView> androidViews = AndroidUtils.getIDsFromXML(xmlFile);

                PsiStatement psiStatement = PsiTreeUtil.getParentOfType(psiElement, PsiStatement.class);
                if (psiStatement == null) {
                    return;
                }

                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiStatement.getProject());

                PsiElement[] localVariables = PsiTreeUtil.collectElements(psiStatement.getParent(), new PsiElementFilter() {
                    @Override
                    public boolean isAccepted(PsiElement element) {
                        return element instanceof PsiLocalVariable;
                    }
                });

                Set<String> variables = new HashSet<String>();
                for (PsiElement localVariable : localVariables) {
                    variables.add(((PsiLocalVariable) localVariable).getName());
                }

                for (AndroidView v : androidViews) {
                    if (!variables.contains(v.getFieldName())) {
                        String sb1;

                        if (variableName != null) {
                            sb1 = String.format("%s %s = (%s) %s.findViewById(%s);", v.getName(), v.getFieldName(), v.getName(), variableName, v.getId());
                        } else {
                            sb1 = String.format("%s %s = (%s) findViewById(%s);", v.getName(), v.getFieldName(), v.getName(), v.getId());
                        }

                        PsiStatement statementFromText = elementFactory.createStatementFromText(sb1, null);
                        psiStatement.getParent().addAfter(statementFromText, psiStatement);
                    }
                }

                JavaCodeStyleManager.getInstance(psiStatement.getProject()).shortenClassReferences(psiStatement.getParent());
                new ReformatAndOptimizeImportsProcessor(psiStatement.getProject(), psiStatement.getContainingFile(), true).run();

            }
        });

    }

    @NotNull
    @Override
    public String getText() {
        return "Local View Variables";
    }
}
