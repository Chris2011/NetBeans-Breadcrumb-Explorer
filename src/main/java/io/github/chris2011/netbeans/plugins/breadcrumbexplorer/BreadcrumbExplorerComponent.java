package io.github.chris2011.netbeans.plugins.breadcrumbexplorer;

import io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils.FileScanner;
import io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils.PathUtils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.CloseButtonFactory;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;

public class BreadcrumbExplorerComponent extends JPanel implements PreferenceChangeListener {
    public BreadcrumbExplorerComponent(Document forDocument) {
        super(new BorderLayout());

        {
            //add listener for View|Show Outline
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
        }

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        FileObject focusedFileObject = NbEditorUtilities.getFileObject(forDocument);

        if (focusedFileObject != null) {
            createMenuStructure(menuBar, focusedFileObject);
        }

        add(menuBar, BorderLayout.WEST);

        JButton closeButton = CloseButtonFactory.createBigCloseButton();
        add(closeButton, BorderLayout.EAST);

        closeButton.addActionListener((ActionEvent e) -> {
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            prefs.putBoolean(BreadcrumbExplorerSideBarFactory.KEY_BREADCRUMB_EXPLORER, false);

            preferenceChange(null);
        });

        preferenceChange(null);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        setVisible(BreadcrumbExplorerComponent.isBreadcrumbExplorerVisible());
    }

    private void createMenuStructure(JMenuBar menuBar, FileObject focusedFileObject) {
        String[] splitPath = PathUtils.splitPath(PathUtils.getRelativeProjectPath(focusedFileObject));

        for (String name : splitPath) {
            LinkedHashMap<String, String> scannedFolders = FileScanner.getScannedElements(PathUtils.getAbsoluteFolderPath(name, focusedFileObject));
            JMenuItem parentMenu = createMenu(name, scannedFolders);

            menuBar.add(parentMenu);
        }
    }

    private JMenu createMenu(String menuName, LinkedHashMap<String, String> items) {
        JMenu menu = new JMenu(menuName);

        for (Map.Entry<String, String> entry : items.entrySet()) {
            String elementName = entry.getKey();
            String elementPath = entry.getValue();

            JMenuItem menuItem = createMenuItem(elementName, elementPath);
            menu.add(menuItem);
        }

        return menu;
    }

    private JMenuItem createMenuItem(String itemName, String itemPath) {
        if (isDirectory(itemPath)) {
            JMenu subMenu = createSubMenu(itemName, itemPath);

            return subMenu;
        } else {
            JMenuItem menuItem = new JMenuItem(itemName, FileScanner.getFileIcon(itemPath));

            menuItem.addActionListener((ActionEvent e) -> {
                openFileInEditor(itemPath);
            });

            return menuItem;
        }
    }

    private JMenu createSubMenu(String subMenuName, String subMenuPath) {
        LinkedHashMap<String, String> subMenuItems = FileScanner.getScannedElements(subMenuPath);
        JMenu subMenu = createMenu(subMenuName, subMenuItems);

        return subMenu;
    }

    private boolean isDirectory(String path) {
        FileObject fileObject = FileUtil.toFileObject(new File(path));
        return fileObject != null && fileObject.isFolder();
    }

    private void openFileInEditor(String filePath) {
        File file = new File(filePath);
        FileObject fileObject = FileUtil.toFileObject(file);

        if (fileObject != null) {
            try {
                DataObject.find(fileObject).getLookup().lookup(OpenCookie.class).open();
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static boolean isBreadcrumbExplorerVisible() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);

        return prefs.getBoolean(BreadcrumbExplorerSideBarFactory.KEY_BREADCRUMB_EXPLORER, BreadcrumbExplorerSideBarFactory.DEFAULT_BREADCRUMB_EXPLORER);
    }
}
