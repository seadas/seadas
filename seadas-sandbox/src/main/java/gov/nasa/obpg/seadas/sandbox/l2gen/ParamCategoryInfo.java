package gov.nasa.obpg.seadas.sandbox.l2gen;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class ParamCategoryInfo implements Comparable {

    private String name = null;
    private boolean visible = false;
    private boolean defaultBucket = false;

    private ArrayList<String> paramNames = new ArrayList<String>();
    private ArrayList<ParamInfo> paramInfos = new ArrayList<ParamInfo>();

    public ParamCategoryInfo(String name, boolean visible) {
        this.name = name;
        this.visible = visible;
    }

    public ParamCategoryInfo(String name) {
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

    public ArrayList<ParamInfo> getParamInfos() {
        return paramInfos;
    }


    public void addParamName(String name) {
        paramNames.add(name);
    }

    public void clearParamNames() {
        paramNames.clear();
    }

    public void addParamOptionsInfos(ParamInfo paramInfo) {
        paramInfos.add(paramInfo);
    }

    public void clearParamOptionsInfos() {
        paramInfos.clear();
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
        Collections.sort(paramInfos);
    }


    @Override
    public int compareTo(Object o) {
        return getName().compareToIgnoreCase(((ParamCategoryInfo) o).getName());
    }
}
