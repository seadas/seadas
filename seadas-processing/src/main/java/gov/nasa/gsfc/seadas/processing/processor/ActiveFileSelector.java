package gov.nasa.gsfc.seadas.processing.processor;

import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.common.FileSelector;

import javax.swing.*;
import javax.swing.event.SwingPropertyChangeSupport;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.esa.snap.rcp.SnapApp;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 7/12/13
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActiveFileSelector {

    private SwingPropertyChangeSupport externalPropertyChangeSupport;
    private SwingPropertyChangeSupport thisPropertyChangeSupport;

    private FileSelector fileSelector;
    private String label;
    private ParamInfo.Type type;
    private boolean allowReFireOnChange = false;

    private String propertyName;

    boolean controlHandlerEnabled = true;

    ActiveFileSelector(SwingPropertyChangeSupport externalPropertyChangeSupport, final String propertyName, String label, ParamInfo.Type type) {
        this.externalPropertyChangeSupport = externalPropertyChangeSupport;
        this.propertyName = propertyName;
        this.label = label;
        this.type = type;

        thisPropertyChangeSupport = new SwingPropertyChangeSupport(this);

        fileSelector = new FileSelector(SnapApp.getDefault().getAppContext(), type, label);

        addControlListeners();
        addEventListeners();
    }


    private void addControlListeners() {
        getFileSelector().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (isControlHandlerEnabled()) {
                    thisPropertyChangeSupport.firePropertyChange(propertyName, null, getFileSelector().getFileName());
                }
            }
        });
    }

    private void addEventListeners() {
        externalPropertyChangeSupport.addPropertyChangeListener(propertyName, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (!allowReFireOnChange) {
                    disableControlHandler();
                }
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
        thisPropertyChangeSupport.addPropertyChangeListener(propertyName, listener);
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
