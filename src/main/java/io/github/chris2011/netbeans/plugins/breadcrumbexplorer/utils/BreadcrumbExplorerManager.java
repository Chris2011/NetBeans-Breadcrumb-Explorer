package io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 *
 * @author clenz
 */
public class BreadcrumbExplorerManager {
    private static final List<BreadcrumbExplorerObserver> observers = new ArrayList<>();
    private static boolean showIconsMacFinderLike = loadInitialShowIconsSetting();

    public static void addObserver(BreadcrumbExplorerObserver observer) {
        observers.add(observer);
    }

    public static void removeObserver(BreadcrumbExplorerObserver observer) {
        observers.remove(observer);
    }

    public static void setShowIconsMacFinderLike(boolean showIcons) {
        showIconsMacFinderLike = showIcons;
        notifyAllObservers();
    }
    
    public static boolean getOptionState() {
        return showIconsMacFinderLike;
    }

    private static void notifyAllObservers() {
        for (BreadcrumbExplorerObserver observer : observers) {
            observer.updateIconVisibility();
        }
    }

    private static boolean loadInitialShowIconsSetting() {
        return Preferences.userNodeForPackage(BreadcrumbExplorerManager.class).getBoolean("showIconsMacFinderLike", false);
    }
}
