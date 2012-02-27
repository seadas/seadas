package gov.nasa.obpg.seadas.sandbox.l2gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class ParamOptionsInfo implements Comparable {

    private String name = null;
    private String value = null;
    private Type type = null;
    private String defaultValue = null;
    private String description = null;
    private String source = null;

    private ArrayList<ParamValidValueInfo> validValueInfos = new ArrayList<ParamValidValueInfo>();

    public static enum Type {
        BOOLEAN, STRING, INT, FLOAT
    }

    public ParamOptionsInfo(String name, String value, Type type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ArrayList<ParamValidValueInfo> getValidValueInfos() {
        return validValueInfos;
    }

    public void setValidValueInfos(ArrayList<ParamValidValueInfo> validValueInfos) {
        this.validValueInfos = validValueInfos;
    }

    public void addValidValueInfo(ParamValidValueInfo paramValidValueInfo) {
        this.validValueInfos.add(paramValidValueInfo);
    }



    public void sortValidValueInfos() {
      //  Collections.sort(validValueInfos, new ParamValidValueInfo.ValueComparator());
        Collections.sort(validValueInfos, ParamValidValueInfo.SORT_BY_VALUE);
    }


//    public static final Comparator<ParamOptionsInfo> CASE_SENSITIVE_ORDER
//            = new CaseSensitiveComparator();
//
//    public static final Comparator<ParamOptionsInfo> CASE_INSENSITIVE_ORDER
//            = new CaseInsensitiveComparator();
//
//
//    private static class CaseSensitiveComparator
//            implements Comparator<ParamOptionsInfo> {
//
//        public int compare(ParamOptionsInfo s1, ParamOptionsInfo s2) {
//            return s1.getName().compareTo(s2.getName());
//        }
//    }
//
//    private static class CaseInsensitiveComparator
//            implements Comparator<ParamOptionsInfo> {
//
//        public int compare(ParamOptionsInfo s1, ParamOptionsInfo s2) {
//            return s1.getName().compareToIgnoreCase(s2.getName());
//        }
//    }

    public static final Comparator<ParamOptionsInfo> SORT_BY_NAME
            = new NameComparator();

        @Override
    public int compareTo(Object o) {
        return getName().compareToIgnoreCase(((ParamOptionsInfo)o).getName());
    }

    public static class NameComparator implements Comparator<ParamOptionsInfo> {

        public int compare(ParamOptionsInfo s1, ParamOptionsInfo s2) {
            return s1.getName().compareToIgnoreCase(s2.getName());
        }
    }
}
