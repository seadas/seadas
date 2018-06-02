package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.processing.core.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;    

/**
 * Created by aabduraz on 7/25/16.
 */
public class ParFileManager{
    private String parFileOptionName;

    ProcessorModel processorModel;
    protected ParamList paramList;

    private String parFileLocation;

    public ParFileManager(ProcessorModel processorModel) {
        this.processorModel = processorModel;
        paramList = processorModel.getParamList();
        parFileOptionName = processorModel.getParFileOptionName();

    }

    public String[] getCmdArrayWithParFile() {
        String parString;

        File parFile = computeParFile();
        String parFileName = parFile.getAbsolutePath();

        if (parFileOptionName.equals("none")) {
            parString =  parFileName;
        } else {
            parString =  parFileOptionName + "=" + parFileName;
        }
        return new String[]{parString};
    }



    private File computeParFile() {

        try {

            final File tempFile = File.createTempFile(processorModel.getProgramName() + "-tmpParFile", ".par", processorModel.getIFileDir());
            parFileLocation = tempFile.getAbsolutePath();
                    System.out.println(tempFile.getAbsoluteFile());
            //tempFile.deleteOnExit();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(tempFile);
                String parString = getParString();
                fileWriter.write(parString + "\n");
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
            return tempFile;

        } catch (IOException e) {
            Logger.getGlobal().warning("parfile is not created. " + e.getMessage());
            return null;
        }
    }

    public String getParString() {
        return paramList.getParamString("\n");
    }

    public String getParFileOptionName() {
        return parFileOptionName;
    }

    public void setParFileOptionName(String parFileOptionName) {
        this.parFileOptionName = parFileOptionName;
    }

}
