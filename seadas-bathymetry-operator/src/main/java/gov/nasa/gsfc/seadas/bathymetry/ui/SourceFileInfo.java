package gov.nasa.gsfc.seadas.bathymetry.ui;

import gov.nasa.gsfc.seadas.bathymetry.operator.BathymetryOp;
import gov.nasa.gsfc.seadas.bathymetry.util.ResourceInstallationUtils;

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
    private File file;
    private boolean status;
    private String statusMessage;


    public SourceFileInfo(int resolution, Unit unit, File file) {
        setUnit(unit);
        setResolution(resolution);
        setFile(file);
        setDescription();
    }

//    public SourceFileInfo(int resolution, Unit unit, String filename) {
//        setUnit(unit);
//        setResolution(resolution);
//        setFile(filename);
//        setDescription();
//    }


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



        if (isEnabled()) {
            String core = "Filename=" + getExistingFile();
            this.description = "<html>" + core + "</html>";
        } else {
            String core =  "Filename=" + getAltFile();
            this.description = "<html>" + core + "<br> NOTE: this file is not currently installed -- see help</html>";
        }
    }


    public File getFile() {

        if (file == null) {
            return null;
        }

        return file;
    }


    public File getExistingFile() {
        if (getFile() == null) {
            return null;
        }

        if (getFile().exists()) {
            return getFile();
        }

        if (getAltFile() != null && getAltFile().exists()) {
            return getAltFile();
        }

        return null;
    }

    private void setFile(File file) {
        this.file = file;

        setStatus(true, null);
    }



    public File getAltFile() {
        File altFile = ResourceInstallationUtils.getTargetFile(file.getName());
        return altFile;
    }

    public boolean isEnabled() {

        if (getExistingFile() == null) {
            return false;
        }

        return getStatus();

    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setStatus(boolean status, String statusMessage) {
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
        if (file == null) {
            return "";
        }

        return file.getName();
    }
}
