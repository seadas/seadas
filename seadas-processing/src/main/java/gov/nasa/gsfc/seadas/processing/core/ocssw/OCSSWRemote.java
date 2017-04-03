package gov.nasa.gsfc.seadas.processing.core.ocssw;

import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.core.ocssw.OCSSW;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWRemote extends OCSSW {

    @Override
    public void execute(ParamList paramListl) {

    }

    @Override
    public String getOfileName(String ifileName) {
        return null;
    }

    @Override
    public String getFileType(String ifileName) {
        return null;
    }

    @Override
    public String getOcsswDataDirPath() {
        return null;
    }

    @Override
    public void setOcsswDataDirPath(String ocsswDataDirPath) {

    }

    @Override
    public String getOcsswInstallDirPath() {
        return null;
    }

    @Override
    public void setOcsswInstallDirPath(String ocsswInstallDirPath) {

    }

    @Override
    public String getOcsswScriptsDirPath() {
        return null;
    }

    @Override
    public void setOcsswScriptsDirPath(String ocsswScriptsDirPath) {

    }

    @Override
    public String getOcsswInstallerScriptPath() {
        return null;
    }

    @Override
    public void setOcsswInstallerScriptPath(String ocsswInstallerScriptPath) {

    }
}

