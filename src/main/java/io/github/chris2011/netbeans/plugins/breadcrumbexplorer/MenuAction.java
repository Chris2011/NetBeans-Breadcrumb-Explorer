package io.github.chris2011.netbeans.plugins.breadcrumbexplorer;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.api.editor.EditorActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author Chris
 */
@EditorActionRegistration(
    name = "BreadcrumbExplorer.lblShowPathBreadcrumbs",
    menuPath = "View",
    menuPosition = 950,
    preferencesKey = BreadcrumbExplorerSideBarFactory.KEY_BREADCRUMB_EXPLORER,
    preferencesDefault = BreadcrumbExplorerSideBarFactory.DEFAULT_BREADCRUMB_EXPLORER
)
@NbBundle.Messages("BreadcrumbExplorer.lblShowPathBreadcrumbs=Show P&ath Breadcrumbs")
public class MenuAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
