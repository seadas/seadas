package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.*;
import gov.nasa.gsfc.seadas.processing.utilities.SeadasArrayUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by aabduraz on 7/25/16.
 */
public class ParFileManager{
    private String parFileOptionName;

    ProcessorModel processorModel;
    protected ParamList paramList;

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
            final File tempFile = File.createTempFile("tmpParFile", ".par", processorModel.getIFileDir());
            tempFile.deleteOnExit();
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
            SeadasLogger.getLogger().warning("parfile is not created. " + e.getMessage());
            return null;
        }
    }

    public String getParString() {

        StringBuilder parString = new StringBuilder("");
        Iterator itr = paramList.getParamArray().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            String optionValue = option.getValue();
            if (!option.getType().equals(ParamInfo.Type.HELP) && optionValue.length() > 0) {
                if (!option.getDefaultValue().equals(optionValue)) {
                    parString.append(option.getName() + "=" + optionValue + "\n");
                }
            }

        }
        return paramList.getParamString("\n");
    }
}
