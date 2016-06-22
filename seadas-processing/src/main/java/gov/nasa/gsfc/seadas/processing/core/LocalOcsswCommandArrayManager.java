package gov.nasa.gsfc.seadas.processing.core;

import gov.nasa.gsfc.seadas.processing.general.SeadasLogger;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by aabduraz on 6/12/16.
 */
public class LocalOcsswCommandArrayManager extends OcsswCommandArrayManager {

    private String parFileOptionName;

    LocalOcsswCommandArrayManager(ProcessorModel processorModel) {
        super(processorModel);
        parFileOptionName = processorModel.getParFileOptionName();
    }

    /**
     * this method returns a command array for execution.
     * the array is constructed using the paramList data and input/output files.
     * the command array structure is: full pathname of the program to be executed, input file name, params in the required order and finally the output file name.
     * assumption: order starts with 1
     *
     * @return
     */
    public String[] getProgramCommandArray() {

        if (processorModel.acceptsParFile()) {
            cmdArray = getCmdArrayWithParFile();

        } else {
            cmdArray = getCmdArrayWithArguments();
        }

        return cmdArray;
    }


    private String[] getCmdArrayWithParFile() {
        final String[] cmdArray = concat(processorModel.getCmdArrayPrefix(), new String[]{getParFileCommandLineOption()});
        for (int i = 0; i < cmdArray.length; i++) {
            SeadasLogger.getLogger().info("i = " + i + " " + cmdArray[i]);
        }
        return cmdArray;
    }

    private String getParFileCommandLineOption() {
        File parFile = computeParFile();
        String parFileName = parFile.getAbsolutePath();

        if (parFileOptionName.equals("none")) {
            return parFileName;
        } else {
            return parFileOptionName + "=" + parFileName;
        }
    }

    private String[] getCmdArrayParam() {

        String[] cmdArrayParam = new String[paramList.getParamArray().size()];

        Iterator itr = paramList.getParamArray().iterator();

        ParamInfo option;
        int optionOrder;
        String optionValue;
        ParamInfo.Type optionType;

        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            optionOrder = option.getOrder();
            optionValue = option.getValue();
            optionType = option.getType();
            if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
                if (option.getValue() != null && option.getValue().length() > 0) {
                    cmdArrayParam[optionOrder] = optionValue;
                }
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
                cmdArrayParam[optionOrder] = option.getName() + "=" + optionValue;
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
                if (option.getName() != null && option.getName().length() > 0) {
                    cmdArrayParam[optionOrder] = option.getName();
                }
            }
        }
        return cmdArrayParam;
    }

    private String[] getCmdArrayWithArguments() {
        String[] cmdArray = concat(processorModel.getCmdArrayPrefix(), getCmdArrayParam());

        // get rid of the null strings
        ArrayList<String> cmdList = new ArrayList<String>();
        for (String s : cmdArray) {
            if (s != null) {
                cmdList.add(s);
            }
        }
        cmdArray = cmdList.toArray(new String[cmdList.size()]);
        return cmdArray;
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
                    if (!OCSSW.isOCSSWInstalledLocal()) {
                        if (option.getType().equals(ParamInfo.Type.OFILE) || option.getType().equals(ParamInfo.Type.IFILE)) {
                            optionValue = optionValue.replace(OCSSW.getOCSSWClientSharedDirName(), OCSSW.getServerSharedDirName());
                        }
                    }
                    parString.append(option.getName() + "=" + optionValue + "\n");
                }
            }

        }
        return paramList.getParamString("\n");
    }
}
