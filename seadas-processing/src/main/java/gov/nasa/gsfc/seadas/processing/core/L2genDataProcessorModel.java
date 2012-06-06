package gov.nasa.gsfc.seadas.processing.core;

import java.beans.PropertyChangeListener;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/6/12
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public interface L2genDataProcessorModel {
    public String getParamValue(String name);
    public void setParamValue(String name, String value);
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
    public boolean isValidIfile();
    public boolean isRequiresGeofile();
}
