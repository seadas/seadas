package gov.nasa.gsfc.seadas.processing.core;

import gov.nasa.gsfc.seadas.processing.general.*;
import org.esa.beam.util.Guardian;
import org.esa.beam.visat.VisatApp;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
public class ProcessorModel implements L2genDataProcessorModel {
    private String programName;
    private String programLocation;
    private ArrayList<ParamInfo> paramList;
    private boolean acceptsParFile;
    private String[] processorEnv;
    private String errorMessage;
    private String parString;

    private boolean hasGeoFile;
    private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    private Set<String> primaryOptions;
    private String parFileOptionName;

    private boolean readyToRun;
    private final String runButtonPropertyName = "RUN_BUTTON_STATUS_CHANGED";
    private final String allparamInitializedPropertyName = "ALL_PARAMS_INITIALIZED";

    private String primaryInputFileOptionName;
    private String primaryOutputFileOptionName;
    private ProcessorModel secondaryProcessor;
    private Pattern progressPattern;

    ProcessorTypeInfo.ProcessorID processorID;

    public ProcessorModel(String name) {
        this(name, null);
    }

    public ProcessorModel(String name, String parXMLFileName) {
        this.setProgramName(name);
        computeProcessorEnv();
        String progressRegex = ParamUtils.DEFAULT_PROGRESS_REGEX;
        if (parXMLFileName != null) {
            setParamList(ParamUtils.computeParamList(parXMLFileName));
            acceptsParFile = ParamUtils.getOptionStatus(parXMLFileName, "hasParFile");
            parFileOptionName = ParamUtils.getParFileOptionName(parXMLFileName);
            progressRegex = ParamUtils.getProgressRegex(parXMLFileName);
            hasGeoFile = ParamUtils.getOptionStatus(parXMLFileName, "hasGeoFile");
            setPrimaryOptions(ParamUtils.getPrimaryOptions(parXMLFileName));
            setPrimaryInputFileOptionName(getPrimaryInputFileOptionName());
            setPrimaryOutputFileOptionName(getPrimaryOutputFileOptionName());
        } else {
            setParamList(new ArrayList<ParamInfo>());
            parFileOptionName = ParamUtils.DEFAULT_PAR_FILE_NAME;
            acceptsParFile = false;
            hasGeoFile = false;
        }
        processorID = ProcessorTypeInfo.getProcessorID(programName);
        progressPattern = Pattern.compile(progressRegex);
    }

    public void addParamInfo(ParamInfo info) {
        getParamList().add(info);

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


    public void addParamInfo(String name, String value, int order) {
        ParamInfo info = new ParamInfo(name, value);
        info.setOrder(order);
        addParamInfo(info);
    }

    public String getParFileOptionName() {

        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getType() != null) {
                if (option.getType().equals(ParamInfo.Type.IFILE) && getPrimaryOptions().contains(option.getName())) {

                    return option.getName();
                }
            }
        }
        return ParamUtils.DEFAULT_PAR_FILE_NAME;
    }

    public String getPrimaryInputFileOptionName() {

        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getType() != null) {
                if (option.getType().equals(ParamInfo.Type.IFILE) && getPrimaryOptions().contains(option.getName())) {

                    return option.getName();
                }
            }
        }
        return null;
    }

    public String getPrimaryOutputFileOptionName() {

        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getType() != null) {
                if (option.getType().equals(ParamInfo.Type.OFILE) && getPrimaryOptions().contains(option.getName())) {

                    return option.getName();
                }
            }
        }
        return "";
    }

    public boolean hasGeoFile() {
        return hasGeoFile;
    }

    public void setHasGeoFile(boolean hasGeoFile) {
        this.hasGeoFile = hasGeoFile;
    }

    public boolean isValidProcessor() {
        SeadasLogger.getLogger().info("program location: " + programLocation);
        return programLocation != null;
    }

    public String getProgramName() {
        return programName;
    }

    public boolean hasPrimaryOutputFile() {
        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getType().equals(ParamInfo.Type.OFILE) && getPrimaryOptions().contains(option.getName())) {

                return true;
            }
        }
        return false;
    }


    public ArrayList getProgramParamList() {
        return getParamList();
    }


    public void setParString(String parString) {
        this.parString = parString;
    }

    public void setAcceptsParFile(boolean acceptsParFile) {
        this.acceptsParFile = acceptsParFile;
    }

    public boolean acceptsParFile() {
        return acceptsParFile;
    }

    public String getProgramErrorMessage() {
        return errorMessage;
    }

    public void updateParamInfo(ParamInfo currentOption, String newValue) {
        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = itr.next();
            SeadasLogger.getLogger().info(option.getName() + "|  " + currentOption.getName() + "|");
            if (option.getName().equals(currentOption.getName())) {
                String oldValue = option.getValue();
                option.setValue(newValue);
                checkCompleteness();
                getPropertyChangeSupport().firePropertyChange(option.getName(), oldValue, newValue);
                return;
            }
        }
    }

    public void propertyChange() {
        //addPropertyChangeListener();
    }

    private void checkCompleteness() {
        boolean complete = true;
        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = itr.next();
            if (option.getValue() == null || option.getValue().trim().length() == 0) {
                complete = false;
                break;
            }
        }

        if (complete) {
            fireEvent(getAllparamInitializedPropertyName(), false, true);
        }
    }

    public ParamInfo getParamInfo(String paramName) {
        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = itr.next();
            if (option.getName().equals(paramName.trim())) {
                return option;
            }
        }
        return null;
    }

    public boolean isAllParamsValid() {

        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = itr.next();
            if (option.getValue() == null) {
                return false;
            }
        }
        return true;
    }

    public String getParamValue(String paramName) {
        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = itr.next();
            if (option.getName().equals(paramName)) {
                return option.getValue();
            }
        }
        return null;
    }

    public void updateParamInfo(String paramName, String newValue) {
        Guardian.assertNotNull("parameter name", paramName);
        Iterator<ParamInfo> itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = itr.next();
            if (option.getName().equals(paramName.trim())) {
                String oldValue = option.getValue();
                //newValue = "";
                option.setValue(newValue);
                checkCompleteness();
                getPropertyChangeSupport().firePropertyChange(paramName, oldValue, newValue);
                return;
            }
        }
    }

    private void updateGeoFileStatus(String ifileName) {

        //ProcessorTypeInfo.ProcessorID processorID = ProcessorTypeInfo.getProcessorID(programName);

        if ((processorID == ProcessorTypeInfo.ProcessorID.L1BRSGEN
                || processorID == ProcessorTypeInfo.ProcessorID.L1MAPGEN
                || processorID == ProcessorTypeInfo.ProcessorID.MODIS_L1B_PY) && (new FileInfo(ifileName)).getMissionName().indexOf("MODIS") != -1) {
                setHasGeoFile(true);

        }                         else {
            setHasGeoFile(false);
        }
    }

    public boolean updateIFileInfo(String ifileName) {

        if (verifyIFilePath(ifileName)) {
            updateParamInfo(getPrimaryInputFileOptionName(), ifileName);

            updateGeoFileStatus(ifileName);
            if (hasGeoFile()) {
                updateParamInfo("geofile", SeadasFileUtils.getGeoFileNameFromIFile(ifileName));
            }
            if (hasPrimaryOutputFile()) {
                updateParamInfo(getPrimaryOutputFileOptionName(), SeadasFileUtils.getDefaultOFileNameFromIFile(ifileName, programName));
                setReadyToRun(true);
            }
            return true;
        } else {
            return false;
        }

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

    private void computeProcessorEnv() {

        final File ocsswRoot;
        try {
            ocsswRoot = OCSSW.getOcsswRoot();
        } catch (IOException e) {
            errorMessage = e.getMessage();
            if (VisatApp.getApp() != null)
                VisatApp.getApp().showErrorDialog(getProgramName(), e.getMessage());
            return;
        }

        final String[] envp = {
                "OCSSWROOT=" + ocsswRoot.getPath()
        };

        processorEnv = envp;
        programLocation = ocsswRoot.getPath() + "/run/scripts/";
    }

    private String[] getCmdArrayWithParFile() {
        final String[] cmdArray = {
                programLocation + "ocssw_runner",
                getProgramName(),
                parFileOptionName + "=" + computeParFile()
        };
        for (int i = 0; i < cmdArray.length; i++) {
            SeadasLogger.getLogger().info("i = " + i + " " + cmdArray[i]);
        }
        return cmdArray;
    }

    private String[] getCmdArrayWithArguments() {
        String[] cmdArray = new String[getParamList().size() + 2];
        cmdArray[0] = programLocation + "ocssw_runner";
        cmdArray[1] = getProgramName();

        Iterator itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();

            if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
                cmdArray[option.getOrder() + 1] = option.getValue();
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
                cmdArray[option.getOrder() + 1] = option.getName() + "=" + option.getValue();
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
                cmdArray[option.getOrder() + 1] = option.getName();
            }

            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + "=" + option.getValue());
        }

        ArrayList<String> finalCommandArgList = new ArrayList<String>();
        for (String s : cmdArray) {
            if (s != null && s.length() > 0) {

                finalCommandArgList.add(s);
            }
        }

        cmdArray = finalCommandArgList.toArray(new String[finalCommandArgList.size()]);

        for (int i = 0; i < cmdArray.length; i++) {
            SeadasLogger.getLogger().info("i = " + i + " " + cmdArray[i]);
        }

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

        if (acceptsParFile) {
            return getCmdArrayWithParFile();

        } else {

            return getCmdArrayWithArguments();
        }
    }

    public String[] getProgramEnv() {
        return processorEnv;

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

        if (parString != null) {
            return parString;
        }


        StringBuilder parString = new StringBuilder("");
        Iterator itr = getParamList().iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + " = " + option.getValue() + "option value is valid :" + (new Boolean(option.getValue().length() > 0)));
            SeadasLogger.getLogger().info(option.getName() + " = " + option.getValue() + "option type is :" + option.getType() + " " + option.getType().equals(ParamInfo.Type.HELP));

            if (!option.getType().equals(ParamInfo.Type.HELP) && option.getValue().length() > 0) {

                if (!option.getDefaultValue().equals(option.getValue())) {
                    parString = parString.append(option.getName() + "=" + option.getValue() + "\n");
                }
            }

        }
        SeadasLogger.getLogger().info("parString: " + parString);
        return parString.toString();
    }

    public Process executeProcess() throws IOException {
        try {
            return executeProcess(getRootDir());
        } catch (Exception e) {
            SeadasLogger.getLogger().severe(e.getMessage());
            return Runtime.getRuntime().exec(getProgramCmdArray(), getProgramEnv());
        }

    }

    public Process executeProcess(File rootDir) throws IOException {

        SeadasLogger.getLogger().info("Executing processor " + getProgramName() + "...");

        //return Runtime.getRuntime().exec(getProgramCmdArray(), getProgramEnv(), getProgramRoot());

        return Runtime.getRuntime().exec(getProgramCmdArray(), getProgramEnv(), rootDir);
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
            getPropertyChangeSupport().addPropertyChangeListener(propertyName, listener);
        } else {
            eventInfo.addPropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        EventInfo eventInfo = getEventInfo(propertyName);
        if (eventInfo == null) {
            getPropertyChangeSupport().removePropertyChangeListener(propertyName, listener);
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
        for (ParamInfo paramInfo : getParamList()) {
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

    public boolean isRequiresGeofile() {
        return hasGeoFile;
    }

    private boolean verifyIFilePath(String ifileName) {

        File ifile = new File(ifileName);

        if (ifile.exists()) {
            return true;
        }
        return false;
    }

    private String getIfileDir() {

        String ifileDir = getParamValue(getPrimaryInputFileOptionName());
        ifileDir = ifileDir.substring(0, ifileDir.lastIndexOf(System.getProperty("file.separator")));
        return ifileDir;
    }

    private String getOFileFullPath(String fileName) {

        if (fileName.indexOf(System.getProperty("file.separator")) == 0 && new File(fileName).getParentFile().exists()) {
            return fileName;
        } else if (new File(getIfileDir(), fileName).getParentFile().exists()) {
            return getIfileDir() + System.getProperty("file.separator") + fileName;

        } else {
            return null;
        }
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public ArrayList<ParamInfo> getParamList() {
        return paramList;
    }

    public void setParamList(ArrayList<ParamInfo> paramList) {
        this.paramList = paramList;
    }

    public SwingPropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public void appendPropertyChangeSupport(SwingPropertyChangeSupport propertyChangeSupport) {
        PropertyChangeListener[] pr = propertyChangeSupport.getPropertyChangeListeners();
        for (int i = 0; i < pr.length; i++) {
            this.propertyChangeSupport.addPropertyChangeListener(pr[i]);
        }
    }

    public void setPropertyChangeSupport(SwingPropertyChangeSupport propertyChangeSupport) {
        this.propertyChangeSupport = propertyChangeSupport;
    }

    public Set<String> getPrimaryOptions() {
        return primaryOptions;
    }

    public void setPrimaryOptions(Set<String> primaryOptions) {
        this.primaryOptions = primaryOptions;
    }

    public void setPrimaryInputFileOptionName(String primaryInputFileOptionName) {
        this.primaryInputFileOptionName = primaryInputFileOptionName;
    }

    public void setPrimaryOutputFileOptionName(String primaryOutputFileOptionName) {
        this.primaryOutputFileOptionName = primaryOutputFileOptionName;
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
}