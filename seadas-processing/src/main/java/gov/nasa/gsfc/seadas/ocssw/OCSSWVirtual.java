package gov.nasa.gsfc.seadas.ocssw;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

/**
 * Created by aabduraz on 3/27/17.
 */
public class OCSSWVirtual extends OCSSW {
    @Override
    public boolean isOCSSWExist() {
        return false;
    }

    @Override
    public String getOcsswRunnerScriptPath() {
        return null;
    }

    @Override
    public Process execute(ProcessorModel processorModel) {
        return null;
    }

    @Override
    public Process execute(ParamList paramListl) {
        return null;
    }

    @Override
    public Process execute(String[] commandArray) {
        return null;
    }

    @Override
    public Process execute(String programName, String[] commandArrayParams) {
        return null;
    }

    @Override
    public String getOfileName(String ifileName) {
        return null;
    }

    @Override
    public String getOfileName(String ifileName, String[] options) {
        return null;
    }

    @Override
    public String getOfileName(String ifileName, String programName, String suiteValue) {
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
    public  String getOcsswInstallDirPath() {
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
    public boolean isMissionDirExist(String missionName) {
        return false;
    }

    @Override
    public String[] getMissionSuites(String missionName, String programName) {
        return new String[0];
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

    @Override
    public void setCommandArrayPrefix() {

    }

    @Override
    public void setCommandArraySuffix() {

    }
}
