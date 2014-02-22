package de.espend.idea.android.action.write;

import com.intellij.codeInsight.actions.ReformatAndOptimizeImportsProcessor;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import de.espend.idea.android.AndroidView;
import de.espend.idea.android.utils.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InflateThisExpressionAction extends BaseIntentionAction {

    final private PsiFile xmlFile;
    final private PsiElement psiElement;

    @Nullable
    private String variableName = null;

    public InflateThisExpressionAction(PsiLocalVariable psiLocalVariable, PsiFile xmlFile) {
        this.xmlFile = xmlFile;
        this.psiElement = psiLocalVariable;
        this.variableName = psiLocalVariable.getName();
    }

    public InflateThisExpressionAction(PsiElement psiElement, PsiFile xmlFile) {
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
        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run() {
                List<AndroidView> androidViews = AndroidUtils.getIDsFromXML(xmlFile);

                PsiStatement psiStatement = PsiTreeUtil.getParentOfType(psiElement, PsiStatement.class);
                if(psiStatement == null) {
                    return;
                }

                PsiClass psiClass = PsiTreeUtil.getParentOfType(psiStatement, PsiClass.class);
                Set<String> fieldSet = new HashSet<String>();
                for(PsiField field: psiClass.getFields()) {
                    fieldSet.add(field.getName());
                }

                PsiElement[] PsiThisExpressions = PsiTreeUtil.collectElements(psiStatement.getParent(), new PsiElementFilter() {
                    @Override
                    public boolean isAccepted(PsiElement element) {
                        return element instanceof PsiThisExpression;
                    }
                });

                Set<String> thisSet = new HashSet<String>();
                for(PsiElement psiThisExpression: PsiThisExpressions) {
                    thisSet.add(psiThisExpression.getParent().getText());
                }

                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiStatement.getProject());
                for (AndroidView v: androidViews) {

                    if(!fieldSet.contains(v.getFieldName())) {
                        String sb = "private " + v.getName() + " " + v.getFieldName() + ";";
                        psiClass.add(elementFactory.createFieldFromText(sb, psiClass));
                    }

                    if(!thisSet.contains("this." + v.getFieldName())) {

                        String sb1;
                        if(variableName != null) {
                            sb1 = String.format("this.%s = (%s) %s.findViewById(R.id.%s);", v.getFieldName(), v.getName(), variableName, v.getFieldName());
                        } else {
                            sb1 = String.format("this.%s = (%s) findViewById(R.id.%s);", v.getFieldName(), v.getName(), v.getFieldName());
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
        return "Field View Variables";
    }
}
