package gov.nasa.gsfc.seadas.processing.l2gen;

import gov.nasa.gsfc.seadas.processing.core.ParamInfo;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class ParamValidValueInfo implements Comparable {

    private String value = null;
    private String description = null;
    private ParamInfo parent = null;



    public ParamValidValueInfo(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public String getShortDescription(int maxLength) {
        if (description != null && description.length() > maxLength) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(description.substring(0,maxLength-1)).append(" ...");
         return stringBuilder.toString();
        } else {
        return description;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ParamInfo getParent() {
        return parent;
    }

    public void setParent(ParamInfo parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(Object object) {
        return getValue().compareToIgnoreCase(((ParamValidValueInfo) object).getValue());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");

        if (value != null) {
            stringBuilder.append(value);

            if (description != null) {
                stringBuilder.append(" - " + getShortDescription(70));
            }
        }

        return stringBuilder.toString();
    }

//    public static final Comparator<ParamValidValueInfo> SORT_BY_VALUE
//            = new ValueComparator();
//
//
//    public static class ValueComparator implements Comparator<ParamValidValueInfo> {
//
//        public int compare(ParamValidValueInfo s1, ParamValidValueInfo s2) {
//            return s1.getValue().compareToIgnoreCase(s2.getValue());
//        }
//    }
}
