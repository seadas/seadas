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
    private boolean defaultSelected = false;
    private AlgorithmInfo algorithmInfo = null;
    private boolean toStringShowProductName = false;

    public static final int NULL_WAVELENGTH = -1;

    // applicable only to the global wavelengths not the product specific ones
    private boolean isPartiallySelected = false;

    public WavelengthInfo(int wavelength, AlgorithmInfo algorithmInfo) {
        this.wavelength = wavelength;
        this.algorithmInfo = algorithmInfo;
    }

    public WavelengthInfo() {

    }

    public WavelengthInfo(int wavelength) {
        this.wavelength = wavelength;
    }

    public WavelengthInfo(String wavelength) {
        if (wavelength == null) {
            this.wavelength = NULL_WAVELENGTH;
        } else {
            this.wavelength = Integer.parseInt(wavelength);
        }
    }

    public int getWavelength() {
        return wavelength;
    }


    public void setWavelength(int wavelength) {
        this.wavelength = wavelength;
    }

    public String getWavelengthString() {
        return Integer.toString(wavelength);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }


    public boolean isVisible() {
        if (wavelength >= 0 && wavelength < VISIBLE_UPPER_LIMIT) {
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

    public AlgorithmInfo getAlgorithmInfo() {
        return algorithmInfo;
    }

    public void setAlgorithmInfo(AlgorithmInfo algorithmInfo) {
        this.algorithmInfo = algorithmInfo;
    }


    public String toString() {

        if (toStringShowProductName == true) {
            StringBuilder myStringBuilder = new StringBuilder("");

            myStringBuilder.append(algorithmInfo.getProductName());

            if (wavelength != NULL_WAVELENGTH) {
                myStringBuilder.append("_");
                myStringBuilder.append(Integer.toString(wavelength));
            }

            if (algorithmInfo.getName() != null) {
                myStringBuilder.append("_");
                myStringBuilder.append(algorithmInfo.getName());
            }

            return myStringBuilder.toString();

        } else {
            return Integer.toString(wavelength);
        }


    }

    public boolean isToStringShowProductName() {
        return toStringShowProductName;
    }

    public void setToStringShowProductName(boolean toStringShowProductName) {
        this.toStringShowProductName = toStringShowProductName;
    }

    public boolean isDefaultSelected() {
        return defaultSelected;
    }

    public void setDefaultSelected(boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }
}
