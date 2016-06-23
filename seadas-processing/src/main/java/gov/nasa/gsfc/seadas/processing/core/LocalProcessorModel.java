package gov.nasa.gsfc.seadas.processing.core;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.processing.general.SeadasFileUtils;
import gov.nasa.gsfc.seadas.processing.general.SeadasLogger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created by aabduraz on 6/8/16.
 */
public class LocalProcessorModel extends ProcessorModel {

    private String[] cmdArray;
    private boolean acceptsParFile;
    private String parFileOptionName;

    public LocalProcessorModel(String programName) {
        super(programName);
    }
    public LocalProcessorModel(String programName, String parXMLFileName) {
        super(programName, parXMLFileName);
        acceptsParFile = false;
        parFileOptionName = ParamUtils.DEFAULT_PAR_FILE_NAME;
    }

    public LocalProcessorModel(String programName, ArrayList<ParamInfo> paramList) {
        super(programName, paramList);
        acceptsParFile = ParamUtils.getOptionStatus(parXMLFileName, "hasParFile");
        parFileOptionName = ParamUtils.getParFileOptionName(parXMLFileName);
        progressPattern = Pattern.compile(ParamUtils.getProgressRegex(parXMLFileName));
    }

    public void setAcceptsParFile(boolean acceptsParFile) {
        this.acceptsParFile = acceptsParFile;
    }

    public boolean acceptsParFile() {
        return acceptsParFile;
    }

    private String getParFileCommandLineOption() {
        File parFile = computeParFile();
        String parFileName = parFile.getAbsolutePath();

        //check dir name for remote execution
        if (!RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY).equals(OCSSW.SEADAS_OCSSW_LOCATION_LOCAL)) {
            parFileName = parFileName.replace(OCSSW.getOCSSWClientSharedDirName(), OCSSW.getServerSharedDirName());
        }

        if (parFileOptionName.equals("none")) {
            return parFileName;
        } else {
            return parFileOptionName + "=" + parFileName;
        }
    }

    private String[] getCmdArrayWithParFile() {
        final String[] cmdArray = concat(getCmdArrayPrefix(), new String[]{getParFileCommandLineOption()});
        for (int i = 0; i < cmdArray.length; i++) {
            SeadasLogger.getLogger().info("i = " + i + " " + cmdArray[i]);
        }
        return cmdArray;
    }

    public String getCmdArrayString() {
        Iterator itr = getRemoteServerCmdArray().iterator();
        StringBuilder cmdArray = new StringBuilder();
        String tmpString;
        while (itr.hasNext()) {
            tmpString = (String) itr.next();
            if (tmpString.indexOf(File.separator) != -1) {
                tmpString = tmpString.replaceAll(tmpString.substring(tmpString.indexOf(File.separator), tmpString.lastIndexOf(File.separator) + 1), "");
            }
            cmdArray.append(tmpString + ";");
        }

        return cmdArray.toString();
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private String[] getCmdArrayPrefix() {
        String[] cmdArrayPrefix;

        if (programName.equals(OCSSW.OCSSW_INSTALLER)) {
            cmdArrayPrefix = new String[1];
            cmdArrayPrefix[0] = getProgramName();
            if (!OCSSW.isOCSSWExist()) {
                cmdArrayPrefix[0] = OCSSW.TMP_OCSSW_INSTALLER;
            } else {
                cmdArrayPrefix[0] = OCSSW.getOcsswEnv() + "/run/scripts/install_ocssw.py";
            }
        } else {
            cmdArrayPrefix = new String[4];
            cmdArrayPrefix[0] = OCSSW.getOcsswScriptPath();
            cmdArrayPrefix[1] = "--ocsswroot";
            cmdArrayPrefix[2] = OCSSW.getOcsswEnv();
            cmdArrayPrefix[3] = getProgramName();
        }
        return cmdArrayPrefix;
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
            if ((optionType.equals(ParamInfo.Type.IFILE) || optionType.equals(ParamInfo.Type.OFILE))
                    && optionValue != null && optionValue.trim().length() > 0) {
            }
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
            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + "=" + option.getValue());
        }
        return cmdArrayParam;
    }

    private String[] getCmdArrayWithArguments() {
        String[] cmdArray = concat(getCmdArrayPrefix(), getCmdArrayParam());

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

    /**
     * this method returns a command array for execution.
     * the array is constructed using the paramList data and input/output files.
     * the command array structure is: full pathname of the program to be executed, input file name, params in the required order and finally the output file name.
     * assumption: order starts with 1
     *
     * @return
     */
    public String[] getProgramCmdArray() {

        if (acceptsParFile) { // && RuntimeContext.getConfig().getContextProperty(OCSSW.OCSSW_LOCATION_PROPERTY).equals(OCSSW.SEADAS_OCSSW_LOCATION_LOCAL)) {
            cmdArray = getCmdArrayWithParFile();

        } else {
            cmdArray = getCmdArrayWithArguments();
        }

        return cmdArray;
    }
    private File computeParFile() {

        try {
            final File tempFile = File.createTempFile("tmpParFile", ".par", getIFileDir());
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
            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + " = " + option.getValue() + "option value is valid :" + (new Boolean(option.getValue().length() > 0)));
            SeadasLogger.getLogger().info(option.getName() + " = " + option.getValue() + "option type is :" + option.getType() + " " + option.getType().equals(ParamInfo.Type.HELP));

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
