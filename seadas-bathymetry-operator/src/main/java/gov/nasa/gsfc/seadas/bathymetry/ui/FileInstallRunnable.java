package gov.nasa.gsfc.seadas.bathymetry.ui;

import gov.nasa.gsfc.seadas.bathymetry.operator.WatermaskClassifier;
import gov.nasa.gsfc.seadas.bathymetry.util.ResourceInstallationUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 2/18/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
class FileInstallRunnable implements Runnable {
    URL sourceUrl;
    BathymetryData bathymetryData;
    SourceFileInfo sourceFileInfo;
    String filename;
    boolean valid = true;

    public FileInstallRunnable(URL sourceUrl, String filename, SourceFileInfo sourceFileInfo, BathymetryData bathymetryData) {
        if (sourceUrl== null || filename == null ||  sourceFileInfo == null  || bathymetryData == null) {
            valid = false;
            return;
        }

        this.sourceUrl = sourceUrl;
        this.sourceFileInfo = sourceFileInfo;
        this.bathymetryData = bathymetryData;
        this.filename = filename;
    }

    public void run() {
        if (!valid) {
            return;
        }

    //    final String filename = sourceFileInfo.getFile().getName().toString();

        try {
            ResourceInstallationUtils.installAuxdata(sourceUrl, filename);

            if (sourceFileInfo.getMode() == WatermaskClassifier.Mode.SRTM_GC) {
                File gcFile = ResourceInstallationUtils.getTargetFile(WatermaskClassifier.GC_WATER_MASK_FILE);

                if (!gcFile.exists()) {
                    final URL northSourceUrl = new URL(BathymetryData.LANDMASK_URL + "/" + gcFile.getName());

                    ResourceInstallationUtils.installAuxdata(northSourceUrl, gcFile.getName());
                }
            }

        } catch (IOException e) {
            sourceFileInfo.setStatus(false, e.getMessage());
        }


        bathymetryData.fireEvent(BathymetryData.NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT, null, sourceFileInfo);
    }
}
