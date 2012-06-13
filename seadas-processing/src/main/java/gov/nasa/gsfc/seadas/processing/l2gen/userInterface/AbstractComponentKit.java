package gov.nasa.gsfc.seadas.processing.l2gen.userInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 6/13/12
 * Time: 11:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class AbstractComponentKit {

    private String name;
    private String toolTip;
    private Component component;

    private JLabel jLabel = new JLabel();

    private boolean controlHandlerEnabled = true;


    public AbstractComponentKit(String name, Component component) {
        addComponent(component);
        setName(name);
    }



    public AbstractComponentKit(String name, String toolTip, Component component) {
        this(name, component);
        setToolTip(toolTip);
    }

    public AbstractComponentKit(String name, String toolTip) {
        setName(name);
        setToolTip(toolTip);
    }


    public void setName(String name) {
        this.name = name;
        jLabel.setText(name);
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
        jLabel.setToolTipText(toolTip);
    }


    public JLabel getjLabel() {
        return jLabel;
    }


    public String getName() {
        return name;
    }

    public String getToolTip() {
        return toolTip;
    }


    public void setEnabled(boolean enabled) {
        if (component != null) {
            component.setEnabled(enabled);
        }
        if (jLabel != null) {
            jLabel.setEnabled(enabled);
        }
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

    public Component getComponent() {
        return component;
    }


    public void addComponent(Component component) {
        this.component = component;
        if (component != null) {
            addControlListeners();
        }
    }

    private void addControlListeners() {

        if (getComponent() instanceof JCheckBox) {
            ((JCheckBox) getComponent()).addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (isControlHandlerEnabled()) {
                        controlHandler();
                    }
                }
            });
        }
    }


    public void controlHandler() {
    }

    public void setComponentSelected(boolean selected, boolean allowEvent) {
        if (allowEvent) {
            if (getComponent() instanceof JCheckBox) {
                ((JCheckBox) getComponent()).setSelected(selected);
            }
        } else {
            disableControlHandler();
            if (getComponent() instanceof JCheckBox) {
                ((JCheckBox) getComponent()).setSelected(selected);
            }
            enableControlHandler();
        }
    }

    public boolean isComponentSelected() {
        if (getComponent() instanceof JCheckBox) {
            return ((JCheckBox) getComponent()).isSelected();
        }
        return false;
    }
}
