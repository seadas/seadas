package gov.nasa.gsfc.seadas.colorbar.ui;

import org.esa.beam.util.SystemUtils;
import org.esa.beam.visat.VisatApp;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 7/14/14
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColorBarImportDialog { //extends ModalDialog {
    private String lastDirKey;

    JFileChooser fileChooser;

    public File promptForFile() {
        if (lastDirKey == null) {
            lastDirKey = "user." + "colorbar" + ".import.dir";
        }

        File currentDir = null;
        VisatApp visatApp = VisatApp.getApp();
        String currentDirPath = visatApp.getPreferences().getPropertyString(lastDirKey,
                SystemUtils.getUserHomeDir().getPath());
        if (currentDirPath != null) {
            currentDir = new File(currentDirPath);
        }
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }
        fileChooser.setCurrentDirectory(currentDir);

        File file = null;
        boolean canceled = false;
        while (file == null && !canceled) {
            int result = fileChooser.showOpenDialog(visatApp.getMainFrame());
            file = fileChooser.getSelectedFile();
            if (file != null && file.getParent() != null) {
                visatApp.getPreferences().setPropertyString(lastDirKey, file.getParent());
            }
            if (result == JFileChooser.APPROVE_OPTION) {
                if (file != null && !file.getName().trim().equals("")) {
                    if (!file.exists()) {
                        visatApp.showErrorDialog("File not found:\n" + file.getPath());
                        file = null;
                    } else {

                        double fileSize = file.length() / (1024.0 * 1024.0);
                        if (fileSize == 0.0) {
                            visatApp.showErrorDialog("File is empty:\n" + file.getPath());
                            file = null;
                        } else if (!isFileOfFormatImage(file)) {
                            visatApp.showInfoDialog(
                                    "The selected file\n"
                                            + "'" + file.getPath() + "'\n"
                                            + "appears not to be an image file.\n\n"
                                            + "Please use 'Open' in the file menu to open an image file.\n"
                                    , null);
                            file = null;
                        }
                    }
                }
            } else {
                canceled = true;
            }
        }

        return canceled ? null : file;
    }

    private boolean isFileOfFormatImage(File file) {
        boolean isImage = false;
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(file);

            if (ImageIO.read(iis) != null) {
                System.out.println("It's an image");
                isImage = true;
            }
        } catch (IOException ioe) {
             System.out.println("IOException for reading in color bar file!");
        }
        return isImage;
    }

}
