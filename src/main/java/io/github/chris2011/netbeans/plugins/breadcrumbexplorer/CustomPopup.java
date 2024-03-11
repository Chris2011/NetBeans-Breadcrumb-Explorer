package io.github.chris2011.netbeans.plugins.breadcrumbexplorer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * CustomPopup extends JWindow to create a custom popup window. It supports
 * hierarchical structure by maintaining a parent-child relationship between
 * popups, allowing for nested popups.
 */
public class CustomPopup extends JDialog {

    private JPanel contentPanel;
    private CustomPopup parentPopup; // Reference to the parent popup
    private final List<CustomPopup> childPopups; // List of child popups

    /**
     * Constructor for CustomPopup. Initializes the popup with a specified owner
     * window.
     *
     * @param owner The parent window for this popup.
     */
    public CustomPopup(Window owner) {
        super(owner);
        setModal(false);
        setModalityType(ModalityType.MODELESS);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);

        this.childPopups = new ArrayList<>();
        initialize();
    }

    /**
     * Initializes the content panel of the popup. Sets the layout and border
     * for the content panel.
     */
    private void initialize() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        this.setContentPane(contentPanel);
    }

    /**
     * Adds a JComponent item to the popup's content panel.
     *
     * @param item The JComponent to be added to the popup.
     */
    public void addItem(JComponent item) {
        contentPanel.add(item);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Shows the popup at a specified location relative to an invoker component.
     *
     * @param invoker The component relative to which the popup is displayed.
     * @param x The x offset from the invoker's location.
     * @param y The y offset from the invoker's location.
     */
    public void showPopup(Component invoker, int x, int y) {
        this.pack(); // Adjust size based on content
        this.setLocationRelativeTo(invoker);
        this.setLocation(x, y);
        this.setVisible(true);
    }

    /**
     * Hides the popup and recursively hides all child popups.
     */
    public void hidePopup() {
        for (CustomPopup child : new ArrayList<>(childPopups)) {
            child.hidePopup();
        }
        this.setVisible(false);
        if (parentPopup != null) {
            parentPopup.removeChildPopup(this);
        }
    }

    /**
     * Sets the parent popup of this popup.
     *
     * @param parentPopup The parent popup.
     */
    public void setParentPopup(CustomPopup parentPopup) {
        this.parentPopup = parentPopup;
    }

    /**
     * Returns the parent popup of this popup.
     *
     * @return The parent popup.
     */
    public CustomPopup getParentPopup() {
        return parentPopup;
    }

    /**
     * Returns the list of child popups of this popup.
     *
     * @return The list of child popups.
     */
    public List<CustomPopup> getChildPopups() {
        return childPopups;
    }

    /**
     * Adds a child popup to this popup.
     *
     * @param childPopup The child popup to be added.
     */
    public void addChildPopup(CustomPopup childPopup) {
        childPopups.add(childPopup);
        childPopup.setParentPopup(this);
    }

    /**
     * Removes a child popup from this popup.
     *
     * @param childPopup The child popup to be removed.
     */
    public void removeChildPopup(CustomPopup childPopup) {
        childPopups.remove(childPopup);
    }
}
