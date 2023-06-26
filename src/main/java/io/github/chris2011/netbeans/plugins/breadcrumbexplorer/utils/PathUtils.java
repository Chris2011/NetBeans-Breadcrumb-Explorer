package io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;

/**
 *
 * @author Chris
 */
public class PathUtils {

    public static String getRelativeProjectPath(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);

        if (project != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            String projectDirectoryPath = projectDirectory.getPath();
            String fileObjectPath = fileObject.getPath();

            if (fileObjectPath.startsWith(projectDirectoryPath)) {
                return fileObjectPath.substring(projectDirectoryPath.length() - projectDirectory.getName().length());
            }
        }

        return null;
    }

    public static String getAbsoluteFolderPath(String name, FileObject fileObject) {
        String escapedName = Pattern.quote(name);
        // TODO: Problem with this path: C:\Users\Chris\Documents\NetBeansProjects\HTML5Application\public_html\folder\вфывфваывшгне_minified__[01-04-2022_23-50-44]\ья ашду.min.css
        Pattern pattern = Pattern.compile(".*" + escapedName);
        Matcher matcher = pattern.matcher(fileObject.getPath());

        if (matcher.find()) {
            return matcher.group(0);
        }

        return null;
    }

    public static String[] splitPath(String pathString) {
        Path path = Paths.get(pathString);

        String separator = Utilities.isWindows() ? "\\\\" : "/";

        return path.toString().split(separator);
    }
}
