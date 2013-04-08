package gov.nasa.gsfc.seadas.processing.core;

import gov.nasa.gsfc.seadas.processing.general.*;
import org.esa.beam.visat.VisatApp;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/16/12
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessorModel implements L2genDataProcessorModel, Cloneable {
    private String programName;

    private ParamList paramList;
    private boolean acceptsParFile;
    private boolean hasGeoFile;

    private Set<String> primaryOptions;
    private String parFileOptionName;

    private boolean readyToRun;
    private final String runButtonPropertyName = "RUN_BUTTON_STATUS_CHANGED";
    private final String allparamInitializedPropertyName = "ALL_PARAMS_INITIALIZED";
    private final int NUMBER_OF_PREFIX_ELEMENTS = 4;
    private ProcessorModel secondaryProcessor;
    private Pattern progressPattern;

    private ProcessorTypeInfo.ProcessorID processorID;

    private boolean multipleInputFiles;
    private ArrayList<String> filesToUpload;
    private ArrayList<String> filesToDownload;
    private ArrayList<String> finalCmdArray;

    private boolean openInSeadas;

    public ProcessorModel(String name) {
        acceptsParFile = false;
        hasGeoFile = false;
        readyToRun = false;
        multipleInputFiles = false;
        paramList = new ParamList();
        parFileOptionName = ParamUtils.DEFAULT_PAR_FILE_NAME;

        programName = name;
        processorID = ProcessorTypeInfo.getProcessorID(programName);

        primaryOptions = new HashSet<String>();
        primaryOptions.add("ifile");
        primaryOptions.add("ofile");

        progressPattern = Pattern.compile(ParamUtils.DEFAULT_PROGRESS_REGEX);
        setOpenInSeadas(false);
    }

    public ProcessorModel(String name, String parXMLFileName) {
        this(name);
        if (parXMLFileName != null && parXMLFileName.length() > 0) {
            setParamList(ParamUtils.computeParamList(parXMLFileName));
            acceptsParFile = ParamUtils.getOptionStatus(parXMLFileName, "hasParFile");
            parFileOptionName = ParamUtils.getParFileOptionName(parXMLFileName);
            progressPattern = Pattern.compile(ParamUtils.getProgressRegex(parXMLFileName));
            hasGeoFile = ParamUtils.getOptionStatus(parXMLFileName, "hasGeoFile");
            setPrimaryOptions(ParamUtils.getPrimaryOptions(parXMLFileName));
            setOpenInSeadas(false);
        }
    }

    public ProcessorModel(String name, ArrayList<ParamInfo> paramList) {
        this(name);
        setParamList(paramList);
    }

    public static ProcessorModel valueOf(String programName, String xmlFileName) {
        ProcessorTypeInfo.ProcessorID processorID = ProcessorTypeInfo.getProcessorID(programName);
        switch (processorID) {
            case EXTRACTOR:
                return new Extractor_Processor(programName, xmlFileName);
            case MODIS_L1B_PY:
                return new Modis_L1B_Processor(programName, xmlFileName);
            case LONLAT2PIXLINE:
                return new LonLat2Pixels_Processor(programName, xmlFileName);
            case SMIGEN:
                return new SMIGEN_Processor(programName, xmlFileName);
            case L2BIN:
                return new L2Bin_Processor(programName, xmlFileName);
            case L2BIN_AQUARIUS:
                return new L2Bin_Processor(programName, xmlFileName);
            case L3BIN:
                return new L3Bin_Processor(programName, xmlFileName);
            case OCSSW_INSTALLER:
                return new OCSSWInstaller_Processor(programName, xmlFileName);
            default:
        }
        return new ProcessorModel(programName, xmlFileName);
    }

    public void addParamInfo(ParamInfo info) {
        paramList.addInfo(info);
    }

    public void removeParamInfo(ParamInfo paramInfo) {
        paramList.removeInfo(paramInfo.getName());
    }

    public boolean isReadyToRun() {
        return readyToRun;
    }

    public void setReadyToRun(boolean readyToRun) {
        boolean oldValue = this.readyToRun;
        this.readyToRun = readyToRun;
        fireEvent(getRunButtonPropertyName(), oldValue, readyToRun);
//        if (processorID == ProcessorTypeInfo.ProcessorID.L2BIN
//                || processorID == ProcessorTypeInfo.ProcessorID.L3BIN ) {
//
//        }
    }

    public String getOfileName() {
        return getParamValue(getPrimaryOutputFileOptionName());
    }

    public boolean isMultipleInputFiles() {
        return multipleInputFiles;
    }

    public void setMultipleInputFiles(boolean multipleInputFiles) {
        this.multipleInputFiles = multipleInputFiles;
    }

    public void createsmitoppmProcessorModel(String ofileName) {
        ProcessorModel smitoppm = new ProcessorModel("smitoppm_4_ui");
        smitoppm.setAcceptsParFile(false);
        ParamInfo pi1 = new ParamInfo("ifile", getParamValue(getPrimaryOutputFileOptionName()));
        pi1.setOrder(1);
        ParamInfo pi2 = new ParamInfo("ofile", ofileName);
        pi2.setOrder(2);
        smitoppm.addParamInfo(pi1);
        smitoppm.addParamInfo(pi2);
        setSecondaryProcessor(smitoppm);

    }

    public void addParamInfo(String name, String value, ParamInfo.Type type) {
        ParamInfo info = new ParamInfo(name, value, type);
        addParamInfo(info);
    }


    public void addParamInfo(String name, String value, ParamInfo.Type type, int order) {
        ParamInfo info = new ParamInfo(name, value, type);
        info.setOrder(order);
        addParamInfo(info);
    }

    public String getPrimaryInputFileOptionName() {
        for (String name : primaryOptions) {
            ParamInfo param = paramList.getInfo(name);
            if ((param != null) &&
                    (param.getType() == ParamInfo.Type.IFILE) &&
                    (!param.getName().toLowerCase().contains("geo"))) {
                return name;
            }
        }
        return null;
    }

    public String getPrimaryOutputFileOptionName() {
        for (String name : primaryOptions) {
            ParamInfo param = paramList.getInfo(name);
            if ((param != null) && (param.getType() == ParamInfo.Type.OFILE)) {
                return name;
            }
        }
        return null;
    }

    public boolean hasGeoFile() {
        return hasGeoFile;
    }

    public void setHasGeoFile(boolean hasGeoFile) {
        boolean oldValue = this.hasGeoFile;
        this.hasGeoFile = hasGeoFile;
        paramList.getPropertyChangeSupport().firePropertyChange("geofile", oldValue, hasGeoFile);
    }

    public boolean isValidProcessor() {
        SeadasLogger.getLogger().info("program location: " + OCSSW.getOcsswScriptPath());
        return OCSSW.getOcsswScriptPath() != null;
    }

    public String getProgramName() {
        return programName;
    }

    public ArrayList<ParamInfo> getProgramParamList() {
        return paramList.getParamArray();
    }

    public boolean hasPrimaryOutputFile() {
        String name = getPrimaryOutputFileOptionName();
        if (name == null) {
            return false;
        } else {
            return true;
        }
    }

    public void setAcceptsParFile(boolean acceptsParFile) {
        this.acceptsParFile = acceptsParFile;
    }

    public boolean acceptsParFile() {
        return acceptsParFile;
    }

    public void updateParamInfo(ParamInfo currentOption, String newValue) {
        paramList.setValue(currentOption.getName(), newValue);
        checkCompleteness();
    }

    protected void checkCompleteness() {
        boolean complete = true;

        for (ParamInfo param : paramList.getParamArray()) {
            if (param.getValue() == null || param.getValue().trim().length() == 0) {
                complete = false;
                break;
            }
        }

        if (complete) {
            fireEvent(getAllparamInitializedPropertyName(), false, true);
        }
    }

    public ParamInfo getParamInfo(String paramName) {
        return paramList.getInfo(paramName);
    }

    public boolean isAllParamsValid() {
        for (ParamInfo param : paramList.getParamArray()) {
            if (param.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    public String getParamValue(String paramName) {
        ParamInfo option = getParamInfo(paramName);
        if (option != null) {
            return option.getValue();
        }
        return null;
    }

    public void updateParamInfo(String paramName, String newValue) {
        ParamInfo option = getParamInfo(paramName);
        if (option != null) {
            String oldValue = option.getValue();
            option.setValue(newValue);
            checkCompleteness();
            if (!oldValue.equals(newValue)) {
                getPropertyChangeSupport().firePropertyChange(option.getName(), oldValue, newValue);
            }
        }
    }

    private void updateGeoFileStatus(String ifileName) {

        if ((processorID == ProcessorTypeInfo.ProcessorID.L1BRSGEN
                || processorID == ProcessorTypeInfo.ProcessorID.L1MAPGEN
                || processorID == ProcessorTypeInfo.ProcessorID.MODIS_L1B_PY) && (new FileInfo(ifileName)).getMissionName().indexOf("MODIS") != -1) {
            setHasGeoFile(true);

        } else {
            setHasGeoFile(false);
        }
    }

    public boolean updateIFileInfo(String ifileName) {

        if (verifyIFilePath(ifileName)) {
            String ofileName = SeadasFileUtils.findNextLevelFileName(ifileName, programName);
            if (ofileName != null) {
                updateParamInfo(getPrimaryInputFileOptionName(), ifileName);
                updateGeoFileInfo(ifileName);

                //updateOFileInfo(SeadasFileUtils.getDefaultOFileNameFromIFile(ifileName, programName));
                updateOFileInfo(getOFileFullPath(ofileName));

                return true;
            }
        }
        VisatApp.getApp().showErrorDialog("Cannot compute output file name. Please select a correct input file for " + ((programName == null) ? "this processor." : programName));
        updateParamInfo(getPrimaryInputFileOptionName(), "");    //use an empty string
        updateOFileInfo("");
        return false;
    }

    public boolean updateGeoFileInfo(String ifileName) {
        updateGeoFileStatus(ifileName);
        if (hasGeoFile()) {
            updateParamInfo("geofile", SeadasFileUtils.getGeoFileNameFromIFile(ifileName));
            return true;
        }
        return false;
    }


    public boolean updateOFileInfo(String newValue) {
        System.out.println("next level ofile name: " + newValue);
        if (newValue != null) {
            updateParamInfo(getPrimaryOutputFileOptionName(), newValue + "\n");
            setReadyToRun(newValue.trim().length() == 0 ? false : true);
            return true;
        }
        return false;
    }


    public void setParamValue(String name, String value) {
        SeadasLogger.getLogger().info("primary io file option names: " + getPrimaryInputFileOptionName() + " " + getPrimaryOutputFileOptionName());
        if (name.trim().equals(getPrimaryInputFileOptionName())) {
            updateIFileInfo(value);
        } else if (name.trim().equals(getPrimaryOutputFileOptionName())) {
            updateOFileInfo(value);
        } else {
            updateParamInfo(name, value);
        }
    }

    private String getParFileCommandLineOption() {
        if (parFileOptionName.equals("none")) {
            return computeParFile().toString();
        } else {
            return parFileOptionName + "=" + computeParFile();
        }
    }

    private String[] getCmdArrayWithParFile() {
        final String[] cmdArray = {
                OCSSW.getOcsswScriptPath(),
                getProgramName(),
                getParFileCommandLineOption()
        };

        for (int i = 0; i < cmdArray.length; i++) {
            SeadasLogger.getLogger().info("i = " + i + " " + cmdArray[i]);
        }
        finalCmdArray.add(0, "programLocation : " + cmdArray[0]);
        finalCmdArray.add(1, "programName : " + cmdArray[1]);
        return cmdArray;
    }

    public String getCmdArrayString() {
        Iterator itr = finalCmdArray.iterator();
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

    private String[] getCmdArrayPrefix(){
        String[] cmdArrayPrefix;

        if (programName.equals(OCSSW.OCSSW_INSTALLER)) {
            cmdArrayPrefix = new String[paramList.getParamArray().size() + 1];
            cmdArrayPrefix[0] = getProgramName();
            if (!OCSSW.isOCSSWExist()) {
                cmdArrayPrefix[0] = OCSSW.TMP_OCSSW_INSTALLER;
            } else {
                cmdArrayPrefix[0] = OCSSW.getOcsswEnv() + "/run/scripts/install_ocssw.py";
            }
        } else {
            cmdArrayPrefix = new String[paramList.getParamArray().size() + 4];
            cmdArrayPrefix[0] = OCSSW.getOcsswScriptPath();
            cmdArrayPrefix[1] = "--ocsswroot";
            cmdArrayPrefix[2] = OCSSW.getOcsswEnv();
            cmdArrayPrefix[3] = getProgramName();
        }
        return cmdArrayPrefix;
    }

    private String[] getCmdArrayParam(){

        String[] cmdArrayParam = new String[paramList.getParamArray().size()];

        Iterator itr = paramList.getParamArray().iterator();
        ParamInfo option;
        String cmdString = null;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();

            if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
                if (option.getValue() != null && option.getValue().length() > 0) {
                    cmdArrayParam[option.getOrder()] = option.getValue();
                    cmdString = "argument : " + option.getValue();
                }
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
                cmdArrayParam[option.getOrder()] = option.getName() + "=" + option.getValue();
                cmdString = "option : " + option.getName() + "=" + option.getValue();
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
                if (option.getName() != null && option.getName().length() > 0) {
                    cmdArrayParam[option.getOrder()] = option.getName();
                    cmdString = "flag : " + option.getName();
                }
            }

            if (option.getType().equals(ParamInfo.Type.IFILE) && option.getValue() != null && option.getValue().trim().length() > 0) {
                filesToUpload.add(option.getValue());
                cmdString = cmdString.replaceAll("argument", "ifile");
                cmdString = cmdString.replaceAll("option", "ifile");
            } else if (option.getType().equals(ParamInfo.Type.OFILE)) {
                filesToDownload.add(option.getValue());
                cmdString = cmdString.replaceAll("argument", "ofile");
                cmdString = cmdString.replaceAll("option", "ofile");
            }
            finalCmdArray.add(cmdString);
            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + "=" + option.getValue());
        }
          return cmdArrayParam;
    }

    private String[] getCmdArrayWithArguments() {

        String[] cmdArrayPrefix = getCmdArrayPrefix();

        String[] cmdArray = concat(getCmdArrayPrefix(), getCmdArrayParam());

//
//        String[] cmdArrayParam = new String[paramList.getParamArray().size()];
//
//        Iterator itr = paramList.getParamArray().iterator();
//        ParamInfo option;
//        String cmdString = null;
//        while (itr.hasNext()) {
//            option = (ParamInfo) itr.next();
//
//            if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
//                if (option.getValue() != null && option.getValue().length() > 0) {
//                    cmdArray[option.getOrder() + cmdArrayParamIndex] = option.getValue();
//                    cmdString = "argument : " + option.getValue();
//                }
//            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
//                cmdArray[option.getOrder() + cmdArrayParamIndex] = option.getName() + "=" + option.getValue();
//                cmdString = "option : " + option.getName() + "=" + option.getValue();
//            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
//                if (option.getName() != null && option.getName().length() > 0) {
//                    cmdArray[option.getOrder() + cmdArrayParamIndex] = option.getName();
//                    cmdString = "flag : " + option.getName();
//                }
//            }
//
//            if (option.getType().equals(ParamInfo.Type.IFILE) && option.getValue() != null && option.getValue().trim().length() > 0) {
//                filesToUpload.add(option.getValue());
//                cmdString = cmdString.replaceAll("argument", "ifile");
//                cmdString = cmdString.replaceAll("option", "ifile");
//            } else if (option.getType().equals(ParamInfo.Type.OFILE)) {
//                filesToDownload.add(option.getValue());
//                cmdString = cmdString.replaceAll("argument", "ofile");
//                cmdString = cmdString.replaceAll("option", "ofile");
//            }
//            finalCmdArray.add(cmdString);
//            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + "=" + option.getValue());
//        }
//
//        // get rid of the null strings
//        ArrayList<String> cmdList = new ArrayList<String>();
//        for (String s : cmdArray) {
//            if (s != null) {
//                cmdList.add(s);
//            }
//        }
//
//        cmdArray = cmdList.toArray(new String[cmdList.size()]);
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

        filesToUpload = new ArrayList<String>();
        filesToDownload = new ArrayList<String>();
        finalCmdArray = new ArrayList<String>();

        if (acceptsParFile) {
            return getCmdArrayWithParFile();

        } else {

            return getCmdArrayWithArguments();
        }
    }

//    public String[] getProgramEnv() {
//        return processorEnv;
//
//    }

    public String[] getFilesToUpload() {
        return filesToUpload.toArray(new String[filesToUpload.size()]);
    }

    private File computeParFile() {

        try {
            final File tempFile = File.createTempFile("tmpParFile", ".par");
            tempFile.deleteOnExit();
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter(tempFile);
                fileWriter.write(getParString() + "\n");
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

        if (filesToUpload == null) {
            filesToUpload = new ArrayList<String>();
        }
        if (filesToDownload == null) {
            filesToDownload = new ArrayList<String>();
        }
        if (finalCmdArray == null) {
            finalCmdArray = new ArrayList<String>();
        }

        StringBuilder parString = new StringBuilder("");
        Iterator itr = paramList.getParamArray().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + " = " + option.getValue() + "option value is valid :" + (new Boolean(option.getValue().length() > 0)));
            SeadasLogger.getLogger().info(option.getName() + " = " + option.getValue() + "option type is :" + option.getType() + " " + option.getType().equals(ParamInfo.Type.HELP));

            if (!option.getType().equals(ParamInfo.Type.HELP) && option.getValue().length() > 0) {

                if (!option.getDefaultValue().equals(option.getValue())) {
                    parString.append(option.getName() + "=" + option.getValue() + "\n");
                    if (option.getType().equals(ParamInfo.Type.IFILE)) {
                        finalCmdArray.add("ifile : " + option.getName() + "=" + option.getValue());
                    } else if (option.getType().equals(ParamInfo.Type.OFILE)) {
                        finalCmdArray.add("ofile : " + option.getName() + "=" + option.getValue());
                    } else {
                        finalCmdArray.add("option : " + option.getName() + "=" + option.getValue());
                    }
                }

                if (option.getType().equals(ParamInfo.Type.IFILE)) {
                    filesToUpload.add(option.getValue());
                } else if (option.getType().equals(ParamInfo.Type.OFILE)) {
                    filesToDownload.add(option.getValue());
                }
            }

        }
        SeadasLogger.getLogger().info("parString: " + parString);
        // return parString.toString();
        return paramList.getParamString("\n");
    }

    public String getParStringForRemoteServer() {
        if (filesToUpload == null) {
            filesToUpload = new ArrayList<String>();
        }
        if (filesToDownload == null) {
            filesToDownload = new ArrayList<String>();
        }
        if (finalCmdArray == null) {
            finalCmdArray = new ArrayList<String>();
        }

        StringBuilder parString = new StringBuilder("");
        Iterator itr = paramList.getParamArray().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + " = " + option.getValue() + "option value is valid :" + (new Boolean(option.getValue().length() > 0)));
            SeadasLogger.getLogger().info(option.getName() + " = " + option.getValue() + "option type is :" + option.getType() + " " + option.getType().equals(ParamInfo.Type.HELP));

            if (!option.getType().equals(ParamInfo.Type.HELP) && option.getValue().length() > 0) {

                if (!option.getDefaultValue().equals(option.getValue())) {
                    //parString.append(option.getName() + "=" + option.getValue() + "\n");
                    if (option.getType().equals(ParamInfo.Type.IFILE)) {
                        finalCmdArray.add("ifile : " + option.getName() + "=" + option.getValue());
                        parString.append("ifile : " + option.getName() + "=" + option.getValue() + "\n");
                    } else if (option.getType().equals(ParamInfo.Type.OFILE)) {
                        finalCmdArray.add("ofile : " + option.getName() + "=" + option.getValue());
                        parString.append("ofile : " + option.getName() + "=" + option.getValue() + "\n");
                    } else {
                        finalCmdArray.add("option : " + option.getName() + "=" + option.getValue());
                        parString.append("option : " + option.getName() + "=" + option.getValue() + "\n");
                    }
                }

                if (option.getType().equals(ParamInfo.Type.IFILE)) {
                    filesToUpload.add(option.getValue());
                } else if (option.getType().equals(ParamInfo.PARAM_TYPE_OFILE)) {
                    filesToDownload.add(option.getValue());
                }
            }

        }
        SeadasLogger.getLogger().info("parString: " + parString);
        return parString.toString();
        //return paramList.getParamString("\n");
    }

    public String getInputFileList() {
        return null;
    }

    public String getOutputFileList() {
        return null;
    }

    public String getParFileName() {
        return null;
    }

    public EventInfo[] eventInfos = {
            new EventInfo("none", this),
    };

    private EventInfo getEventInfo(String name) {
        for (EventInfo eventInfo : eventInfos) {
            if (name.equals(eventInfo.getName())) {
                return eventInfo;
            }
        }
        return null;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        SeadasLogger.getLogger().info("added property name: " + propertyName);
        if (propertyName != null) {
            EventInfo eventInfo = getEventInfo(propertyName);
            if (eventInfo == null) {
                paramList.addPropertyChangeListener(propertyName, listener);
            } else {
                eventInfo.addPropertyChangeListener(listener);
            }
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        EventInfo eventInfo = getEventInfo(propertyName);
        if (eventInfo == null) {
            paramList.removePropertyChangeListener(propertyName, listener);
        } else {
            eventInfo.removePropertyChangeListener(listener);
        }
    }

    public void disableEvent(String name) {
        EventInfo eventInfo = getEventInfo(name);
        if (eventInfo == null) {
            SeadasLogger.getLogger().severe("disableEvent - eventInfo not found for " + name);
        } else {
            eventInfo.setEnabled(false);
        }
    }

    public void enableEvent(String name) {
        EventInfo eventInfo = getEventInfo(name);
        if (eventInfo == null) {
            SeadasLogger.getLogger().severe("enableEvent - eventInfo not found for " + name);
        } else {
            eventInfo.setEnabled(true);
        }
    }

    public void fireEvent(String name) {
        fireEvent(name, null, null);
    }

    public void fireEvent(String name, Object oldValue, Object newValue) {
        EventInfo eventInfo = getEventInfo(name);
        if (eventInfo == null) {
            getPropertyChangeSupport().firePropertyChange(new PropertyChangeEvent(this, name, oldValue, newValue));
        } else {
            eventInfo.fireEvent(oldValue, newValue);
        }
    }

    public void fireAllParamEvents() {
        for (ParamInfo paramInfo : paramList.getParamArray()) {
            if (paramInfo.getName() != null && !paramInfo.getName().toLowerCase().equals("none")) {
                fireEvent(paramInfo.getName());
            }
        }
    }

    public File getRootDir() {
        File rootDir = (new File(getParamValue(getPrimaryInputFileOptionName()))).getParentFile();
        if (rootDir != null) {
            return rootDir;
        } else {
            try {
                rootDir = OCSSW.getOcsswRoot();
            } catch (Exception e) {
                SeadasLogger.getLogger().severe("error in getting ocssw root!");
            }

        }
        return rootDir == null ? new File(".") : rootDir;
    }

    public ProcessorModel getSecondaryProcessor() {
        return secondaryProcessor;
    }

    public void setSecondaryProcessor(ProcessorModel secondaryProcessor) {
        this.secondaryProcessor = secondaryProcessor;
    }

    public boolean isValidIfile() {
        return true;
    }

    public boolean isGeofileRequired() {
        return hasGeoFile;
    }

    @Override
    public boolean isWavelengthRequired() {
        return true;
    }

    private boolean verifyIFilePath(String ifileName) {

        File ifile = new File(ifileName);

        if (ifile.exists()) {
            return true;
        }
        return false;
    }

    private String getIfileDirString() {
        String ifileDir;
        try {
            ifileDir = getParamValue(getPrimaryInputFileOptionName());
            ifileDir = ifileDir.substring(0, ifileDir.lastIndexOf(System.getProperty("file.separator")));
        } catch (Exception e) {
            ifileDir = System.getProperty("user.dir");
        }
        return ifileDir;
    }

    public File getIFileDir() {

        if (new File(getIfileDirString()).isDirectory()) {
            return new File(getIfileDirString());
        } else {
            return null;
        }
    }

    private String getOFileFullPath(String fileName) {

        if (fileName.indexOf(System.getProperty("file.separator")) == 0 && new File(fileName).getParentFile().exists()) {
            return fileName;
        } else if (new File(getIfileDirString(), fileName).getParentFile().exists()) {
            return getIfileDirString() + System.getProperty("file.separator") + fileName;

        } else {
            return null;
        }
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public ParamList getParamList() {
        return paramList;
    }

    public void setParamList(ParamList paramList) {
        this.paramList = paramList;
    }

    public void setParamList(ArrayList<ParamInfo> paramArray) {
        paramList.clear();
        for (ParamInfo param : paramArray) {
            paramList.addInfo(param);
        }
    }

    public SwingPropertyChangeSupport getPropertyChangeSupport() {
        return paramList.getPropertyChangeSupport();
    }

    public void appendPropertyChangeSupport(SwingPropertyChangeSupport propertyChangeSupport) {
        paramList.appendPropertyChangeSupport(propertyChangeSupport);
    }

    public Set<String> getPrimaryOptions() {
        return primaryOptions;
    }

    public void setPrimaryOptions(Set<String> primaryOptions) {
        this.primaryOptions = primaryOptions;
    }

    public String getRunButtonPropertyName() {
        return runButtonPropertyName;
    }

    public String getAllparamInitializedPropertyName() {
        return allparamInitializedPropertyName;
    }

    private String executionLogMessage;

    public String getExecutionLogMessage() {
        return executionLogMessage;
    }

    public void setExecutionLogMessage(String executionLogMessage) {
        this.executionLogMessage = executionLogMessage;
    }

    public void setProgressPattern(Pattern progressPattern) {
        this.progressPattern = progressPattern;
    }

    public Pattern getProgressPattern() {
        return progressPattern;
    }

    public boolean isOpenInSeadas() {
        return openInSeadas;
    }

    public void setOpenInSeadas(boolean openInSeadas) {
        this.openInSeadas = openInSeadas;
    }


    private static class Extractor_Processor extends ProcessorModel {
        Extractor_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);

//            addPropertyChangeListener(getPrimaryInputFileOptionName(), new PropertyChangeListener() {
//                  @Override
//                  public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                      String newProdValue = (String) propertyChangeEvent.getNewValue();
//                      setProgramName(getExtractorProgramName(newProdValue));
//                   }
//              });
        }

        public boolean updateIFileInfo(String ifileName) {

            setProgramName(getExtractorProgramName(ifileName));
            return super.updateIFileInfo(ifileName);
        }

        private String getExtractorProgramName(String ifileName) {

            FileInfo ifileInfo = new FileInfo(ifileName);
            SeadasFileUtils.debug("Extractor ifile info: " + ifileInfo.getTypeName() + ifileInfo.getMissionName());
            String programName = null;
            if (ifileInfo.getMissionName() != null && ifileInfo.getTypeName() != null) {
                if (ifileInfo.getMissionName().indexOf("MODIS") != -1 && ifileInfo.getTypeName().indexOf("1A") != -1) {
                    programName = "l1aextract_modis";
                } else if (ifileInfo.getMissionName().indexOf("SeaWiFS") != -1 && ifileInfo.getTypeName().indexOf("1A") != -1 ||
                        ifileInfo.getMissionName().indexOf("CZCS") != -1) {
                    programName = "l1aextract_seawifs";
                } else if ((ifileInfo.getTypeName().indexOf("L2") != -1 || ifileInfo.getTypeName().indexOf("Level 2") != -1) ||
                        (ifileInfo.getMissionName().indexOf("OCTS") != -1 && (ifileInfo.getTypeName().indexOf("L1") != -1 || ifileInfo.getTypeName().indexOf("Level 1") != -1))) {
                    programName = "l2extract";
                }
            }
            return programName;
        }
    }

    private static class Modis_L1B_Processor extends ProcessorModel {
        Modis_L1B_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
        }

        public boolean updateOFileInfo(String ofileName) {
            updateParamInfo("--okm", ofileName.replaceAll("LAC", "LAC"));
            getParamInfo("--okm").setDefaultValue(getParamValue("--okm"));
            updateParamInfo("--hkm", ofileName.replaceAll("LAC", "HKM"));
            getParamInfo("--hkm").setDefaultValue(getParamValue("--hkm"));
            updateParamInfo("--qkm", ofileName.replaceAll("LAC", "QKM"));
            getParamInfo("--qkm").setDefaultValue(getParamValue("--qkm"));
            updateParamInfo("--obc", ofileName.replaceAll("LAC", "OBC"));
            getParamInfo("--obc").setDefaultValue(getParamValue("--obc"));
            setReadyToRun(ofileName.trim().length() == 0 ? false : true);
            return true;
        }

        public String getOfileName() {

            StringBuilder ofileNameList = new StringBuilder();
            if (!(getParamInfo("--del-okm").getValue().equals("true") || getParamInfo("--del-okm").getValue().equals("1"))) {
                ofileNameList.append("\n" + getParamValue("--okm"));
            }
            if (!(getParamInfo("--del-hkm").getValue().equals("true") || getParamInfo("--del-hkm").getValue().equals("1"))) {
                ofileNameList.append("\n" + getParamValue("--hkm"));
            }
            if (!(getParamInfo("--del-qkm").getValue().equals("true") || getParamInfo("--del-qkm").getValue().equals("1"))) {
                ofileNameList.append("\n" + getParamValue("--qkm"));
            }
            if (getParamInfo("--keep-obc").getValue().equals("true") || getParamInfo("--keep-obc").getValue().equals("1")) {
                ofileNameList.append("\n" + getParamValue("--obc"));
            }
            return ofileNameList.toString();
        }
    }

    private static class LonLat2Pixels_Processor extends ProcessorModel {
        LonLat2Pixels_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
            addPropertyChangeListener("ifile", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                    String oldProdValue = (String) propertyChangeEvent.getOldValue();
//                    String newProdValue = (String) propertyChangeEvent.getNewValue();
//                    //System.out.println("property ifile changed");
                    checkCompleteness();

                }
            });
            addPropertyChangeListener("SWlon", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                    String oldProdValue = (String) propertyChangeEvent.getOldValue();
//                    String newProdValue = (String) propertyChangeEvent.getNewValue();
//                    System.out.println("property SWlon changed");
                    checkCompleteness();

                }
            });
            addPropertyChangeListener("SWlat", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                    String oldProdValue = (String) propertyChangeEvent.getOldValue();
//                    String newProdValue = (String) propertyChangeEvent.getNewValue();
//                    System.out.println("property SWlat changed");
                    checkCompleteness();

                }
            });
            addPropertyChangeListener("NElon", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                    String oldProdValue = (String) propertyChangeEvent.getOldValue();
//                    String newProdValue = (String) propertyChangeEvent.getNewValue();
//                    System.out.println("property NElon changed");
                    checkCompleteness();

                }
            });
            addPropertyChangeListener("NElat", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
//                    String oldProdValue = (String) propertyChangeEvent.getOldValue();
//                    String newProdValue = (String) propertyChangeEvent.getNewValue();
//                    System.out.println("property NElat changed");
                    checkCompleteness();
                }
            });
        }

        public boolean updateIFileInfo(String ifileName) {
            updateParamInfo(getPrimaryInputFileOptionName(), ifileName);
            updateGeoFileInfo(ifileName);
            return true;
        }
    }

    private static class L2Bin_Processor extends ProcessorModel {
        L2Bin_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
            setMultipleInputFiles(true);
        }
    }

    private static class L2BinAquarius_Processor extends ProcessorModel {
        L2BinAquarius_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
            setMultipleInputFiles(true);
        }
    }

    private static class L3Bin_Processor extends ProcessorModel {
        L3Bin_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
            setMultipleInputFiles(true);
            addPropertyChangeListener("out_parm", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    String oldProdValue = (String) propertyChangeEvent.getOldValue();
                    String newProdValue = (String) propertyChangeEvent.getNewValue();
                    String ofileName = getParamValue(getPrimaryOutputFileOptionName());
                    if (oldProdValue.trim().length() > 0 && ofileName.indexOf(oldProdValue) != -1) {
                        ofileName = ofileName.replaceAll(oldProdValue, newProdValue);
                    } else {
                        ofileName = ofileName + "_" + newProdValue;
                    }
                    updateOFileInfo(ofileName);
                }
            });
        }

        public String getOfileName() {
            if (!(getParamValue("noext").equals("1"))) {
                return getParamValue(getPrimaryOutputFileOptionName()) + ".main";
            }
            return getParamValue(getPrimaryOutputFileOptionName());
        }
    }


    private static class SMIGEN_Processor extends ProcessorModel {
        SMIGEN_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
            addPropertyChangeListener("prod", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    String oldProdValue = (String) propertyChangeEvent.getOldValue();
                    String newProdValue = (String) propertyChangeEvent.getNewValue();
                    String ofileName = getParamValue(getPrimaryOutputFileOptionName());
                    if (oldProdValue.trim().length() > 0 && ofileName.indexOf(oldProdValue) != -1) {
                        ofileName = ofileName.replaceAll(oldProdValue, newProdValue);
                    } else {
                        ofileName = ofileName + "_" + newProdValue;
                    }
                    updateOFileInfo(ofileName);
                }
            });

            addPropertyChangeListener("resolution", new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    String oldResolutionValue = (String) propertyChangeEvent.getOldValue();
                    String newResolutionValue = (String) propertyChangeEvent.getNewValue();
                    String ofileName = getParamValue(getPrimaryOutputFileOptionName());
                    if (newResolutionValue.trim().length() > 0 && ofileName.indexOf(newResolutionValue) != -1) {
                        ofileName = ofileName.replaceAll(oldResolutionValue, newResolutionValue);
                    } else {
                        ofileName = ofileName + "_" + newResolutionValue;
                    }
                    updateOFileInfo(ofileName);
                }
            });
            setOpenInSeadas(true);
        }
    }

    private static class OCSSWInstaller_Processor extends ProcessorModel {
        OCSSWInstaller_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
        }

        @Override
        public String[] getProgramCmdArray() {
            String[] cmdArray = super.getProgramCmdArray();
            if (!OCSSW.isOCSSWExist()) {
                cmdArray[0] = OCSSW.TMP_OCSSW_INSTALLER;
            } else {
                cmdArray[0] = OCSSW.getOcsswEnv() + "/run/scripts/install_ocssw.py";
            }
            return cmdArray;
        }
    }
}