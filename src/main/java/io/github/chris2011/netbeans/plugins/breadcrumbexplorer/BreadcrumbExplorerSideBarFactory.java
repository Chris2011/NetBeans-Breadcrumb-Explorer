package io.github.chris2011.netbeans.plugins.breadcrumbexplorer;

import javax.swing.JComponent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.SideBarFactory;

/**
 *
 * @author Chris
 */
public class BreadcrumbExplorerSideBarFactory implements SideBarFactory {
    public static final String KEY_BREADCRUMB_EXPLORER = "enable.breadcrumb_explorer";
    public static final boolean DEFAULT_BREADCRUMB_EXPLORER = true;

    @Override
    public JComponent createSideBar(JTextComponent target) {
        final Document doc = target.getDocument();

        return new BreadcrumbExplorerComponent(doc);
    }
}
