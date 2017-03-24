package gov.nasa.gsfc.seadas.processing.common;

import com.bc.ceres.binding.Property;
import com.bc.ceres.binding.PropertyContainer;
import com.bc.ceres.swing.TableLayout;
import com.bc.ceres.swing.binding.BindingContext;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 5/3/12
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class LonLat2PixLineUI {

    private String north;
    private ProcessorModel pm;
    ArrayList paramList;

    public LonLat2PixLineUI(ProcessorModel pm) {
        north = "north";
        this.pm = pm;
        paramList = pm.getProgramParamList();

    }

    private void createUI() {

        JPanel lonlatPanel = new JPanel();
        TableLayout lonlatLayout = new TableLayout(2);
        lonlatPanel.setLayout(lonlatLayout);
        lonlatPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));



        Iterator itr = paramList.iterator();
        while (itr.hasNext()) {
            final ParamInfo pi = (ParamInfo) itr.next();
             lonlatPanel.add(makeOptionField(pi));
        }

    }

    private JPanel makeOptionField(ParamInfo pi) {

        final JPanel optionPanel = new JPanel();
        optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
        optionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionPanel.add(new JLabel(pi.getName()));


        final PropertyContainer vc = new PropertyContainer();
        vc.addProperty(Property.create(pi.getName(), pi.getValue()));
        vc.getDescriptor(pi.getName()).setDisplayName(pi.getName());

        final BindingContext ctx = new BindingContext(vc);
        final NumberFormatter formatter = new NumberFormatter(new DecimalFormat("#00.00#"));
        formatter.setValueClass(Double.class); // to ensure that double values are returned
        final JFormattedTextField field = new JFormattedTextField(formatter);
        field.setColumns(4);
        field.setHorizontalAlignment(JFormattedTextField.LEFT);
        field.setFocusLostBehavior(0);
        ctx.bind(pi.getName(), field);

        optionPanel.add(field);

        return optionPanel;

    }

}
