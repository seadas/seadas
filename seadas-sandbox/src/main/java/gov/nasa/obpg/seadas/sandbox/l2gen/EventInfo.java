package gov.nasa.obpg.seadas.sandbox.l2gen;

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
    private boolean enabled = false;
    private boolean pending = false;
    Object sourceObject;
    private SwingPropertyChangeSupport propertyChangeSupport;

    public EventInfo(String name, Object sourceObject) {
        this.name = name;
        this.sourceObject = sourceObject;
        propertyChangeSupport = new SwingPropertyChangeSupport(sourceObject);
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled && pending) {
            fireEvent();
        }
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void fireEvent(Object oldValue, Object newValue) {
        if (enabled != true) {
            pending = true;
        } else {
            pending = false;
            System.out.println("Firing event" + name);
            propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(sourceObject, name, oldValue, newValue));
        }
    }

    public void fireEvent() {
        fireEvent(null, null);
    }




    public String toString() {
        return name;
    }

}
