package gov.nasa.obpg.seadas.sandbox.l2gen;

import java.util.Comparator;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class ParamValidValueInfo {

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


    public static final Comparator<ParamValidValueInfo> CASE_SENSITIVE_ORDER
            = new CaseSensitiveComparator();

    public static final Comparator<ParamValidValueInfo> CASE_INSENSITIVE_ORDER
            = new CaseInsensitiveComparator();


    private static class CaseSensitiveComparator
            implements Comparator<ParamValidValueInfo> {

        public int compare(ParamValidValueInfo s1, ParamValidValueInfo s2) {
            return s1.getValue().compareTo(s2.getValue());
        }
    }

    private static class CaseInsensitiveComparator
            implements Comparator<ParamValidValueInfo> {

        public int compare(ParamValidValueInfo s1, ParamValidValueInfo s2) {
            return s1.getValue().compareToIgnoreCase(s2.getValue());
        }
    }
}
