package gov.nasa.gsfc.seadas.processing.core;

import gov.nasa.gsfc.seadas.processing.processor.MultlevelProcessorForm;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dshea
 * Date: 9/4/12
 * Time: 2:34 PM
 * <p/>
 * Processor model for the Seadas Processor script
 */
public class MultiParamList extends ParamList {
    public static final String MAIN_KEY = MultlevelProcessorForm.Processor.MAIN.toString();

    private HashMap<String, ParamList> paramLists;

    public MultiParamList() {
        paramLists = new HashMap<String, ParamList>();
    }

    public void addParamList(String listName, ParamList list) {
        if(listName != null && !listName.equals(MAIN_KEY)) {
            paramLists.put(listName, list);
        } else {
            super.set(list);
        }
    }

    public ParamList getParamList(String listName) {
        if (listName != null && listName.equals(MAIN_KEY)) {
            return this;
        }
        return paramLists.get(listName);
    }

    public void addInfo(String listName, ParamInfo param) {
        ParamList list = getParamList(listName);
        if(list != null) {
            list.addInfo(param);
        }
    }

    public ParamInfo getInfo(String listName, String name) {
        ParamList list = getParamList(listName);
        if(list != null) {
            return list.getInfo(name);
        }
        return null;
    }

    public ParamInfo removeInfo(String listName, String name) {
        ParamList list = getParamList(listName);
        if(list != null) {
            return list.removeInfo(name);
        }
        return null;
    }

    public String getParamValue(String listName, String name) {
        ParamList list = getParamList(listName);
        if (list != null) {
            return list.getValue(name);
        }
        return null;
    }

    public boolean isValueTrue(String listName, String name) {
        ParamList list = getParamList(listName);
        if (list != null) {
            return list.isValueTrue(name);
        }
        return false;
    }

    public boolean setParamValue(String listName, String name, String value) {
        ParamList list = getParamList(listName);
        if (list != null) {
            return list.setValue(name, value);
        }
        return false;
    }

    public void addPropertyChangeListener(String listName, String name, PropertyChangeListener listener) {
        ParamList list = getParamList(listName);
        if (list != null) {
            list.addPropertyChangeListener(name, listener);
        }
    }

    public void removePropertyChangeListener(String listName, String name, PropertyChangeListener listener) {
        ParamList list = getParamList(listName);
        if (list != null) {
            list.removePropertyChangeListener(name, listener);
        }
    }

    public SwingPropertyChangeSupport getPropertyChangeSupport(String listName) {
        ParamList list = getParamList(listName);
        if (list != null) {
            return list.getPropertyChangeSupport();
        }
        return null;
    }

    public void appendPropertyChangeSupport(String listName, SwingPropertyChangeSupport propertyChangeSupport) {
        ParamList list = getParamList(listName);
        if (list != null) {
            list.appendPropertyChangeSupport(propertyChangeSupport);
        }
    }

    public void clearPropertyChangeSupport(String listName) {
        ParamList list = getParamList(listName);
        if (list != null) {
            list.clearPropertyChangeSupport();
        }
    }

    public Object clone() {
        MultiParamList result = (MultiParamList) super.clone();
        result.paramLists = new HashMap<String, ParamList>();

        for(Map.Entry<String, ParamList> entry : paramLists.entrySet()) {
            result.addParamList(entry.getKey(), (ParamList)entry.getValue().clone());
        }
        return result;
    }

    @Override
    public String getParamString(String separator) {

        StringBuilder sb = new StringBuilder();
        ParamList list;

        // let's put main at the top
        sb.append("[").append("main").append("]");
        sb.append(separator);
        sb.append(super.getParamString(separator));

        // loop through all the param lists
        for (Map.Entry<String, ParamList> entry : paramLists.entrySet()) {
            list = entry.getValue();
            if (list != null && !list.isDefault()) {
                sb.append(separator);
                sb.append("\n[").append(entry.getKey()).append("]");
                sb.append(separator);
                sb.append(list.getParamString(separator));
            }
        }
        return sb.toString();
    }

}
