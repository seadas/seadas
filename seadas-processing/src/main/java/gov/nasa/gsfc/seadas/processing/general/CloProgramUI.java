package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import org.esa.beam.framework.datamodel.Product;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/10/12
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CloProgramUI {
    public JPanel getParamPanel();
    public ProcessorModel getProcessorModel();
    public Product getSelectedSourceProduct();
    public boolean isOpenOutputInApp();
    public String getParamString();
    public void setParamString(String paramString);
}
