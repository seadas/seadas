package gov.nasa.gsfc.seadas.processing.general;

import org.esa.beam.framework.datamodel.Product;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/10/12
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface CloProgramUI {

    public ProcessorModel getProcessorModel();
    public Product getSelectedSourceProduct();


}
