package gov.nasa.gsfc.seadas.watermask.ui;

import gov.nasa.gsfc.seadas.watermask.util.ResourceInstallationUtils;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 2/18/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
class FileInstallRunnable
        implements Runnable {
    URL sourceUrl;
    String filename;
    LandMasksData landMasksData;

    public FileInstallRunnable(URL sourceUrl, String filename, LandMasksData landMasksData) {
        this.sourceUrl = sourceUrl;
        this.filename = filename;
        this.landMasksData = landMasksData;
    }

    public void run() {
        try {
            ResourceInstallationUtils.installAuxdata(sourceUrl, filename);
            landMasksData.fireEvent(LandMasksData.FILE_INSTALLED_EVENT, null, filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
