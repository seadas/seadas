package gov.nasa.gsfc.seadas.watermask.ui;

import gov.nasa.gsfc.seadas.watermask.util.ResourceInstallationUtils;

import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 2/7/13
 * Time: 3:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileInstallationThread  extends Thread  {

    URL sourceUrl;
    String filename;

    public FileInstallationThread(URL sourceUrl, String filename) {
        this.sourceUrl = sourceUrl;
        this.filename = filename;
    }

    public void run() {
        try {
            ResourceInstallationUtils.installAuxdata(sourceUrl, filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
