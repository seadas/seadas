package gov.nasa.gsfc.seadas.processing.common;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ParamUtils;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.esa.snap.rcp.util.Dialogs;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/9/15
 * Time: 2:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtractorParamUI extends ParamUIFactory {

    private final String NON_INTERGER_VARS = "prod_list";
    public ExtractorParamUI(ProcessorModel pm) {
        super(pm);
    }

    @Override
    protected JPanel makeOptionField(final ParamInfo pi) {

        final String optionName = pi.getName();
        final JPanel optionPanel = new JPanel();
        optionPanel.setName(optionName);
        TableLayout fieldLayout = new TableLayout(1);
        fieldLayout.setTableFill(TableLayout.Fill.HORIZONTAL);
        optionPanel.setLayout(fieldLayout);
        optionPanel.setName(optionName);
        optionPanel.add(new JLabel(ParamUtils.removePreceedingDashes(optionName)));


        if (pi.getValue() == null || pi.getValue().length() == 0) {
            if (pi.getDefaultValue() != null) {
                processorModel.updateParamInfo(pi, pi.getDefaultValue());
            }
        }

        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(optionName, pi.getValue()));
        vc.getDescriptor(optionName).setDisplayName(optionName);
        final BindingContext ctx = new BindingContext(vc);
        final JTextField field = new JTextField();
        field.setColumns(8);
        field.setPreferredSize(field.getPreferredSize());
        field.setMaximumSize(field.getPreferredSize());
        field.setMinimumSize(field.getPreferredSize());
        field.setName(pi.getName());

        if (pi.getDescription() != null) {
            field.setToolTipText(pi.getDescription().replaceAll("\\s+", " "));
        }
        ctx.bind(optionName, field);

        ctx.addPropertyChangeListener(optionName, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                String value = field.getText();
                if (!field.getText().trim().equals(pi.getValue().trim())) {
                    if (NON_INTERGER_VARS.contains(pi.getName()) || new Double(field.getText()).doubleValue() > 0 ) {
                        processorModel.updateParamInfo(pi, field.getText());
                    } else {
                        Dialogs.showError("Please enter a value greater than zero!");
                        field.setText(" ");
                    }
                }
            }
        });

        processorModel.addPropertyChangeListener(pi.getName(), new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if (! field.getText().trim().equals(pi.getValue().trim()))
                field.setText(pi.getValue());
            }
        });

        optionPanel.add(field);

        return optionPanel;
    }
}
