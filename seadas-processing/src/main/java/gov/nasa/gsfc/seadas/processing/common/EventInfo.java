package gov.nasa.gsfc.seadas.processing.common;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class EventInfo {

    private String name;
    private int enabledCount = 0;

    private boolean pending = false;
    Object sourceObject;
    private SwingPropertyChangeSupport propertyChangeSupport;

    public EventInfo(String name, Object sourceObject) {
        this.name = name;
        this.sourceObject = sourceObject;
        propertyChangeSupport = new SwingPropertyChangeSupport(sourceObject);
    }


    public void setEnabled(boolean enabled) {
        if (enabled) {
            enabledCount--;
        } else {
            enabledCount++;
        }

        if (pending) {
            fireEvent();
        }
    }

    public boolean isEnabled() {

        if (enabledCount == 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getName() {
        return name;
    }



    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void fireEvent(Object oldValue, Object newValue) {
        if (!isEnabled()) {
          //  System.out.println("Setting pending event fire - " + name);
            pending = true;
        } else {
            pending = false;
           // System.out.println("Actually Firing event - " + name);
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(sourceObject, name, oldValue, newValue));
        }
    }

    public void fireEvent() {
        fireEvent(null, null);
    }




    public String toString() {
        return name;
    }

    public int getEnabledCount() {
        return enabledCount;
    }


}
