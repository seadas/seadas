package gov.nasa.gsfc.seadas.processing.processor;

import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.general.FileSelector;
import org.esa.beam.visat.VisatApp;

import javax.swing.*;
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
public class ActiveFileSelector {

    private SwingPropertyChangeSupport callersPropertyChangeSupport;
    private SwingPropertyChangeSupport myPropertyChangeSupport;

    private FileSelector fileSelector;
    private String label;
    ParamInfo.Type type;

    private String propertyName;

    boolean controlHandlerEnabled = true;

    ActiveFileSelector(SwingPropertyChangeSupport callersPropertyChangeSupport, final String propertyName, String label, ParamInfo.Type type) {
        this.callersPropertyChangeSupport = callersPropertyChangeSupport;
        this.propertyName = propertyName;
        this.label = label;
        this.type = type;

        myPropertyChangeSupport = new SwingPropertyChangeSupport(this);

        fileSelector = new FileSelector(VisatApp.getApp(), type, label);

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

    private FileSelector getFileSelector() {
        return fileSelector;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPropertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public String getFilename() {
       return getFileSelector().getFileName();
    }

    public JPanel getJPanel() {
        return getFileSelector().getjPanel();
    }

    public void setVisible(boolean visible) {
        getFileSelector().setVisible(visible);
    }

}
