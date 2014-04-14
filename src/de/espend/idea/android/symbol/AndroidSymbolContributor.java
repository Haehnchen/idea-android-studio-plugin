package de.espend.idea.android.symbol;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import de.espend.idea.android.AndroidView;
import de.espend.idea.android.utils.AndroidUtils;
import de.espend.idea.android.utils.AndroidViewUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AndroidSymbolContributor implements ChooseByNameContributor {

    @NotNull
    @Override
    public String[] getNames(Project project, boolean b) {
        List<AndroidView> androidViewList = AndroidUtils.getProjectViews(project);

        Set<String> names = new HashSet<String>();
        for(AndroidView androidView : androidViewList) {
            String id = androidView.getId();
            if(id.startsWith("R.id.")) {
                names.add(id.substring("R.id.".length()));
            }
        }

        return names.toArray(new String[names.size()]);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String s, String s2, Project project, boolean b) {

        List<NavigationItem> navigationItems = new ArrayList<NavigationItem>();

        List<AndroidView> androidViewList = AndroidUtils.getProjectViews(project);


        Set<String> names = new HashSet<String>();
        for(AndroidView androidView : androidViewList) {
            String id = androidView.getId();
            if(id.startsWith("R.id.")) {
                id = id.substring("R.id.".length());
            }

            if(id.equals(s)) {
                navigationItems.add(new NavigationItemEx(androidView.getXmlTarget(), s, AndroidViewUtil.getViewIcon(project, androidView) , androidView.getName()));
            }

        }

        return navigationItems.toArray(new NavigationItem[navigationItems.size()]);
    }

    public class NavigationItemEx implements NavigationItem, ItemPresentation {

        private PsiElement psiElement;
        private String name;
        private Icon icon;
        private String locationString;
        private boolean appendBundleLocation = true;

        public NavigationItemEx(PsiElement psiElement, final String name, final Icon icon, final String locationString) {
            this.psiElement = psiElement;
            this.name = name;
            this.icon = icon;
            this.locationString = locationString;
        }

        @Nullable
        @Override
        public String getName() {
            return this.name;
        }

        @Nullable
        @Override
        public ItemPresentation getPresentation() {
            return this;
        }

        @Override
        public void navigate(boolean requestFocus) {
            final Navigatable descriptor = PsiNavigationSupport.getInstance().getDescriptor(this.psiElement);
            if (descriptor != null) {
                descriptor.navigate(requestFocus);
            }
        }

        @Override
        public boolean canNavigate() {
            return PsiNavigationSupport.getInstance().canNavigate(this.psiElement);
        }

        @Override
        public boolean canNavigateToSource() {
            return canNavigate();
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Nullable
        @Override
        public String getPresentableText() {
            return name;
        }

        @Nullable
        @Override
        public String getLocationString() {

            if(!this.appendBundleLocation) {
                return this.locationString;
            }

            PsiFile psiFile = psiElement.getContainingFile();

            if(psiFile == null) {
                return this.locationString;
            }

            String locationPathString = this.locationString;
            return locationPathString + " " + psiFile.getName();
        }

        @Nullable
        @Override
        public Icon getIcon(boolean b) {
            return icon;
        }

    }

}
