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

        return fileObject.getPath().substring((project.getProjectDirectory().getParent().getPath() + File.separator).length());
    }

    public static List<String> getAbsoluteFolderPath(List<String> nameList, FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        List<String> folders = new ArrayList<>();

        if (project == null) {
            folders.add(fileObject.getPath());

            return folders;
        }

        StringBuilder absolutePath = new StringBuilder(project.getProjectDirectory().getParent().getPath());

        for (String folder : nameList) {
            absolutePath.append(File.separator).append(folder);
            folders.add(absolutePath.toString());
        }

        return folders;
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
