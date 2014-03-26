package de.espend.idea.android.action.write;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.codeStyle.CodeStyleManagerImpl;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author max
 */
public class ReformatAndOptimizeImportsProcessor extends AbstractLayoutCodeProcessor {
    public static final String COMMAND_NAME = CodeInsightBundle.message("progress.reformat.and.optimize.common.command.text");
    private static final String PROGRESS_TEXT = CodeInsightBundle.message("reformat.and.optimize.progress.common.text");

    private final OptimizeImportsProcessor myOptimizeImportsProcessor;
    private final ReformatCodeProcessor myReformatCodeProcessor;

    public ReformatAndOptimizeImportsProcessor(Project project, PsiFile file, boolean processChangedTextOnly) {
        super(project, file, PROGRESS_TEXT, COMMAND_NAME, processChangedTextOnly);
        myOptimizeImportsProcessor = new OptimizeImportsProcessor(project, file);
        myReformatCodeProcessor = new ReformatCodeProcessor(project, file, null, processChangedTextOnly);
    }

    @NotNull
    @Override
    protected FutureTask<Boolean> prepareTask(@NotNull PsiFile psiFile, boolean b) throws IncorrectOperationException {
        return new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return true;
            }
        });
    }

    @Override
    @NotNull
    public FutureTask<Boolean> preprocessFile(@NotNull PsiFile file, boolean processChangedTextOnly) throws IncorrectOperationException {
        final FutureTask<Boolean> reformatTask = myReformatCodeProcessor.preprocessFile(file, processChangedTextOnly);
        final FutureTask<Boolean> optimizeImportsTask = myOptimizeImportsProcessor.preprocessFile(file, false);
        return new FutureTask<Boolean>(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                reformatTask.run();
                if (!reformatTask.get() || reformatTask.isCancelled()) {
                    return false;
                }

                CodeStyleManagerImpl.setSequentialProcessingAllowed(false);
                try {
                    optimizeImportsTask.run();
                    return optimizeImportsTask.get() && !optimizeImportsTask.isCancelled();
                }
                finally {
                    CodeStyleManagerImpl.setSequentialProcessingAllowed(true);
                }
            }
        });
    }
}