package gov.nasa.gsfc.seadas.processing.core;

import gov.nasa.gsfc.seadas.processing.general.*;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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

    private ProcessorModel secondaryProcessor;
    private Pattern progressPattern;

    private ProcessorTypeInfo.ProcessorID processorID;

    private boolean multipleInputFiles;
    private ArrayList<String> filesToUpload;
    private ArrayList<String> filesToDownload;
    private ArrayList<String> finalCmdArray;

    public ProcessorModel(String name) {
        acceptsParFile = false;
        hasGeoFile = false;
        readyToRun = false;
        multipleInputFiles = false;
        paramList = new ParamList();
        parFileOptionName = ParamUtils.DEFAULT_PAR_FILE_NAME;

        programName = name;
        processorID = ProcessorTypeInfo.getProcessorID(programName);
        //computeProcessorEnv();

        primaryOptions = new HashSet<String>();
        primaryOptions.add("ifile");
        primaryOptions.add("ofile");

        progressPattern = Pattern.compile(ParamUtils.DEFAULT_PROGRESS_REGEX);
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
        }
    }

    public ProcessorModel(String name, ArrayList<ParamInfo> paramList) {
        this(name);
        setParamList(paramList);
    }

    public static ProcessorModel valueOf(String programName, String xmlFileName) {
        ProcessorTypeInfo.ProcessorID processorID = ProcessorTypeInfo.getProcessorID(programName);
        switch (processorID) {
            case MODIS_L1B_PY:
                return new Modis_L1B_Processor(programName, xmlFileName);
            case LONLAT2PIXLINE:
                return new LonLat2Pixels_Processor(programName, xmlFileName);
            case SMIGEN:
                return new SMIGEN_Processor(programName, xmlFileName);
            case L2BIN:
                return new L2Bin_Processor(programName, xmlFileName);
            case L3BIN:
                return new L3Bin_Processor(programName, xmlFileName);
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

    private void checkCompleteness() {
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
            updateParamInfo(getPrimaryInputFileOptionName(), ifileName);
            updateGeoFileInfo(ifileName);
            updateOFileInfo(SeadasFileUtils.getDefaultOFileNameFromIFile(ifileName, programName));
            return true;
        } else {
            return false;
        }

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

        String ofileFullPathName = getOFileFullPath(newValue);
        if (ofileFullPathName != null) {
            updateParamInfo(getPrimaryOutputFileOptionName(), ofileFullPathName);
            setReadyToRun(true);
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

    private String[] getCmdArrayWithArguments() {

        String[] cmdArray = new String[paramList.getParamArray().size() + 2];

        cmdArray[0] = OCSSW.getOcsswScriptPath();
        cmdArray[1] = getProgramName();

        Iterator itr = paramList.getParamArray().iterator();
        ParamInfo option;
        String cmdString = null;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();

            if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
                if (option.getValue() != null && option.getValue().length() > 0) {
                    cmdArray[option.getOrder() + 1] = option.getValue();
                    cmdString = "argument : " + option.getValue();
                }
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
                cmdArray[option.getOrder() + 1] = option.getName() + "=" + option.getValue();
                cmdString = "option : " + option.getName() + "=" + option.getValue();
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
                if (option.getName() != null && option.getName().length() > 0) {
                    cmdArray[option.getOrder() + 1] = option.getName();
                    cmdString = "flag : " + option.getName();
                }
            }

            if (option.getType().equals(ParamInfo.Type.IFILE)) {
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
                fileWriter.write(getParString());
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
        EventInfo eventInfo = getEventInfo(propertyName);
        if (eventInfo == null) {
            paramList.addPropertyChangeListener(propertyName, listener);
        } else {
            eventInfo.addPropertyChangeListener(listener);
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

    private static class Modis_L1B_Processor extends ProcessorModel {
        Modis_L1B_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
        }

        public boolean updateOFileInfo(String ofileName) {
            updateParamInfo("--okm", ofileName.replaceAll("LAC", "OKM"));
            updateParamInfo("--hkm", ofileName.replaceAll("LAC", "HKM"));
            updateParamInfo("--qkm", ofileName.replaceAll("LAC", "QKM"));
            updateParamInfo("--obc", ofileName.replaceAll("LAC", "OBC"));
            setReadyToRun(true);
            return true;

        }

        public String getOfileName() {

            StringBuilder ofileNameList = new StringBuilder();
            ofileNameList.append("\n " + getParamValue("--okm"));
            ofileNameList.append("\n " + getParamValue("--hkm"));
            ofileNameList.append("\n " + getParamValue("--qkm"));
            ofileNameList.append("\n " + getParamValue("--obc"));
            System.out.println(ofileNameList.toString());
            return ofileNameList.toString();
        }
    }

    private static class LonLat2Pixels_Processor extends ProcessorModel {
        LonLat2Pixels_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
        }

        public boolean updateOFileInfo(String ofileName) {
            return true;
        }
    }

    private static class L2Bin_Processor extends ProcessorModel {
        L2Bin_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
            setMultipleInputFiles(true);
        }


    }

    private static class L3Bin_Processor extends ProcessorModel {
        L3Bin_Processor(String programName, String xmlFileName) {
            super(programName, xmlFileName);
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

            if (!(getParamValue("noext").equals("1") || getParamValue("noext").equals("1"))) {
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
        }
    }


}