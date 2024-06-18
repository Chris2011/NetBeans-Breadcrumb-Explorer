package io.github.chris2011.netbeans.plugins.breadcrumbexplorer;

import io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils.BreadcrumbExplorerManager;
import io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils.BreadcrumbExplorerObserver;
import io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils.FileScanner;
import io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils.PathUtils;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.awt.CloseButtonFactory;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

public class BreadcrumbExplorerComponent extends JPanel
    implements PreferenceChangeListener, BreadcrumbExplorerObserver {

    private List<CustomPopup> openPopups = new ArrayList<>();
    private boolean showAllIcons = false;
    private final Map<JLabel, String> labelPathMap = new LinkedHashMap<>();

    public BreadcrumbExplorerComponent(Document forDocument) {
        super(new BorderLayout());

        initUI(forDocument);
        BreadcrumbExplorerManager.addObserver(this);
        applyInitialPreferences();

        {
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        setVisible(BreadcrumbExplorerComponent.isBreadcrumbExplorerVisible());

        if (evt != null && "showIconsMacFinderLike".equals(evt.getKey())) {
            showAllIcons = NbPreferences.forModule(BreadcrumbExplorerManager.class).getBoolean("showIconsMacFinderLike",
                BreadcrumbExplorerManager.getOptionState());
            updateIconVisibility();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();

        BreadcrumbExplorerManager.removeObserver(this);
    }

    private void initUI(Document forDocument) {
        JPanel pathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JScrollPane scrollPane = new JScrollPane(pathPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));

        FileObject focusedFileObject = NbEditorUtilities.getFileObject(forDocument);
        if (focusedFileObject != null) {
            createPathStructure(pathPanel, focusedFileObject);
        }

        add(scrollPane, BorderLayout.CENTER);
        JButton closeButton = CloseButtonFactory.createBigCloseButton();
        add(closeButton, BorderLayout.EAST);
        closeButton.addActionListener(e -> closeExplorer());

        addGlobalMouseListener();
    }

    private void applyInitialPreferences() {
        showAllIcons = NbPreferences.forModule(BreadcrumbExplorerManager.class).getBoolean("showIconsMacFinderLike",
            BreadcrumbExplorerManager.getOptionState());
        updateIconVisibility();
    }

    private void closeExplorer() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        prefs.putBoolean(BreadcrumbExplorerSideBarFactory.KEY_BREADCRUMB_EXPLORER, false);

        preferenceChange(null);

    }

    /**
     * Creates the path structure for the provided file object and displays it
     * within the specified panel. This method splits the file object's path
     * into segments, creates a label for each segment, and attaches a mouse
     * listener to each label to handle clicks by opening a custom popup menu
     * showing the contents of the folder represented by the label.
     *
     * @param pathPanel The panel where the path structure will be displayed.
     * @param focusedFileObject The file object whose path is to be displayed.
     */
    private void createPathStructure(JPanel pathPanel, FileObject focusedFileObject) {
        List<String> splitPath = PathUtils.splitPath(PathUtils.getRelativeProjectPath(focusedFileObject));
        List<String> absoluteFolderPath = PathUtils.getAbsoluteFolderPath(splitPath, focusedFileObject);

        IntStream.range(0, splitPath.size()).forEach(i -> {
            int index = absoluteFolderPath.size() == 1 ? 0 : i;
            String name = splitPath.get(index);

            LinkedHashMap<String, String> scannedFolders = FileScanner
                .getScannedElements(absoluteFolderPath.get(index));

            List<String> paths = new ArrayList<>(2);
            paths.add(Utilities.isWindows() ? absoluteFolderPath.get(index).replace("/", "\\")
                : absoluteFolderPath.get(index));
            paths.add(PathUtils.getRelativeFolderPath(absoluteFolderPath.get(index), focusedFileObject)
                .replace("\\", "/").replace("//", "/"));

            JLabel label = null;

            if (i == splitPath.size() - 1) {
                label = createActionsLabel(createPathLabel(name), paths);
            } else {
                label = createPathLabel(name);
                label.addMouseListener(new LabelMouseAdapter(scannedFolders, label));
            }

            label.setIcon(FileScanner.getIconForFileObject(absoluteFolderPath.get(index)));
            labelPathMap.put(label, absoluteFolderPath.get(index));

            pathPanel.add(label);

            if (i < splitPath.size() - 1) {
                JLabel separatorLabel = createActionsLabel(createPathLabel(" > "), paths);
                pathPanel.add(separatorLabel);
            }
        });

        updateIconVisibility();
    }

    private JLabel createActionsLabel(JLabel label, List<String> paths) {
        String absolutePath = paths.get(0);
        String relativePath = paths.get(1);

        LinkedHashMap<String, Runnable> actions = new LinkedHashMap(3);
        actions.put("Copy absolute path", () -> copyPath(absolutePath));
        actions.put("Copy relative path", () -> copyPath(relativePath));
        actions.put("Open containing folder", ()
            -> openContainingFolder(absolutePath));

        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                CustomPopup popup = new CustomPopup(SwingUtilities.getWindowAncestor(label));
                openPopups.add(popup);
                addGlobalKeyListener(popup);

                int maxWidth = 0;
                FontMetrics metrics = popup.getFontMetrics(UIManager.getFont("Label.font"));

                for (String name : actions.keySet()) {
                    int width = metrics.stringWidth(name) + 40; // Include additional space for icons and padding
                    maxWidth = Math.max(maxWidth, width);
                }

                for (Map.Entry<String, Runnable> entry : actions.entrySet()) {
                    JLabel label = createPathLabel(entry.getKey()); // Create a label for each item

                    JPanel panel = new JPanel(new BorderLayout());
                    panel.add(label, BorderLayout.CENTER);
                    panel.setMaximumSize(new Dimension(maxWidth, label.getPreferredSize().height));
                    panel.setPreferredSize(new Dimension(maxWidth, label.getPreferredSize().height));

                    label.addMouseListener(new SeparatorLabelMouseAdapter(actions, label));

                    popup.addItem(panel);
                }

                Point locOnScreen = label.getLocationOnScreen();
                popup.showPopup(label, locOnScreen.x, locOnScreen.y + label.getHeight());

                popup.pack();

                int popupWidth = popup.getSize().width;
                for (Component comp : popup.getContentPane().getComponents()) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        panel.setPreferredSize(new Dimension(popupWidth, panel.getPreferredSize().height));
                        panel.revalidate();
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                label.setBackground(UIManager.getColor("MenuBar.hoverBackground"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                label.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        return label;
    }

    private void copyPath(String path) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(path), null);
        
        NotificationDisplayer.getDefault().notify("Path copied", NotificationDisplayer.Priority.NORMAL.getIcon(), "Path copied successfully", null);
    }

    private void openContainingFolder(String absolutePath) {
        try {
            File file = new File(absolutePath);

            if (!file.exists()) {
                openContainingFolder(file.getParentFile().getAbsolutePath());

                return;
            }

            Desktop.getDesktop().open(file.isDirectory() ? file : file.getParentFile());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private class SeparatorLabelMouseAdapter extends MouseAdapter {

        private Map<String, Runnable> actions;
        private JLabel label;

        public SeparatorLabelMouseAdapter(Map<String, Runnable> actions, JLabel label) {
            this.label = label;
            this.actions = actions;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            closeAllPopups();

            // TODO: Add notification for copy absolute path and relative path to get user feedback
            actions.get(label.getText()).run();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            label.setBackground(UIManager.getColor("MenuBar.hoverBackground"));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            label.setBackground(UIManager.getColor("Panel.background"));
        }
    }

    private class LabelMouseAdapter extends MouseAdapter {

        private final LinkedHashMap<String, String> _popupEntries;
        private final JLabel _pathLabel;

        public LabelMouseAdapter(LinkedHashMap<String, String> popupEntries, JLabel pathLabel) {
            _popupEntries = popupEntries;
            _pathLabel = pathLabel;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            closeAllPopups(); // Close any open popups before opening a new one.
            CustomPopup popupMenu = createFolderCustomPopupMenu(_popupEntries, null); // Create a new popup menu for the
            // current segment.

            // Show the popup menu just below the clicked label.
            popupMenu.showPopup(_pathLabel, _pathLabel.getLocationOnScreen().x,
                _pathLabel.getLocationOnScreen().y + _pathLabel.getHeight());
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            _pathLabel.setBackground(UIManager.getColor("MenuBar.hoverBackground")); // Change the label's background
            // color on mouse hover.
        }

        @Override
        public void mouseExited(MouseEvent e) {
            _pathLabel.setBackground(UIManager.getColor("Panel.background")); // Revert the label's background color
            // when the mouse exits.
        }
    }

    /**
     * Creates a custom popup menu for a given set of items with an optional
     * parent popup. This method initializes a new {@link CustomPopup}, sets its
     * parent if provided, and populates it with items represented by
     * {@link JLabel} components. Each item can represent either a directory or
     * a file. Clicking on a directory item opens a new custom popup menu for
     * its contents, creating a hierarchical structure of popups. Clicking on a
     * file item triggers an action to open the file and closes all open popups.
     *
     * @param items A LinkedHashMap containing the names and paths of items to
     * be included in the popup menu. The key is the item name, and the value is
     * its path.
     * @param parentPopup The parent {@link CustomPopup} of the new popup menu
     * being created. This is used to establish a hierarchical relationship
     * between popups. Can be null if the popup is a root menu.
     * @return The newly created {@link CustomPopup} instance populated with
     * items.
     */
    private CustomPopup createFolderCustomPopupMenu(LinkedHashMap<String, String> items, CustomPopup parentPopup) {
        if (parentPopup != null) {
            new ArrayList<>(parentPopup.getChildPopups()).forEach(CustomPopup::hidePopup);
            parentPopup.getChildPopups().clear(); // Clear the list of child popups from the parent
        }

        CustomPopup currentPopup = new CustomPopup(SwingUtilities.getWindowAncestor(this)); // Create a new popup window
        openPopups.add(currentPopup); // Track the popup globally to manage its visibility
        addGlobalKeyListener(currentPopup);

        // Establish a parent-child relationship if a parent popup is provided
        if (parentPopup != null) {
            parentPopup.addChildPopup(currentPopup);
        }

        // Calculate the maximum width needed for the popup based on the items' labels
        int maxWidth = 0;
        FontMetrics metrics = currentPopup.getFontMetrics(UIManager.getFont("Label.font"));

        for (String name : items.keySet()) {
            boolean isDirectory = FileScanner.isDirectory(items.get(name));
            String labelText = isDirectory ? name + " >" : name; // Append '>' for directories
            int width = metrics.stringWidth(labelText) + 40; // Include additional space for icons and padding
            maxWidth = Math.max(maxWidth, width);
        }

        // Populate the popup with items
        for (Map.Entry<String, String> entry : items.entrySet()) {
            JLabel popupMenuPathLabel = createPathLabel(entry.getKey()); // Create a label for each item

            // Configure and add the label to a panel for layout purposes
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(popupMenuPathLabel, BorderLayout.CENTER);
            panel.setMaximumSize(new Dimension(maxWidth, popupMenuPathLabel.getPreferredSize().height));
            panel.setPreferredSize(new Dimension(maxWidth, popupMenuPathLabel.getPreferredSize().height));

            // Set icons and mouse listeners for each label
            popupMenuPathLabel.setIcon(FileScanner.getIconForFileObject(entry.getValue()));
            popupMenuPathLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    popupMenuPathLabel.setBackground(UIManager.getColor("MenuBar.hoverBackground"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    popupMenuPathLabel.setBackground(UIManager.getColor("Panel.background"));
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Open a submenu for directories or perform an action for files
                    if (FileScanner.isDirectory(entry.getValue())) {
                        LinkedHashMap<String, String> subItems = FileScanner.getScannedElements(entry.getValue());
                        CustomPopup subMenu = createFolderCustomPopupMenu(subItems, currentPopup); // Recursively create
                        // a submenu
                        Point locOnScreen = popupMenuPathLabel.getLocationOnScreen();
                        subMenu.showPopup(popupMenuPathLabel, locOnScreen.x + popupMenuPathLabel.getWidth(),
                            locOnScreen.y); // Position the submenu
                    } else {
                        closeAllPopups(); // Close all popups when a file is clicked
                        openFileInEditor(entry.getValue()); // Open the file
                    }
                }
            });

            currentPopup.addItem(panel); // Add the panel to the popup
        }

        // Adjust the popup size and layout based on content
        currentPopup.pack();

        int popupWidth = currentPopup.getSize().width;
        for (Component comp : currentPopup.getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                panel.setPreferredSize(new Dimension(popupWidth, panel.getPreferredSize().height));
                panel.revalidate();
            }
        }

        return currentPopup; // Return the populated and configured popup
    }

    private void addGlobalMouseListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
            if (event instanceof MouseEvent && MouseEvent.MOUSE_CLICKED == event.getID()) {
                MouseEvent me = (MouseEvent) event;

                if (!isClickInsidePopup(me)) {
                    closeAllPopups();
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

    private void addGlobalKeyListener(CustomPopup popupMenu) {
        Action closePopupAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popupMenu.setVisible(false);
            }
        };

        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        InputMap inputMap = popupMenu.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = popupMenu.getRootPane().getActionMap();

        inputMap.put(escapeKeyStroke, "CLOSE_POPUP");
        actionMap.put("CLOSE_POPUP", closePopupAction);
    }

    private boolean isClickInsidePopup(MouseEvent me) {
        for (CustomPopup popup : openPopups) {
            if (isClickInsideComponent(me, popup)) {
                return true;
            }

            for (CustomPopup childPopup : getAllChildPopups(popup)) {
                if (isClickInsideComponent(me, childPopup)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isClickInsideComponent(MouseEvent me, Component component) {
        if (!component.isShowing()) {
            return false;
        }

        Point clickPoint = me.getLocationOnScreen();
        Point componentLocation = component.getLocationOnScreen();
        Dimension componentSize = component.getSize();
        Rectangle componentBounds = new Rectangle(componentLocation, componentSize);

        return componentBounds.contains(clickPoint);
    }

    private List<CustomPopup> getAllChildPopups(CustomPopup popup) {
        List<CustomPopup> allChildren = new ArrayList<>();
        for (CustomPopup childPopup : popup.getChildPopups()) {
            allChildren.add(childPopup);
            allChildren.addAll(getAllChildPopups(childPopup)); // Rekursive Suche nach Kindern
        }
        return allChildren;
    }

    private void closeAllPopups() {
        for (CustomPopup popup : openPopups) {
            popup.hidePopup();
        }

        openPopups.clear();
    }

    private JLabel createPathLabel(String labelName) {
        JLabel label = new JLabel(labelName);

        label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        label.setOpaque(true);

        return label;
    }

    /**
     * Updates the visibility of icons in the path labels based on user
     * preferences. Shows icons on all labels if the preference is enabled,
     * otherwise, shows icons only on the first and last labels.
     *
     * @param showIcons Specifies whether to show icons on all labels (true) or
     * only on the first and last labels (false).
     */
    @Override
    public void updateIconVisibility() {
        showAllIcons = NbPreferences.forModule(BreadcrumbExplorerManager.class).getBoolean("showIconsMacFinderLike",
            BreadcrumbExplorerManager.getOptionState());

        List<JLabel> labels = new ArrayList<>(labelPathMap.keySet());
        JLabel firstLabel = labels.isEmpty() ? null : labels.get(0);
        JLabel lastLabel = labels.isEmpty() ? null : labels.get(labels.size() - 1);

        labels.forEach(label -> {
            boolean shouldShowIcon = showAllIcons || label == firstLabel || label == lastLabel;
            label.setIcon(shouldShowIcon ? FileScanner.getIconForFileObject(labelPathMap.get(label)) : null);
        });

        revalidate();
        repaint();
    }

    private void openFileInEditor(String filePath) {
        File file = new File(filePath);
        FileObject fileObject = FileUtil.toFileObject(file);

        if (fileObject != null) {
            try {
                DataObject dataObject = DataObject.find(fileObject);

                if (dataObject != null && dataObject.getLookup() != null
                    && dataObject.getLookup().lookup(OpenCookie.class) != null) {
                    dataObject.getLookup().lookup(OpenCookie.class).open();
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static boolean isBreadcrumbExplorerVisible() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);

        return prefs.getBoolean(BreadcrumbExplorerSideBarFactory.KEY_BREADCRUMB_EXPLORER,
            BreadcrumbExplorerSideBarFactory.DEFAULT_BREADCRUMB_EXPLORER);
    }
}
