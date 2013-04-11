package gov.nasa.gsfc.seadas.watermask.ui;

import gov.nasa.gsfc.seadas.watermask.operator.WatermaskClassifier;
import gov.nasa.gsfc.seadas.watermask.util.ResourceInstallationUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 9/5/12
 * Time: 9:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class SourceFileInfo {




    public enum Unit {
        METER("m"),
        KILOMETER("km");

        private Unit(String name) {
            this.name = name;
        }

        private final String name;

        public String toString() {
            return name;
        }
    }


    private int resolution;
    private Unit unit;
    private String description;
    private WatermaskClassifier.Mode mode;
    private File file;
    private boolean status;
    private String statusMessage;


    public SourceFileInfo(int resolution, Unit unit, WatermaskClassifier.Mode mode, String filename) {
        setUnit(unit);
        setResolution(resolution);
        setMode(mode);
        setFile(filename);
        setDescription();
    }


    public int getResolution() {
        return resolution;
    }

    public int getResolution(Unit unit) {
        // resolution is returned in units of meters
        if (unit == getUnit()) {
            return resolution;
        } else if (unit == Unit.METER && getUnit() == Unit.KILOMETER) {
            return resolution * 1000;
        } else if (unit == Unit.KILOMETER && getUnit() == Unit.METER) {
            float x = resolution / 1000;
            return Math.round(x);
        }

        return resolution;
    }

    private void setResolution(int resolution) {
        this.resolution = resolution;
    }

    public Unit getUnit() {
        return unit;
    }

    private void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    private void setDescription() {

        String core = "Filename=" + getFile().getAbsolutePath() +"<br>Uses the " + Integer.toString(getResolution()) + " " + getUnit().toString() +
                " dataset obtained from the<br> "
                + getMode().getDescription();

        if (isEnabled()) {
            this.description = "<html>" + core + "</html>";
        } else {
            this.description = "<html>" + core + "<br> NOTE: this file is not currently installed -- see help</html>";
        }
    }


    public WatermaskClassifier.Mode getMode() {
        return mode;
    }

    private void setMode(WatermaskClassifier.Mode mode) {
        this.mode = mode;
    }


    public File getFile() {
        return file;
    }

    private void setFile(String filename) {

        try{
            file = ResourceInstallationUtils.installAuxdata(WatermaskClassifier.class, filename);
            setStatus(true, null);
        } catch (IOException e) {
            setStatus(false, e.getMessage());
        }

    }


    public boolean isEnabled() {

        if (getMode() == WatermaskClassifier.Mode.SRTM_GC) {
            File gcFile = ResourceInstallationUtils.getTargetFile(WatermaskClassifier.GC_WATER_MASK_FILE);
            return gcFile.exists() && file.exists() && getStatus();
        } else {
            return file.exists() && getStatus();
        }
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setStatus(boolean status,String statusMessage) {
        this.status = status;
        this.statusMessage = statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }


    public String getStatusMessage() {
        return statusMessage;
    }


    public boolean getStatus() {
        return status;
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

//        StringBuilder resolutionStringBuilder = new StringBuilder(Integer.toString(getResolution()));
//
//        while (resolutionStringBuilder.length() < 5) {
//            resolutionStringBuilder.insert(0, " ");
//        }
//
//        stringBuilder.append(resolutionStringBuilder.toString());

        if (resolution >= 1000) {
            stringBuilder.append(String.valueOf(resolution / 1000));
            stringBuilder.append(" ");
            stringBuilder.append(Unit.KILOMETER.toString());
        } else {
            stringBuilder.append(Integer.toString(getResolution()));
            stringBuilder.append(" ");
            stringBuilder.append(getUnit().toString());
        }



        stringBuilder.append(" (");
        stringBuilder.append(getMode().toString());
        stringBuilder.append(")");

        return stringBuilder.toString();
    }
}
