package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import org.esa.beam.framework.ui.AppContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 1/20/15
 * Time: 12:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class OCSSWInstallerFormLocal extends OCSSWInstallerForm {

    public OCSSWInstallerFormLocal(AppContext appContext, String programName, String xmlFileName, OCSSW ocssw) {
        super(appContext, programName, xmlFileName, ocssw);

    }

    void updateMissionStatus() {
        missionDataStatus = new HashMap<String, Boolean>();
        missionDataStatus.put("SEAWIFS", new File(missionDataDir + "seawifs").exists());
        missionDataStatus.put("AQUA", new File(missionDataDir + "hmodisa").exists());
        missionDataStatus.put("TERRA", new File(missionDataDir + "hmodist").exists());
        missionDataStatus.put("VIIRSN", new File(missionDataDir + "viirsn").exists());
        missionDataStatus.put("MERIS", new File(missionDataDir + "meris").exists());
        missionDataStatus.put("CZCS", new File(missionDataDir + "czcs").exists());
        missionDataStatus.put("AQUARIUS", new File(missionDataDir + "aquarius").exists());
        missionDataStatus.put("OCTS", new File(missionDataDir + "octs").exists());
        missionDataStatus.put("OLI", new File(missionDataDir + "oli").exists());
        missionDataStatus.put("OSMI", new File(missionDataDir + "osmi").exists());
        missionDataStatus.put("MOS", new File(missionDataDir + "mos").exists());
        missionDataStatus.put("MSI", new File(missionDataDir + "msi").exists());
        missionDataStatus.put("OCM2", new File(missionDataDir + "ocm2").exists());
        missionDataStatus.put("OCM1", new File(missionDataDir + "ocm1").exists());
        missionDataStatus.put("AVHRR", new File(missionDataDir + "avhrr").exists());
        missionDataStatus.put("HICO", new File(missionDataDir + "hico").exists());
        missionDataStatus.put("GOCI", new File(missionDataDir + "goci").exists());
    }

    void init(){
        updateMissionStatus();
    }

    void updateMissionValues() {

        for (Map.Entry<String, Boolean> entry : missionDataStatus.entrySet()) {
            String missionName = entry.getKey();
            Boolean missionStatus = entry.getValue();

            if (missionStatus) {
                processorModel.setParamValue("--" + missionName.toLowerCase(), "1");
            } else {
                processorModel.setParamValue("--" + missionName.toLowerCase(), "0");
            }

        }
        if (new File(missionDataDir + "eval").exists()) {
            processorModel.setParamValue("--eval", "1");
        }
        if (new File(OCSSWInfo.getInstance().getOcsswRoot(), "build").exists()) {
            processorModel.setParamValue("--src", "1");
        }
    }
}
