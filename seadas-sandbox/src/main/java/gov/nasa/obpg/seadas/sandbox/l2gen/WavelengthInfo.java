package gov.nasa.obpg.seadas.sandbox.l2gen;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class WavelengthInfo {

    private String wavelength = null;
    private boolean isSelected = false;

    public WavelengthInfo() {
    }

    public WavelengthInfo(String wavelength) {
        this.wavelength = wavelength;
        }


    public String getWavelength() {
        return wavelength;
    }

    public void setWavelength(String wavelength) {
        this.wavelength = wavelength;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
