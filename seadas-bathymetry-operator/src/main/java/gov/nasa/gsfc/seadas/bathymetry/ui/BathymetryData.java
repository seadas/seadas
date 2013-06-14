package gov.nasa.gsfc.seadas.bathymetry.ui;

import gov.nasa.gsfc.seadas.bathymetry.operator.WatermaskClassifier;

import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/4/12
 * Time: 9:13 AM
 * To change this template use File | Settings | File Templates.
 */
class BathymetryData {

    BathymetryData bathymetryData = this;

    public static String NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT = "NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT";
    public static String FILE_INSTALLED_EVENT2 = "FILE_INSTALLED_EVENT2";
    public static String PROMPT_REQUEST_TO_INSTALL_FILE_EVENT = "REQUEST_TO_INSTALL_FILE_EVENT";
    public static String CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT = "CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT";

    public static String LANDMASK_URL =  "http://oceandata.sci.gsfc.nasa.gov/SeaDAS/installer/landmask";

    private boolean createMasks = false;
    private boolean deleteMasks = false;

    private double maskTransparency = 0.0;
    private boolean showMaskAllBands = true;
    private Color maskColor = new Color(0, 0, 255);
    private String maskName = "Bathymetry";
    private String maskMath = getWaterFractionSmoothedName() + " > 25 and " + getWaterFractionSmoothedName() + " < 75";
    private String maskDescription = "Bathymetry pixels";

    private double maskMinDepth = 0;
    private double maskMaxDepth = -10923;
//    private double maskMinDepth = 75;
//    private double maskMaxDepth = 25;


    private int superSampling = 1;

    private double landMaskTransparency = 0.0;
    private double waterMaskTransparency = 0.5;

    private boolean showLandMaskAllBands = false;
    private boolean showWaterMaskAllBands = false;

    private Color landMaskColor = new Color(100, 49, 12);
    private Color waterMaskColor = new Color(0, 0, 255);

    private String waterFractionBandName = "mask_data_water_fraction";
    private String waterFractionSmoothedName = "mask_data_water_fraction_smoothed";

    private String landMaskName = "LandMask";
    private String landMaskMath = getWaterFractionBandName() + " == 0";
    private String landMaskDescription = "Land pixels";

    private String waterMaskName = "WaterMask";
    private String waterMaskMath = getWaterFractionBandName() + " > 0";
    private String waterMaskDescription = "Water pixels";




    private ArrayList<SourceFileInfo> sourceFileInfos = new ArrayList<SourceFileInfo>();
    private SourceFileInfo sourceFileInfo;

    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    public BathymetryData() {

        SourceFileInfo sourceFileInfo;

        sourceFileInfo = new SourceFileInfo(WatermaskClassifier.RESOLUTION_50m,
                SourceFileInfo.Unit.METER,
                WatermaskClassifier.Mode.SRTM_GC,
                WatermaskClassifier.FILENAME_SRTM_GC_50m);
        getSourceFileInfos().add(sourceFileInfo);

        sourceFileInfo = new SourceFileInfo(WatermaskClassifier.RESOLUTION_150m,
                SourceFileInfo.Unit.METER,
                WatermaskClassifier.Mode.SRTM_GC,
                WatermaskClassifier.FILENAME_SRTM_GC_150m);
        getSourceFileInfos().add(sourceFileInfo);



        sourceFileInfo = new SourceFileInfo(WatermaskClassifier.RESOLUTION_1km,
                SourceFileInfo.Unit.METER,
                WatermaskClassifier.Mode.GSHHS,
                WatermaskClassifier.FILENAME_GSHHS_1km);
        getSourceFileInfos().add(sourceFileInfo);
        // set the default
        this.sourceFileInfo = sourceFileInfo;

        sourceFileInfo = new SourceFileInfo(WatermaskClassifier.RESOLUTION_10km,
                SourceFileInfo.Unit.METER,
                WatermaskClassifier.Mode.GSHHS,
                WatermaskClassifier.FILENAME_GSHHS_10km);
        getSourceFileInfos().add(sourceFileInfo);

        this.addPropertyChangeListener(BathymetryData.NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SourceFileInfo sourceFileInfo = (SourceFileInfo) evt.getNewValue();

                InstallResolutionFileDialog dialog = new InstallResolutionFileDialog(bathymetryData, sourceFileInfo, InstallResolutionFileDialog.Step.CONFIRMATION);
                dialog.setVisible(true);
                dialog.setEnabled(true);
            }
        });
    }


    public boolean isCreateMasks() {
        return createMasks;
    }

    public void setCreateMasks(boolean closeClicked) {
        this.createMasks = closeClicked;
    }

    public double getLandMaskTransparency() {
        return landMaskTransparency;
    }

    public void setLandMaskTransparency(double landMaskTransparency) {
        this.landMaskTransparency = landMaskTransparency;
    }

    public double getWaterMaskTransparency() {
        return waterMaskTransparency;
    }

    public void setWaterMaskTransparency(double waterMaskTransparency) {
        this.waterMaskTransparency = waterMaskTransparency;
    }

    public double getMaskTransparency() {
        return maskTransparency;
    }

    public void setMaskTransparency(double maskTransparency) {
        this.maskTransparency = maskTransparency;
    }

    public boolean isShowLandMaskAllBands() {
        return showLandMaskAllBands;
    }

    public void setShowLandMaskAllBands(boolean showLandMaskAllBands) {
        this.showLandMaskAllBands = showLandMaskAllBands;
    }

    public boolean isShowWaterMaskAllBands() {
        return showWaterMaskAllBands;
    }

    public void setShowWaterMaskAllBands(boolean showWaterMaskAllBands) {
        this.showWaterMaskAllBands = showWaterMaskAllBands;
    }

    public boolean isShowMaskAllBands() {
        return showMaskAllBands;
    }

    public void setShowMaskAllBands(boolean showMaskAllBands) {
        this.showMaskAllBands = showMaskAllBands;
    }

    public Color getLandMaskColor() {
        return landMaskColor;
    }

    public void setLandMaskColor(Color landMaskColor) {
        this.landMaskColor = landMaskColor;
    }

    public Color getWaterMaskColor() {
        return waterMaskColor;
    }

    public void setWaterMaskColor(Color waterMaskColor) {
        this.waterMaskColor = waterMaskColor;
    }

    public Color getMaskColor() {
        return maskColor;
    }

    public void setMaskColor(Color maskColor) {
        this.maskColor = maskColor;
    }

    public int getSuperSampling() {
        return superSampling;
    }

    public void setSuperSampling(int superSampling) {
        this.superSampling = superSampling;
    }

    public SourceFileInfo getSourceFileInfo() {
        return sourceFileInfo;
    }

    public void setSourceFileInfo(SourceFileInfo resolution) {
        this.sourceFileInfo = resolution;
    }

    public String getWaterFractionBandName() {
        return waterFractionBandName;
    }

    public void setWaterFractionBandName(String waterFractionBandName) {
        this.waterFractionBandName = waterFractionBandName;
    }

    public String getWaterFractionSmoothedName() {
        return waterFractionSmoothedName;
    }

    public void setWaterFractionSmoothedName(String waterFractionSmoothedName) {
        this.waterFractionSmoothedName = waterFractionSmoothedName;
    }

    public String getLandMaskName() {
        return landMaskName;
    }

    public void setLandMaskName(String landMaskName) {
        this.landMaskName = landMaskName;
    }

    public String getLandMaskMath() {
        return landMaskMath;
    }

    public void setLandMaskMath(String landMaskMath) {
        this.landMaskMath = landMaskMath;
    }

    public String getLandMaskDescription() {
        return landMaskDescription;
    }

    public void setLandMaskDescription(String landMaskDescription) {
        this.landMaskDescription = landMaskDescription;
    }

    public String getMaskName() {
        return maskName;
    }

    public void setMaskName(String maskName) {
        this.maskName = maskName;
    }

    public String getMaskMath() {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getWaterFractionSmoothedName());
        stringBuilder.append(" > ");
        stringBuilder.append(new Double(getMaskMaxDepth()).toString());
        stringBuilder.append(" and ");
        stringBuilder.append(getWaterFractionSmoothedName());
        stringBuilder.append(" < ");
        stringBuilder.append(new Double(getMaskMinDepth()).toString());

        return stringBuilder.toString();
   //     return maskMath;
    }

    public void setMaskMath(String maskMath) {
        this.maskMath = maskMath;
    }

    public String getMaskDescription() {
        return maskDescription;
    }

    public void setMaskDescription(String maskDescription) {
        this.maskDescription = maskDescription;
    }

    public String getWaterMaskName() {
        return waterMaskName;
    }

    public void setWaterMaskName(String waterMaskName) {
        this.waterMaskName = waterMaskName;
    }

    public String getWaterMaskMath() {
        return waterMaskMath;
    }

    public void setWaterMaskMath(String waterMaskMath) {
        this.waterMaskMath = waterMaskMath;
    }

    public String getWaterMaskDescription() {
        return waterMaskDescription;
    }

    public void setWaterMaskDescription(String waterMaskDescription) {
        this.waterMaskDescription = waterMaskDescription;
    }

    public boolean isDeleteMasks() {
        return deleteMasks;
    }

    public void setDeleteMasks(boolean deleteMasks) {
        this.deleteMasks = deleteMasks;
    }

    public ArrayList<SourceFileInfo> getSourceFileInfos() {
        return sourceFileInfos;
    }

    public void setSourceFileInfos(ArrayList<SourceFileInfo> sourceFileInfos) {
        this.sourceFileInfos = sourceFileInfos;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public void fireEvent(String propertyName) {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, propertyName, null, null));
    }

    public void fireEvent(String propertyName, SourceFileInfo oldValue, SourceFileInfo newValue) {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }


    public double getMaskMinDepth() {
        return maskMinDepth;
    }

    public String getMaskMinDepthString() {
        return new Double(maskMinDepth).toString();
    }

    public void setMaskMinDepth(double maskMinDepth) {
        this.maskMinDepth = maskMinDepth;
    }

    public void setMaskMinDepth(String maskMinDepth) {
        this.maskMinDepth = Double.parseDouble(maskMinDepth);
    }


    public double getMaskMaxDepth() {
        return maskMaxDepth;
    }


    public String getMaskMaxDepthString() {
        return new Double(maskMaxDepth).toString();
    }

    public void setMaskMaxDepth(double maskMaxDepth) {
        this.maskMaxDepth = maskMaxDepth;
    }

    public void setMaskMaxDepth(String maskMaxDepth) {
        this.maskMaxDepth = Double.parseDouble(maskMaxDepth);
    }
}


