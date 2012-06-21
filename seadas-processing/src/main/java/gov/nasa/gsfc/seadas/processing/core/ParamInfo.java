package gov.nasa.gsfc.seadas.processing.core;


import gov.nasa.gsfc.seadas.processing.general.FileInfo;
import gov.nasa.gsfc.seadas.processing.general.FileInfoNew;
import gov.nasa.gsfc.seadas.processing.general.FileTypeInfo;
import gov.nasa.gsfc.seadas.processing.general.SeadasProcessorInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class ParamInfo implements Comparable {

    private String name = NULL_STRING;
    private String value = NULL_STRING;
    private Type type = Type.STRING;
    private String defaultValue = NULL_STRING;
    private String description = NULL_STRING;
    private String source = NULL_STRING;
    private boolean isBit = false;
    private int order;


    private String validationComment = null;

    public static final String PARAM_TYPE_IFILE = "ifile";
    public static final String PARAM_TYPE_OFILE = "ofile";
    public static final String PARAM_TYPE_HELP = "help";
    public static final String PARAM_TYPE_STRING = "string";
    public static final String PARAM_TYPE_FLOAT = "float";
    public static final String PARAM_TYPE_INT = "int";
    public static final String PARAM_TYPE_BOOLEAN = "boolean";

    private ArrayList<ParamValidValueInfo> validValueInfos = new ArrayList<ParamValidValueInfo>();

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
        if (getValidationComment() == null) {
            return true;
        } else {
            return false;
        }
    }


    public static enum Type {
        BOOLEAN, STRING, INT, FLOAT, IFILE, OFILE, HELP
    }

    public static String NULL_STRING = "";
    public static String BOOLEAN_TRUE = "1";
    public static String BOOLEAN_FALSE = "0";

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


    public File getFile(File rootDir) {
        if (type == Type.IFILE && value != null && value.length() > 0) {
            File file = new File(value);
            if (file != null) {
                if (file.isAbsolute()) {
                    return file;
                } else if (!file.isAbsolute() && rootDir != null) {
                    File fileWithRootDir = new File(rootDir.getAbsolutePath(), value);
                    return fileWithRootDir;
                }
            }
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
        if (getValue().equals(getDefaultValue())) {
            return true;
        } else {
            return false;
        }
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

        if ((intParamValue & intParamValidValue) > 0) {
            return true;
        } else {
            return false;
        }
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
        if (validValueInfos.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isBit() {
        return isBit;
    }

    protected void setBit(boolean bit) {
        isBit = bit;
    }




    public FileInfoNew validateIfileValue(String defaultFileParent, SeadasProcessorInfo.Id processorInfoId) {
        clearValidationComment();

        FileInfoNew fileInfo = null;

        if (getType() == ParamInfo.Type.IFILE) {

            if (getName().equals(L2genData.IFILE) || getName().equals(L2genData.GEOFILE)) {

                fileInfo = new FileInfoNew(defaultFileParent, getValue(), true);
                if (fileInfo.getFile() != null) {
                    if (fileInfo.getFile().exists()) {
                        if (getName().equals(L2genData.GEOFILE)) {
                            if (!fileInfo.isTypeId(FileTypeInfo.Id.GEO)) {
                                setValidationComment("WARNING!!! File '" + fileInfo.getFile().getAbsolutePath() + "' is not a GEO file");
                            }
                        } else if (getName().equals(L2genData.IFILE)) {
                            if (!SeadasProcessorInfo.isSupportedMission(fileInfo, processorInfoId)) {
                                setValidationComment("# WARNING!!! file " + getValue() + " is not a valid input mission" + "\n");
                            } else if (!SeadasProcessorInfo.isValidFileType(fileInfo, processorInfoId)) {
                                setValidationComment("# WARNING!!! file " + getValue() + " is not a valid input file type" + "\n");
                            }
                        }
                    } else {
                        setValidationComment("WARNING!!! File'" + fileInfo.getFile().getAbsolutePath() + "' does not exist");
                    }

                } else {
                    setValidationComment("WARNING!!! File'" + getValue() + "' does not exist");
                }
            } else {
                fileInfo = new FileInfoNew(defaultFileParent, getValue(), false);
                if (fileInfo.getFile() != null && !fileInfo.getFile().exists()) {
                    setValidationComment("WARNING!!! File '" + fileInfo.getFile().getAbsolutePath() + "' does not exist");
                }
            }
        }

        return fileInfo;

    }


    protected void setOrder(int order) {
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
}
