package io.github.chris2011.netbeans.plugins.breadcrumbexplorer.utils;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

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

    public static boolean isDirectory(String path) {
        File file = new File(path);

        return file.isDirectory();
    }

    public static Icon getIconForFileObject(String path) {
        File file = new File(path);
        FileObject fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));

        try {
            if (fileObject != null) {
                DataObject dataObject = DataObject.find(fileObject);
                Node node = dataObject.getNodeDelegate();

                // Versuch, das Icon aus dem Node zu extrahieren
                Image image = node.getIcon(BeanInfo.ICON_COLOR_16x16);
                if (image != null) {
                    return new ImageIcon(image);
                }
            }
        } catch (DataObjectNotFoundException e) {
            Exceptions.printStackTrace(e);
        }

        // Fallback, wenn kein spezifisches Icon gefunden wurde
        return UIManager.getIcon("FileView.fileIcon");
    }

}
