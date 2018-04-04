package gov.nasa.gsfc.seadas.processing.core;


import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.common.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A ...
 *
 * @author Danny Knowles
 * @author Aynur Abdurazik
 * @since SeaDAS 7.0
 */
public class ParamInfo implements Comparable, Cloneable {

    public static final String PARAM_TYPE_IFILE = "ifile";
    public static final String PARAM_TYPE_OFILE = "ofile";
    public static final String PARAM_TYPE_HELP = "help";
    public static final String PARAM_TYPE_STRING = "string";
    public static final String PARAM_TYPE_FLOAT = "float";
    public static final String PARAM_TYPE_INT = "int";
    public static final String PARAM_TYPE_BOOLEAN = "boolean";

    public static final String NULL_STRING = "";
    public static final String BOOLEAN_TRUE = "1";
    public static final String BOOLEAN_FALSE = "0";

    public static final String USED_IN_COMMAND_AS_ARGUMENT = "argument";
    public static final String USED_IN_COMMAND_AS_OPTION = "option";
    public static final String USED_IN_COMMAND_AS_FLAG = "flag";

    public static final String[] FILE_COMPRESSION_SUFFIXES = {"bz2", "bzip2", "gz", "gzip", "zip", "tar", "tgz", "z"};

    public static enum Type {
        BOOLEAN, STRING, INT, FLOAT, IFILE, OFILE, HELP, DIR, FLAGS
    }

    private String name = NULL_STRING;
    private String value = NULL_STRING;
    private Type type = Type.STRING;
    private String defaultValue = NULL_STRING;
    private String description = NULL_STRING;
    private String source = NULL_STRING;
    private boolean isBit = false;
    private int order = 0;
    private String validationComment = null;
    private String usedAs = USED_IN_COMMAND_AS_OPTION;

    private ArrayList<ParamValidValueInfo> validValueInfos = new ArrayList<ParamValidValueInfo>();

    public ParamInfo(String name, String value, Type type, String defaultValue) {
        setName(name);
        //setType() should be executed before setValue()!
        setType(type);
        setValue(value);
        setDefaultValue(defaultValue);
    }

    public ParamInfo(String name, String value, Type type) {
        setName(name);
        setValue(value);
        setType(type);
    }

    public ParamInfo(String name, String value) {
        setName(name);
        setValue(value);
    }

    public ParamInfo(String name) {
        setName(name);
    }

    public String getValidationComment() {
        return validationComment;
    }

    private void setValidationComment(String validationComment) {
        this.validationComment = validationComment;
    }

    private void clearValidationComment() {
        this.validationComment = null;
    }

    public boolean isValid() {
        return getValidationComment() == null;
    }

    public String getUsedAs() {
        return usedAs;
    }

    public void setUsedAs(String usedAs) {
        this.usedAs = usedAs;
    }

    public File getFile(File rootDir) {
        if (type == Type.IFILE) {
            return SeadasFileUtils.createFile(rootDir, value);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        // Clean up and handle input exceptions
        if (name == null) {
            this.name = NULL_STRING;
            return;
        }
        name = name.trim();

        if (name.length() == 0) {
            this.name = NULL_STRING;
        } else {
            this.name = name;
        }
    }

    public String getValue() {
        return value;
    }

    protected void setValue(String value) {
        // Clean up and handle input exceptions
        if (value == null) {
            this.value = NULL_STRING;
            return;
        }
        value = value.trim();

        if (value.length() == 0) {
            this.value = NULL_STRING;
        } else if (getType() == Type.BOOLEAN) {
            this.value = getStandardizedBooleanString(value);
        } else if (getType() == Type.IFILE || getType() == Type.OFILE) {
            this.value = value;
        } else {
            this.value = value;
        }
    }

    public static String getStandardizedBooleanString(String booleanString) {

        if (booleanString == null) {
            return NULL_STRING;
        }

        String allowedTrueValues[] = {"1", "t", "true", "y", "yes", "on"};
        String allowedFalseValue[] = {"0", "f", "false", "n", "no", "off"};

        for (String trueValue : allowedTrueValues) {
            if (booleanString.toLowerCase().equals(trueValue)) {
                return BOOLEAN_TRUE;
            }
        }

        for (String falseValue : allowedFalseValue) {
            if (booleanString.toLowerCase().equals(falseValue)) {
                return BOOLEAN_FALSE;
            }
        }

        return NULL_STRING;
    }

    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isDefault() {
        return getValue().equals(getDefaultValue());
    }

    protected void setDefaultValue(String defaultValue) {
        // Clean up and handle input exceptions
        if (defaultValue == null) {
            this.defaultValue = NULL_STRING;
            return;
        }
        defaultValue = defaultValue.trim();

        if (defaultValue.length() == 0) {
            this.defaultValue = NULL_STRING;
        } else if (getType() == Type.BOOLEAN) {
            this.defaultValue = getStandardizedBooleanString(defaultValue);
        } else {
            this.defaultValue = defaultValue;
        }
    }

    public boolean isBitwiseSelected(ParamValidValueInfo paramValidValueInfo) {
        int intParamValue = Integer.parseInt(value);
        int intParamValidValue = Integer.parseInt(paramValidValueInfo.getValue());

        return (intParamValue & intParamValidValue) > 0;
    }

    public String getDescription() {
        return description;
    }

    protected void setDescription(String description) {
        // Clean up and handle input exceptions
        if (description == null) {
            this.description = NULL_STRING;
            return;
        }
        description = description.trim();

        if (description.length() == 0) {
            this.description = NULL_STRING;
        } else {
            this.description = description;
        }
    }

    public String getSource() {
        return source;
    }

    protected void setSource(String source) {
        // Clean up and handle input exceptions
        if (source == null) {
            this.source = NULL_STRING;
            return;
        }
        source = source.trim();

        if (source.length() == 0) {
            this.source = NULL_STRING;
        } else {
            this.source = source;
        }
    }

    public ArrayList<ParamValidValueInfo> getValidValueInfos() {
        return validValueInfos;
    }

    protected void setValidValueInfos(ArrayList<ParamValidValueInfo> validValueInfos) {
        this.validValueInfos = validValueInfos;
    }

    protected void addValidValueInfo(ParamValidValueInfo paramValidValueInfo) {
        this.validValueInfos.add(paramValidValueInfo);
    }

    protected void clearValidValueInfos() {
        this.validValueInfos.clear();
    }

    public boolean hasValidValueInfos() {
        return validValueInfos.size() > 0;
    }

    public boolean isBit() {
        return isBit;
    }

    protected void setBit(boolean bit) {
        isBit = bit;
    }

    public FileInfo validateIfileValue(String defaultFileParent, SeadasProcessorInfo.Id processorInfoId, OCSSW ocssw) {
        clearValidationComment();

        FileInfo fileInfo = null;

        if (getType() == ParamInfo.Type.IFILE) {

            if (getName().equals(L2genData.IFILE) || getName().equals(L2genData.GEOFILE)) {
                String value = SeadasFileUtils.expandEnvironment(getValue());
                fileInfo = new FileInfo(defaultFileParent, value, true, ocssw);
                if (fileInfo.getFile() != null) {
                    if (fileInfo.getFile().exists()) {
                        String filename = fileInfo.getFile().getAbsolutePath();

                        boolean isCompressedFile = false;

                        for (String compressionSuffix : FILE_COMPRESSION_SUFFIXES) {
                            if (filename.toLowerCase().endsWith("." + compressionSuffix)) {
                                isCompressedFile = true;
                            }
                        }
                        if (isCompressedFile) {
                            setValidationComment("WARNING!!! File '" + filename + "' is compressed (please uncompress it)");
                        } else if (getName().equals(L2genData.GEOFILE)) {
                            if (!fileInfo.isTypeId(FileTypeInfo.Id.GEO)) {
                                //todo check geofile validity needs to be done on the server as well
                                setValidationComment("WARNING!!! File '" + filename + "' is not a GEO file");
                            }
                        } else if (getName().equals(L2genData.IFILE)) {
                            if (!SeadasProcessorInfo.isSupportedMission(fileInfo, processorInfoId)) {
                                setValidationComment("# WARNING!!! file " + filename + " is not a valid input mission" + ": Mission="+ fileInfo.getMissionName() + "\n");
                            } else if (!fileInfo.isMissionDirExist()) {
                                File dir = fileInfo.getSubsensorDirectory();
                                if(dir == null) {
                                    dir = fileInfo.getMissionDirectory();
                                }
                                if (dir != null) {
                                    setValidationComment("WARNING!!! Mission directory '" + dir.getAbsolutePath() + "' does not exist");
                                } else {
                                    setValidationComment("WARNING!!! Mission directory does not exist");
                                }
                            } else if (!SeadasProcessorInfo.isValidFileType(fileInfo, processorInfoId)) {
                                setValidationComment("# WARNING!!! file " + filename + " is not a valid input file type" + "\n");
                            }
                        }
                    } else {
                        setValidationComment("WARNING!!! File'" + fileInfo.getFile().getAbsolutePath() + "' does not exist");
                    }
                } else {
                    setValidationComment("WARNING!!! File'" + getValue() + "' does not exist");
                }
            } else {
                fileInfo = new FileInfo(defaultFileParent, getValue(), false, ocssw);
                if (fileInfo.getFile() != null && !fileInfo.getFile().exists()) {
                    setValidationComment("WARNING!!! File '" + fileInfo.getFile().getAbsolutePath() + "' does not exist");
                }
            }
        }

        return fileInfo;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    protected void sortValidValueInfos() {
        //  Collections.sort(validValueInfos, new ParamValidValueInfo.ValueComparator());
        Collections.sort(validValueInfos);
    }

    @Override
    public int compareTo(Object o) {
        return getName().compareToIgnoreCase(((ParamInfo) o).getName());
    }

    @Override
    public Object clone() {
        ParamInfo info = new ParamInfo(name);
        info.value = value;
        info.type = type;
        info.defaultValue = defaultValue;
        info.description = description;
        info.source = source;
        info.isBit = isBit;
        info.order = order;
        info.validationComment = validationComment;
        info.usedAs = usedAs;
        for (ParamValidValueInfo validValueInfo : validValueInfos) {
            info.validValueInfos.add((ParamValidValueInfo) validValueInfo.clone());
        }
        return info;
    }

    // get a string representation of this ParamInfo usable as a param string
    public String getParamString() {
        if (value.length() == 0) {
            return "";
        }
        switch (usedAs) {
            case USED_IN_COMMAND_AS_ARGUMENT:
                return value;
            case USED_IN_COMMAND_AS_FLAG:
                if (isTrue()) {
                    return name;
                } else {
                    return "";
                }
            case USED_IN_COMMAND_AS_OPTION:
                if (value.contains(" ")) {
                    return name + "=\"" + value + "\"";
                } else {
                    return name + "=" + value;
                }
             default:
                 return "";
        }
//        if (usedAs.equals(USED_IN_COMMAND_AS_FLAG)) {
//            if (isTrue()) {
//                return name;
//            } else {
//                return "";
//            }
//        } else if (value.length() > 0){
//            if (value.contains(" ")) {
//                return name + "=\"" + value + "\"";
//            } else {
//                return name + "=" + value;
//            }
    }

    public boolean isTrue() {
        return getStandardizedBooleanString(value).equals(BOOLEAN_TRUE);
    }


}
