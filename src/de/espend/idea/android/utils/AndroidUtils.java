package de.espend.idea.android.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import de.espend.idea.android.AndroidView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class AndroidUtils {

    @Nullable
    public static PsiFile findXmlResource(@Nullable PsiReferenceExpression referenceExpression) {
        if (referenceExpression == null) return null;

        PsiElement firstChild = referenceExpression.getFirstChild();
        if (firstChild == null || !"R.layout".equals(firstChild.getText())) {
            return null;
        }

        PsiElement lastChild = referenceExpression.getLastChild();
        if(lastChild == null) {
            return null;
        }

        String name = String.format("%s.xml", lastChild.getText());
        PsiFile[] foundFiles = FilenameIndex.getFilesByName(referenceExpression.getProject(), name, GlobalSearchScope.allScope(referenceExpression.getProject()));
        if (foundFiles.length <= 0) {
            return null;
        }

        return foundFiles[0];
    }

    public static List<AndroidView> getProjectViews(Project project) {

        List<AndroidView> androidViews = new ArrayList<AndroidView>();
        for(PsiFile psiFile: getLayoutFiles(project)) {
            androidViews.addAll(getIDsFromXML(psiFile));
        }

        return androidViews;
    }

    public static List<PsiFile> getLayoutFiles(Project project) {

        List<PsiFile> psiFileList = new ArrayList<PsiFile>();

        for (VirtualFile virtualFile : FilenameIndex.getAllFilesByExt(project, "xml")) {
            VirtualFile parent = virtualFile.getParent();
            if (parent != null && "layout".equals(parent.getName())) {
                String relative = VfsUtil.getRelativePath(virtualFile, project.getBaseDir(), '/');
                if (relative != null) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                    if (psiFile != null) {
                        psiFileList.add(psiFile);
                    }
                }
            }
        }

        return psiFileList;
    }

    @Nullable
    public static PsiFile findXmlResource(Project project, String layoutName) {

        if (!layoutName.startsWith("R.layout.")) {
            return null;
        }

        layoutName = layoutName.substring("R.layout.".length());

        String name = String.format("%s.xml", layoutName);
        PsiFile[] foundFiles = FilenameIndex.getFilesByName(project, name, GlobalSearchScope.allScope(project));
        if (foundFiles.length <= 0) {
            return null;
        }

        return foundFiles[0];
    }

    @NotNull
    public static List<AndroidView> getIDsFromXML(@NotNull PsiFile f) {
        final ArrayList<AndroidView> ret = new ArrayList<AndroidView>();
        f.accept(new XmlRecursiveElementVisitor() {
            @Override
            public void visitElement(final PsiElement element) {
                super.visitElement(element);
                if (element instanceof XmlTag) {
                    XmlTag t = (XmlTag) element;
                    XmlAttribute id = t.getAttribute("android:id", null);
                    if (id == null) {
                        return;
                    }
                    final String val = id.getValue();
                    if (val == null) {
                        return;
                    }
                    ret.add(new AndroidView(val, t.getName(), id));

                }

            }
        });

        return ret;
    }

    @Nullable
    public static AndroidView getViewType(@NotNull PsiFile f, String findId) {

        // @TODO: replace dup for
        List<AndroidView> views = getIDsFromXML(f);

        for(AndroidView view: views) {
            if(findId.equals(view.getId())) {
                return view;
            }
        }

        return null;
    }

}
