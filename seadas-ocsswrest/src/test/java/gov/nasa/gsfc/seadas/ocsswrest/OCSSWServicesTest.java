package gov.nasa.gsfc.seadas.ocsswrest;

import gov.nasa.gsfc.seadas.ocsswrest.ocsswmodel.OCSSWRemoteImpl;
import org.junit.Test;

import javax.json.JsonObject;

import static org.junit.Assert.*;

/**
 * Created by amhmd on 4/26/17.
 */
public class OCSSWServicesTest {
    @Test
    public void getOCSSWInstallDir() {


    }

    @Test
    public void getOcsswInstallStatus() {

    }

    @Test
    public void setOCSSWProgramName() {

    }

    @Test
    public void getMissionDataStatus() {

    }

    @Test
    public void getMissionSuites() {
        String missionName = "MODIS Aqua";
        System.out.println("missionName = " + missionName);
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        JsonObject fileContents = ocsswRemote.getSensorFileIntoArrayList(missionName);
        System.out.print(fileContents.keySet());
    }

}