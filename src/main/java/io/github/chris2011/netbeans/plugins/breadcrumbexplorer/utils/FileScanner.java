package io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Chris
 */
public class FileScanner {
    public static LinkedHashMap<String, String> getScannedElements(String folderPath) {
        LinkedHashMap<String, String> folderMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> fileMap = new LinkedHashMap<>();

        Path path = Paths.get(folderPath);

        if (!Files.exists(path)) {
            System.out.println("Ungültiger Ordnerpfad: " + folderPath);
            return folderMap;
        }

        File[] files = path.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String pathName = file.getAbsolutePath();

                if (file.isDirectory()) {
                    folderMap.put(name, pathName);
                } else {
                    fileMap.put(name, pathName);
                }
            }
        }

        LinkedHashMap<String, String> mergedMap = new LinkedHashMap<>();
        mergedMap.putAll(folderMap);
        mergedMap.putAll(fileMap);

        return mergedMap;
    }

    public static Icon getFileIcon(String filePath) {
        File file = new File(filePath);
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();

        return fileSystemView.getSystemIcon(file);
    }
}
