package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.l2gen.ParamInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/12/12
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParamSelectorUI extends JPanel  {

    private ParamInfo[] paramOptions;

    public ParamSelectorUI(ArrayList paramList) {
        paramOptions  = (ParamInfo[]) paramList.toArray();
    }

    public void setParamOptions(ArrayList paramList) {
        paramOptions  = (ParamInfo[]) paramList.toArray();

    }

    public ArrayList getParamOptions(){
        return new ArrayList<ParamInfo>(Arrays.asList(paramOptions));
    }

    protected JPanel getSelectedParams() {

        final JPanel allParamsPanel = new JPanel();
        allParamsPanel.setLayout(new FlowLayout());

        for (int i = 0; i < paramOptions.length; i++ ){

            final JPanel paramPanel;

            switch (paramOptions[i].getType()) {
                case BOOLEAN :
                    paramPanel = createIntParamPanel( paramOptions[i] );
                    break;

                case INT     :
                    paramPanel = createIntParamPanel( paramOptions[i] );
                    break;

                case STRING  :
                    paramPanel = createIntParamPanel( paramOptions[i] );
                    break;

                case FLOAT    :
                    paramPanel = createIntParamPanel( paramOptions[i] );
                    break;

                default      :
                    paramPanel = null;
                    break;

            }

            allParamsPanel.add(paramPanel);
        }
        return allParamsPanel;
    }

    private JPanel createIntParamPanel(ParamInfo  paramOption){
        final JPanel paramPanel = new JPanel();
        paramPanel.setLayout(new FlowLayout());
        paramPanel.add(new JLabel(paramOption.getName()));

        JFormattedTextField intParam = new JFormattedTextField();
        intParam.setText(paramOption.getValue());
        intParam.addActionListener( getParamValues() );

        paramPanel.add(intParam);

        return paramPanel;
    }

    private ActionListener getParamValues(){
        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {

            }
        };

        return al;
    }
}
