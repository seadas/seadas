package gov.nasa.gsfc.seadas.processing.core;

import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 6/6/12
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SeaDASProcessorModel {
    public String getParamValue(String name);
    public void setParamValue(String name, String value);
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
    public boolean isValidIfile();
    public boolean isGeofileRequired();
    public boolean isWavelengthRequired();
    public String getPrimaryInputFileOptionName();
    public String getPrimaryOutputFileOptionName();
    public boolean isMultipleInputFiles();
    public void updateParamValues(File selectedFile);
    public String getImplicitInputFileExtensions();
}
