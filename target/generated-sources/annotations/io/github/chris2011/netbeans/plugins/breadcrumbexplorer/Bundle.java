package io.github.chris2011.netbeans.plugins.breadcrumbexplorer;
/** Localizable strings for {@link io.github.chris2011.netbeans.plugins.breadcrumbexplorer}. */
class Bundle {
    /**
     * @return <i>Show P&amp;ath Breadcrumbs</i>
     * @see MenuAction
     */
    static String BreadcrumbExplorer_lblShowPathBreadcrumbs() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "BreadcrumbExplorer.lblShowPathBreadcrumbs");
    }
    /**
     * @return <i>Show breadcrumb explorer</i>
     * @see ShowBreadcrumbExplorer
     */
    static String CTL_ShowBreadcrumbExplorer() {
        return org.openide.util.NbBundle.getMessage(Bundle.class, "CTL_ShowBreadcrumbExplorer");
    }
    private Bundle() {}
}
