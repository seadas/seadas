package gov.nasa.obpg.seadas.sandbox.l2gen;

import java.util.Comparator;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class ParamValidValueInfo implements Comparable  {

    private String value = null;
    private String description = null;
    private ParamOptionsInfo parent = null;

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

    public void setDescription(String description) {
        this.description = description;
    }

    public ParamOptionsInfo getParent() {
        return parent;
    }

    public void setParent(ParamOptionsInfo parent) {
        this.parent = parent;
    }


    public static final Comparator<ParamValidValueInfo> SORT_BY_VALUE
            = new ValueComparator();


    @Override
    public int compareTo(Object o) {
        return getValue().compareToIgnoreCase(((ParamValidValueInfo)o).getValue());
    }

    public static class ValueComparator implements Comparator<ParamValidValueInfo> {

        public int compare(ParamValidValueInfo s1, ParamValidValueInfo s2) {
            return s1.getValue().compareToIgnoreCase(s2.getValue());
        }
    }
}
