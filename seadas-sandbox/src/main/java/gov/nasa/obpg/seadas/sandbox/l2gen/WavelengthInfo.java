package gov.nasa.obpg.seadas.sandbox.l2gen;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class WavelengthInfo {

    public static final int VISIBLE_UPPER_LIMIT = 3000;
    private int wavelength = -1;
    private boolean isSelected = false;

    // applicable only to the global wavelengths not the product specific ones
    private boolean isPartiallySelected = false;


    public WavelengthInfo(int wavelength) {
        this.wavelength = wavelength;
    }

    public WavelengthInfo(String wavelength) {
        if (wavelength != null) {
            this.wavelength = Integer.parseInt(wavelength);
        }
    }

    public int getWavelength() {
        return wavelength;
    }

    public void setWavelength(int wavelength) {
        this.wavelength = wavelength;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String toString() {
        return Integer.toString(wavelength);
    }

    public boolean isVisible() {
        if (wavelength < VISIBLE_UPPER_LIMIT) {
            return true;
        }

        return false;
    }

    public boolean isIR() {
        if (wavelength >= VISIBLE_UPPER_LIMIT) {
            return true;
        }
        return false;
    }
}
