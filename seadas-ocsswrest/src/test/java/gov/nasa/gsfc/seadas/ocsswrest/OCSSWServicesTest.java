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
    public void getOCSSWInstallDir() throws Exception {


    }

    @Test
    public void getOcsswInstallStatus() throws Exception {

    }

    @Test
    public void setOCSSWProgramName() throws Exception {

    }

    @Test
    public void getMissionDataStatus() throws Exception {

    }

    @Test
    public void getMissionSuites() throws Exception {
        String missionName = "MODIS Aqua";
        System.out.println("missionName = " + missionName);
        OCSSWRemoteImpl ocsswRemote = new OCSSWRemoteImpl();
        JsonObject fileContents = ocsswRemote.getSensorFileIntoArrayList(missionName);
        System.out.print(fileContents.keySet());
    }

}