package gov.nasa.gsfc.seadas.processing.processor;

import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.general.FileSelector;
import org.esa.beam.visat.VisatApp;

import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 7/12/13
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class ODirSelector {

    private SwingPropertyChangeSupport callersPropertyChangeSupport;
    private SwingPropertyChangeSupport myPropertyChangeSupport;

    private FileSelector odirSelector;
    private String label;

    private String propertyName;

    boolean controlHandlerEnabled = true;

    ODirSelector(SwingPropertyChangeSupport callersPropertyChangeSupport, final String propertyName, String label) {
        this.callersPropertyChangeSupport = callersPropertyChangeSupport;
        this.propertyName = propertyName;
        this.label = label;

        myPropertyChangeSupport = new SwingPropertyChangeSupport(this);

        odirSelector = new FileSelector(VisatApp.getApp(), ParamInfo.Type.DIR, label);

        addControlListeners();
        addEventListeners();
    }


    private void addControlListeners() {
        getFileSelector().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (isControlHandlerEnabled()) {
                    myPropertyChangeSupport.firePropertyChange(propertyName, null, getFileSelector().getFileName());
                }
            }
        });
    }

    private void addEventListeners() {
        callersPropertyChangeSupport.addPropertyChangeListener(propertyName, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                disableControlHandler();
                getFileSelector().setFilename((String) evt.getNewValue());
                enableControlHandler();
            }
        });
    }

    private boolean isControlHandlerEnabled() {
        return controlHandlerEnabled;
    }

    private void enableControlHandler() {
        controlHandlerEnabled = true;
    }

    private void disableControlHandler() {
        controlHandlerEnabled = false;
    }

    public FileSelector getFileSelector() {
        return odirSelector;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPropertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public String getFilename() {
       return getFileSelector().getFileName();
    }
}
