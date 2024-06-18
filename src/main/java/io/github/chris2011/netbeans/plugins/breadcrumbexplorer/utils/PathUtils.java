package io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

        if (project == null) {
            return fileObject.getPath();
        }

        return fileObject.getPath()
            .substring((project.getProjectDirectory().getParent().getPath() + File.separator).length());
    }

    public static List<String> getAbsoluteFolderPath(List<String> nameList, FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        List<String> folders = new ArrayList<>();

        StringBuilder absolutePath = new StringBuilder("");

        if (project == null) {
            for (int i = 0; i < nameList.size(); i++) {
                absolutePath.append(nameList.get(i));

                if (i < nameList.size() - 1) {
                    absolutePath.append(File.separator);
                }

                folders.add(absolutePath.toString());
            }

            return folders;
        }

        absolutePath = new StringBuilder(project.getProjectDirectory().getParent().getPath());

        for (String folder : nameList) {
            absolutePath.append(File.separator).append(folder);
            folders.add(absolutePath.toString());
        }

        return folders;
    }

    public static String getRelativeFolderPath(String absolutePath, FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);

        if (project == null) {
            // Remove the drive letter if present (Windows)
            if (absolutePath.length() > 2 && absolutePath.charAt(1) == ':') {
                absolutePath = absolutePath.substring(2);
            }

            // Ensure the path starts with a single '/'
            if (!absolutePath.startsWith("/")) {
                absolutePath = "/" + absolutePath;
            }

            return absolutePath;
        }

        return File.separator + absolutePath.substring((project.getProjectDirectory().getPath()).length());
    }

    public static List<String> splitPath(String pathString) {
        if (pathString == null) {
            return Arrays.asList("".split(""));
        }

        Path path = Paths.get(pathString);
        String separator = Utilities.isWindows() ? "\\\\" : "/";

        return Arrays.asList(path.toString().split(separator));
    }
}
