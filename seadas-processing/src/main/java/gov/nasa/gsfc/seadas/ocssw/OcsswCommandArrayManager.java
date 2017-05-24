package gov.nasa.gsfc.seadas.ocssw;

import gov.nasa.gsfc.seadas.processing.core.ParamList;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import java.io.File;

/**
 * Created by aabduraz on 6/12/16.
 */
public abstract class OcsswCommandArrayManager {

    ProcessorModel processorModel;
    public String[] cmdArray;
    protected ParamList paramList;
    private File ifileDir;

    OcsswCommandArrayManager(ProcessorModel processorModel) {
        this.processorModel = processorModel;
        paramList = processorModel.getParamList();
        ifileDir = processorModel.getIFileDir();
    }

    public abstract String[] getProgramCommandArray();

    public File getIfileDir() {
        return ifileDir;
    }

    public void setIfileDir(File ifileDir) {
        this.ifileDir = ifileDir;
    }
}
