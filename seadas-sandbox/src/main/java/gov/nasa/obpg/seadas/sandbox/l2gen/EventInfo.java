package gov.nasa.obpg.seadas.sandbox.l2gen;

import java.beans.PropertyChangeEvent;

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
    L2genData l2genData;

    public EventInfo(String name, L2genData l2genData) {
        this.name = name;
        this.l2genData = l2genData;
    }

    public String toString() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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

    public void fire() {
        if (enabled != true) {
            pending = true;
        } else {
            pending = false;
            l2genData.getPropertyChangeSupport().firePropertyChange(new PropertyChangeEvent(l2genData, name, null, null));
        }
    }


    public void setName(String name) {
        this.name = name;
    }
}
