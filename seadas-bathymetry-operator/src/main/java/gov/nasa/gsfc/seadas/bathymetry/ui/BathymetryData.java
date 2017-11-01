package gov.nasa.gsfc.seadas.bathymetry.ui;

import com.bc.ceres.core.runtime.RuntimeContext;
import gov.nasa.gsfc.seadas.OCSSWInfo;
import gov.nasa.gsfc.seadas.bathymetry.operator.BathymetryOp;
import gov.nasa.gsfc.seadas.bathymetry.util.ResourceInstallationUtils;
import gov.nasa.gsfc.seadas.processing.common.SeadasLogger;


import javax.swing.event.SwingPropertyChangeSupport;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/4/12
 * Time: 9:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class BathymetryData {

    BathymetryData bathymetryData = this;

    public static final int RESOLUTION_BATHYMETRY_FILE = 1855;
    public static final String FILENAME_BATHYMETRY = "ETOPO1_ocssw.nc";

    public static String NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT = "NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT";
    public static String FILE_INSTALLED_EVENT2 = "FILE_INSTALLED_EVENT2";
    public static String PROMPT_REQUEST_TO_INSTALL_FILE_EVENT = "REQUEST_TO_INSTALL_FILE_EVENT";
    public static String CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT = "CONFIRMED_REQUEST_TO_INSTALL_FILE_EVENT";

    public static String LANDMASK_URL = "https://oceandata.sci.gsfc.nasa.gov/SeaDAS/installer/landmask";

    private boolean createMasks = false;
    private boolean deleteMasks = false;

    private boolean isInstallingFile = false;

    private double maskTransparency = 0.7;
    private boolean showMaskAllBands = false;
    private Color maskColor = new Color(0, 0, 255);
    private String maskName = "BATHYMETRY_MASK";
    private String maskDescription = "Bathymetry pixels";

    private double maskMinDepth = 0;
    private double maskMaxDepth = 10923;


    public static final String OCSSWROOT_ENVVAR = "OCSSWROOT";
    public static final String OCSSWROOT_PROPERTY = "ocssw.root";


    private int superSampling = 1;


    //   private String bathymetryBandName = "elevation";


    private ArrayList<SourceFileInfo> sourceFileInfos = new ArrayList<SourceFileInfo>();
    private SourceFileInfo sourceFileInfo;

    private final SwingPropertyChangeSupport propertyChangeSupport = new SwingPropertyChangeSupport(this);


    public BathymetryData() {

        SourceFileInfo sourceFileInfo;

        File bathymetryFile = getBathymetryFile(FILENAME_BATHYMETRY);
//        File ocsswRootDir = getOcsswRoot();
//        File ocsswRunDir = new File(ocsswRootDir, "run");
//        File ocsswRunDataDir = new File(ocsswRunDir, "data");
//        File ocsswRunDataCommonDir = new File(ocsswRunDataDir, "common");
//        File bathymetryFile = new File(ocsswRunDataCommonDir, FILENAME_BATHYMETRY);


        sourceFileInfo = new SourceFileInfo(RESOLUTION_BATHYMETRY_FILE,
                SourceFileInfo.Unit.METER,
                bathymetryFile);
        getSourceFileInfos().add(sourceFileInfo);
        // set the default
        this.sourceFileInfo = sourceFileInfo;


        this.addPropertyChangeListener(BathymetryData.NOTIFY_USER_FILE_INSTALL_RESULTS_EVENT, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SourceFileInfo sourceFileInfo = (SourceFileInfo) evt.getNewValue();

                InstallBathymetryFileDialog dialog = new InstallBathymetryFileDialog(bathymetryData, sourceFileInfo, InstallBathymetryFileDialog.Step.CONFIRMATION);
                dialog.setVisible(true);
                dialog.setEnabled(true);
            }
        });
    }


    public static File getOcsswRoot() {
        String test = System.getenv(OCSSWROOT_ENVVAR);
        if (test != null && test.length() > 1) {
            return new File(RuntimeContext.getConfig().getContextProperty(OCSSWROOT_PROPERTY, System.getenv(OCSSWROOT_ENVVAR)));
        }

        return null;
    }


    public boolean isCreateMasks() {
        return createMasks;
    }

    public void setCreateMasks(boolean closeClicked) {
        this.createMasks = closeClicked;
    }


    public double getMaskTransparency() {
        return maskTransparency;
    }

    public void setMaskTransparency(double maskTransparency) {
        this.maskTransparency = maskTransparency;
    }


    public boolean isShowMaskAllBands() {
        return showMaskAllBands;
    }

    public void setShowMaskAllBands(boolean showMaskAllBands) {
        this.showMaskAllBands = showMaskAllBands;
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


//    public String getBathymetryBandName() {
//        return bathymetryBandName;
//    }
//
//    public void setBathymetryBandName(String bathymetryBandName) {
//        this.bathymetryBandName = bathymetryBandName;
//    }


    public String getMaskName() {
        return maskName;
    }

    public void setMaskName(String maskName) {
        this.maskName = maskName;
    }

    public String getMaskMath() {

//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(BathymetryOp.ELEVATION_BAND_NAME);
//        stringBuilder.append(" >= ");
//        stringBuilder.append(new Double(-getMaskMaxDepth()).toString());
//        stringBuilder.append(" and ");
//        stringBuilder.append(BathymetryOp.ELEVATION_BAND_NAME);
//        stringBuilder.append(" <= ");
//        stringBuilder.append(new Double(-getMaskMinDepth()).toString());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BathymetryOp.BATHYMETRY_BAND_NAME);
        stringBuilder.append(" >= ");
        stringBuilder.append(new Double(getMaskMinDepth()).toString());
        stringBuilder.append(" and ");
        stringBuilder.append(BathymetryOp.BATHYMETRY_BAND_NAME);
        stringBuilder.append(" <= ");
        stringBuilder.append(new Double(getMaskMaxDepth()).toString());

        return stringBuilder.toString();
    }


    public String getMaskDescription() {
        return maskDescription;
    }

    public void setMaskDescription(String maskDescription) {
        this.maskDescription = maskDescription;
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


    static public File getBathymetryFile(String bathymetryFilename) {

         //  File ocsswRootDir = getOcsswRoot();
        //todo Danny commented this out to skip OCSSWROOT and use .seadas for file location until we figure this out
        if (1 == 2) {
            File ocsswRootDir = null;
            try {
                ocsswRootDir = new File(OCSSWInfo.getInstance().getOcsswRoot());
            } catch (Exception e) {
                SeadasLogger.getLogger().warning("ocssw root not found, will try to use alternate source for bathymetry");
            }

            if (ocsswRootDir != null && ocsswRootDir.exists()) {
                File ocsswRunDir = new File(ocsswRootDir, "run");
                if (ocsswRootDir.exists()) {
                    File ocsswRunDataDir = new File(ocsswRunDir, "data");
                    if (ocsswRunDataDir.exists()) {
                        File ocsswRunDataCommonDir = new File(ocsswRunDataDir, "common");
                        if (ocsswRunDataCommonDir.exists()) {
                            File bathymetryFile = new File(ocsswRunDataCommonDir, bathymetryFilename);
                            if (bathymetryFile.exists()) {
                                return bathymetryFile;
                            }
                        }
                    }
                }
            }
        }

        File bathymetryFile = ResourceInstallationUtils.getTargetFile(bathymetryFilename);
      //  if (bathymetryFile.exists()) {
            return bathymetryFile;
      //  }

      //  return null;
    }

    public boolean isInstallingFile() {
        return isInstallingFile;
    }

    public void setInstallingFile(boolean installingFile) {
        isInstallingFile = installingFile;
    }
}


