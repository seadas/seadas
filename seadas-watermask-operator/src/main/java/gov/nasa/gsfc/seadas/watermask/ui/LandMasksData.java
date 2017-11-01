package gov.nasa.gsfc.seadas.watermask.ui;

import gov.nasa.gsfc.seadas.watermask.operator.WatermaskClassifier;

import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
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
class LandMasksData {

    LandMasksData landMasksData = this;

    public static String NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT = "NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT";
    public static String FILE_INSTALLED_EVENT2 = "FILE_INSTALLED_EVENT2";
    public static String PROMPT_REQUEST_TO_INSTALL_FILE_EVENT = "REQUEST_TO_INSTALL_FILE_EVENT";
    public static String CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT = "CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT";

    public static String LANDMASK_URL =  "https://oceandata.sci.gsfc.nasa.gov/SeaDAS/installer/landmask";

    private boolean createMasks = false;
    private boolean deleteMasks = false;

    private int superSampling = 3;
    private int coastalGridSize = 3;
    private int coastalSizeTolerance = 50;


    private double landMaskTransparency = 0.0;
    private double waterMaskTransparency = 0.5;
    private double coastlineMaskTransparency = 0.0;

    private boolean showLandMaskAllBands = true;
    private boolean showWaterMaskAllBands = false;
    private boolean showCoastlineMaskAllBands = false;

    private Color landMaskColor = new Color(51, 51, 51);
    private Color waterMaskColor = new Color(0, 125, 255);
    private Color coastlineMaskColor = new Color(0, 0, 0);


    private String waterFractionBandName = "water_fraction";
    private String waterFractionSmoothedName = "water_fraction_mean";

    private String landMaskName = "LandMask";
    private String landMaskMath = getWaterFractionBandName() + " == 0";
    private String landMaskDescription = "Land masked pixels";


    private String coastlineMaskName = "CoastalMask";
   // private String coastlineMath = getWaterFractionSmoothedName() + " > 25 and " + getWaterFractionSmoothedName() + " < 75";
//    private String coastlineMath = getWaterFractionSmoothedName() + " > 0 and " + getWaterFractionSmoothedName() + " < 100";
    private String coastlineMaskDescription = "Coastline masked pixels";


    private String waterMaskName = "WaterMask";
    private String waterMaskMath = getWaterFractionBandName() + " > 0";
    private String waterMaskDescription = "Water masked pixels";


    private ArrayList<SourceFileInfo> sourceFileInfos = new ArrayList<SourceFileInfo>();
    private SourceFileInfo sourceFileInfo;

    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);

    public LandMasksData() {

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


//        sourceFileInfo = new SourceFileInfo(WatermaskClassifier.RESOLUTION_50m,
//                SourceFileInfo.Unit.METER,
//                WatermaskClassifier.Mode.DEFAULT,
//                WatermaskClassifier.FILENAME_SRTM_GC_50m);
//        getSourceFileInfos().add(sourceFileInfo);
//
//        sourceFileInfo = new SourceFileInfo(WatermaskClassifier.RESOLUTION_150m,
//                SourceFileInfo.Unit.METER,
//                WatermaskClassifier.Mode.DEFAULT,
//                WatermaskClassifier.FILENAME_SRTM_GC_150m);
//        getSourceFileInfos().add(sourceFileInfo);
//
//
//        sourceFileInfo = new SourceFileInfo(WatermaskClassifier.RESOLUTION_1km,
//                SourceFileInfo.Unit.METER,
//                WatermaskClassifier.Mode.DEFAULT,
//                WatermaskClassifier.FILENAME_GSHHS_1km);
//        getSourceFileInfos().add(sourceFileInfo);
//        // set the default
//        this.sourceFileInfo = sourceFileInfo;
//
//        sourceFileInfo = new SourceFileInfo(WatermaskClassifier.RESOLUTION_10km,
//                SourceFileInfo.Unit.METER,
//                WatermaskClassifier.Mode.DEFAULT,
//                WatermaskClassifier.FILENAME_GSHHS_10km);
//        getSourceFileInfos().add(sourceFileInfo);


        this.addPropertyChangeListener(LandMasksData.NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SourceFileInfo sourceFileInfo = (SourceFileInfo) evt.getNewValue();

                InstallResolutionFileDialog dialog = new InstallResolutionFileDialog(landMasksData, sourceFileInfo, InstallResolutionFileDialog.Step.CONFIRMATION);
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

    public double getCoastlineMaskTransparency() {
        return coastlineMaskTransparency;
    }

    public void setCoastlineMaskTransparency(double coastlineMaskTransparency) {
        this.coastlineMaskTransparency = coastlineMaskTransparency;
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

    public boolean isShowCoastlineMaskAllBands() {
        return showCoastlineMaskAllBands;
    }

    public void setShowCoastlineMaskAllBands(boolean showCoastlineMaskAllBands) {
        this.showCoastlineMaskAllBands = showCoastlineMaskAllBands;
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

    public Color getCoastlineMaskColor() {
        return coastlineMaskColor;
    }

    public void setCoastlineMaskColor(Color coastlineMaskColor) {
        this.coastlineMaskColor = coastlineMaskColor;
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

        return waterFractionSmoothedName + getCoastalGridSize();
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

    public String getCoastlineMaskName() {
        return coastlineMaskName;
    }

    public void setCoastlineMaskName(String coastlineMaskName) {
        this.coastlineMaskName = coastlineMaskName;
    }

    public String getCoastalMath() {

        double min = 50  - getCoastalSizeTolerance()/2;
        double max = 50 + getCoastalSizeTolerance()/2;
      return  getWaterFractionSmoothedName() + " > "+ Double.toString(min) + " and " + getWaterFractionSmoothedName() + " < " + Double.toString(max);
    }

//    public void setCoastlineMath(String coastlineMath) {
//        this.coastlineMath = coastlineMath;
//    }

    public String getCoastlineMaskDescription() {
        return coastlineMaskDescription;
    }

    public void setCoastlineMaskDescription(String coastlineMaskDescription) {
        this.coastlineMaskDescription = coastlineMaskDescription;
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


    public int getCoastalGridSize() {
        return coastalGridSize;
    }

    public void setCoastalGridSize(int coastalGridSize) {
        this.coastalGridSize = coastalGridSize;
    }

    public int getCoastalSizeTolerance() {
        return coastalSizeTolerance;
    }

    public void setCoastalSizeTolerance(int coastalSizeTolerance) {
        this.coastalSizeTolerance = coastalSizeTolerance;
    }
}


