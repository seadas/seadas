package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.l2gen.EventInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.ParamInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.SeadasPrint;
import org.esa.beam.util.Guardian;
import org.esa.beam.visat.VisatApp;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
public class ProcessorModel {
    private static final String PROCESSING_SCAN_REGEX = "Processing scan .+?\\((\\d+) of (\\d+)\\)";
    static final Pattern PROCESSING_SCAN_PATTERN = Pattern.compile(PROCESSING_SCAN_REGEX);

    private String programName;
    private String programLocation;
    private ArrayList<ParamInfo> paramList;
    private boolean acceptsParFile;
    private String[] processorEnv;
    private String errorMessage;
    private File programRoot;
    private String parString;

    private boolean hasGeoFile;
    private SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);
    private PropertyChangeSupport changeSupport;

    private Set<String> primaryOptions;

    private String primaryInputFileOptionName, primaryOutputFileOptionName;

    public ProcessorModel(String name) {
        this(name, null);
    }

    public ProcessorModel(String name, String parXMLFileName) {
        this.programName = name;
        computeProcessorEnv();
        if (parXMLFileName != null) {
            paramList = ParamUtils.computeParamList(parXMLFileName);
            acceptsParFile = ParamUtils.getOptionStatus(parXMLFileName, "hasParFile");
            hasGeoFile = ParamUtils.getOptionStatus(parXMLFileName, "hasGeoFile");
            primaryOptions = ParamUtils.getPrimaryOptions(parXMLFileName);
            primaryInputFileOptionName = getPrimaryInputFileOptionName();
            primaryOutputFileOptionName = getPrimaryOutputFileOptionName();
        }

    }

    protected String getPrimaryInputFileOptionName() {

        Iterator<ParamInfo> itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getType().equals(ParamInfo.Type.IFILE) && primaryOptions.contains(option.getName())) {

                return option.getName();
            }
        }
        return null;
    }

    protected String getPrimaryOutputFileOptionName() {

        Iterator<ParamInfo> itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getType().equals(ParamInfo.Type.OFILE) && primaryOptions.contains(option.getName())) {

                return option.getName();
            }
        }
        return null;
    }

    protected boolean hasGeoFile() {
        return hasGeoFile;
    }

    public boolean isValidProcessor() {
        SeadasPrint.debug(programLocation);
        return programLocation != null;
    }

    public String getProgramName() {
        return programName;
    }

    protected boolean hasPrimaryOutputFile() {
        Iterator<ParamInfo> itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getType().equals(ParamInfo.Type.OFILE) && primaryOptions.contains(option.getName())) {

                return true;
            }
        }
        return false;
    }

    public String getProgramLocation() {
        return programLocation;
    }

    public File getProgramRoot() {
        return programRoot;
    }

    public ArrayList getProgramParamList() {
        return paramList;
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

//    public File getOutputFile() {
//
//        if (getOFileName() != null) {
//           return new File(getParamValue(getOFileName()));
//        }
//
//        return null;
//
//    }

    public void updateParamInfo(ParamInfo currentOption, String newValue) {
        Iterator<ParamInfo> itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {

            option = itr.next();
            if (option.getName().equals(currentOption.getName())) {

                option.setValue(newValue);
                return;
            }
        }
    }

    public void propertyChange() {
        //addPropertyChangeListener();
    }

    public ParamInfo getParamInfo(String paramName) {
        Iterator<ParamInfo> itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = itr.next();
            if (option.getName().equals(paramName)) {
                return option;
            }
        }
        return null;
    }

    public boolean isAllParamsValid() {

        Iterator<ParamInfo> itr = paramList.iterator();
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
        Iterator<ParamInfo> itr = paramList.iterator();
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
        Iterator<ParamInfo> itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = itr.next();
            if (option.getName().equals(paramName.trim())) {
                option.setValue(newValue);
                return;
            }
        }
    }

    private void computeProcessorEnv() {

        final File ocsswRoot;
        try {
            ocsswRoot = OCSSW.getOcsswRoot();
        } catch (IOException e) {
            errorMessage = e.getMessage();
            if (VisatApp.getApp() != null)
                VisatApp.getApp().showErrorDialog(programName, e.getMessage());
            return;
        }

        final String[] envp = {
                "OCSSWROOT=" + ocsswRoot.getPath()
        };

        processorEnv = envp;
        programLocation = ocsswRoot.getPath() + "/run/scripts/";
        programRoot = ocsswRoot;
    }

    private String[] getCmdArrayWithParFile() {
        final String[] cmdArray = {
                programLocation + "ocssw_runner",
                programName,
                "par=" + computeParFile()
        };
        for (int i = 0; i < cmdArray.length; i++) {
            SeadasLogger.getLogger().info("i = " + i + " " + cmdArray[i]);
        }
        return cmdArray;
    }

    private String[] getCmdArrayWithArguments() {

//        SeadasLogger.getLogger().info("ifile update: " + getIFileName() + inputFile.toString());
//        SeadasLogger.getLogger().info("ofile update: " + getOFileName() + getOutputFile().toString());
        final String[] cmdArray = new String[paramList.size() + 2];
        cmdArray[0] = programLocation + "ocssw_runner";
        cmdArray[1] = programName;

        Iterator itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            cmdArray[option.getOrder() + 1] = option.getValue();
            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + "=" + option.getValue());
        }

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

    protected String getParString() {

        if (parString != null) {
            return parString;
        }


        StringBuilder parString = new StringBuilder("");
        Iterator itr = paramList.iterator();
        ParamInfo option;
        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            SeadasLogger.getLogger().info("order: " + option.getOrder() + "  " + option.getName() + " = " + option.getValue() + "option value is valid :" + (new Boolean(option.getValue().length() > 0)));
            SeadasLogger.getLogger().info(option.getName() + " = " + option.getValue() + "option type is :" + option.getType() + " " + option.getType().equals(ParamInfo.Type.HELP));

            if (!option.getType().equals(ParamInfo.Type.HELP) && option.getValue().length() > 0) {
                parString = parString.append(option.getName() + " = " + option.getValue() + "\n");
            }

        }
        SeadasLogger.getLogger().info("parString: " + parString);
        return parString.toString();
    }

    public Process executeProcess() throws IOException {

        SeadasLogger.getLogger().info("Executing processor " + getProgramName() + "...");

        return Runtime.getRuntime().exec(getProgramCmdArray(), getProgramEnv(), getProgramRoot());
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
        EventInfo eventInfo = getEventInfo(propertyName);
        if (eventInfo == null) {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        } else {
            eventInfo.addPropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        EventInfo eventInfo = getEventInfo(propertyName);
        if (eventInfo == null) {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        } else {
            eventInfo.removePropertyChangeListener(listener);
        }
    }

    public void disableEvent(String name) {
        EventInfo eventInfo = getEventInfo(name);
        if (eventInfo == null) {
            debug("disableEvent - eventInfo not found for " + name);
        } else {
            eventInfo.setEnabled(false);
        }
    }

    public void enableEvent(String name) {
        EventInfo eventInfo = getEventInfo(name);
        if (eventInfo == null) {
            debug("enableEvent - eventInfo not found for " + name);
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
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, name, oldValue, newValue));
        } else {
            eventInfo.fireEvent(oldValue, newValue);
        }
    }

    public void fireAllParamEvents() {
        for (ParamInfo paramInfo : paramList) {
            if (paramInfo.getName() != null && !paramInfo.getName().toLowerCase().equals("none")) {
                fireEvent(paramInfo.getName());
            }
        }
    }

    private void debug(String string) {

        //  System.out.println(string);
    }

    public void setProperty(String property) {
        //changeSupport.firePropertyChange("property", this.property, this.property=property);
    }

}
