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
public class ParamCategoriesInfo implements Comparable {

    private String name = null;
    private boolean visible = false;
    private boolean defaultBucket = false;

    private ArrayList<String> paramNames = new ArrayList<String>();
    private ArrayList<ParamOptionsInfo> paramOptionsInfos = new ArrayList<ParamOptionsInfo>();

    public ParamCategoriesInfo(String name, boolean visible) {
        this.name = name;
        this.visible = visible;
    }

    public ParamCategoriesInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public ArrayList<String> getParamNames() {
        return paramNames;
    }

    public void setParamNames(ArrayList<String> paramNames) {
        this.paramNames = paramNames;
    }

    public ArrayList<ParamOptionsInfo> getParamOptionsInfos() {
        return paramOptionsInfos;
    }


    public void addParamName(String name) {
        paramNames.add(name);
    }

    public void clearParamNames() {
        paramNames.clear();
    }

    public void addParamOptionsInfos(ParamOptionsInfo paramOptionsInfo) {
        paramOptionsInfos.add(paramOptionsInfo);
    }

    public void clearParamOptionsInfos() {
        paramOptionsInfos.clear();
    }

    public boolean isDefaultBucket() {
        return defaultBucket;
    }

    public void setDefaultBucket(boolean defaultBucket) {
        this.defaultBucket = defaultBucket;
    }

    public void sortParamNameInfos() {
        Collections.sort(paramNames, String.CASE_INSENSITIVE_ORDER);
    }

    public void sortParamOptionsInfos() {
        // Collections.sort(paramOptionsInfos, new ParamOptionsInfo.NameComparator());
        Collections.sort(paramOptionsInfos, ParamOptionsInfo.SORT_BY_NAME);
    }


    public static final Comparator<ParamCategoriesInfo> SORT_BY_NAME
            = new NameComparator();


    public static class NameComparator implements Comparator<ParamCategoriesInfo> {

        public int compare(ParamCategoriesInfo s1, ParamCategoriesInfo s2) {
            return s1.getName().compareToIgnoreCase(s2.getName());
        }
    }

    @Override
    public int compareTo(Object o) {
        return getName().compareToIgnoreCase(((ParamCategoriesInfo) o).getName());
    }
}
