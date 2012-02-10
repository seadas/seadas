package gov.nasa.obpg.seadas.sandbox.l2gen;

import java.util.ArrayList;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class WavelengthInfo extends BaseInfo {

    public static final int VISIBLE_UPPER_LIMIT = 3000;
    public static final int NULL_WAVELENGTH = -1;

    private int wavelength = NULL_WAVELENGTH;
    private boolean defaultSelected = false;

    public WavelengthInfo(int wavelength, AlgorithmInfo algorithmInfo) {
        super(Integer.toString(wavelength), algorithmInfo);
        this.wavelength = wavelength;
    }

    public WavelengthInfo(int wavelength) {
        this(wavelength, null);
    }

    public WavelengthInfo(String wavelengthStr) {
        super(wavelengthStr);
        try {
            this.wavelength = Integer.parseInt(wavelengthStr);
        } catch (Exception e) {
            this.wavelength = NULL_WAVELENGTH;
        }
    }

    public int getWavelength() {
        return wavelength;
    }

    public void setWavelength(int wavelength) {
        this.wavelength = wavelength;
        setName(Integer.toString(wavelength));
    }

    public String getWavelengthString() {
        return Integer.toString(wavelength);
    }

    @Override
    public String getFullName() {

        StringBuilder result = new StringBuilder();

        BaseInfo aInfo = getParent();

        if (aInfo != null) {
            String prefix = ((AlgorithmInfo) aInfo).getPrefix();
            String suffix = ((AlgorithmInfo) aInfo).getSuffix();

            if (prefix != null && !prefix.isEmpty()) {
                result.append(prefix);
            }

            if (wavelength != NULL_WAVELENGTH) {
                result.append(getName());
            }

            if (suffix != null && !suffix.isEmpty()) {
                result.append(suffix);
            }
        }

        return result.toString().replaceAll("[_]+", "_");
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
        return (AlgorithmInfo) getParent();
    }



    public boolean isDefaultSelected() {
        return defaultSelected;
    }

    public void setDefaultSelected(boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    @Override
    public boolean isWavelengthDependent() {
        if (wavelength != NULL_WAVELENGTH) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isWavelengthIndependent() {
        if (wavelength == NULL_WAVELENGTH) {
            return true;
        }
        return false;
    }


}
