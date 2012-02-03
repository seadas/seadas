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
    private boolean toStringShowProductName = false;

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
        String productStr = "";
        String algorithmStr = "";
        String wavelengthStr = "";
// todo use prefix and suffix to make name

  //      this.getAlgorithmInfo().getPrefix()



        BaseInfo algorithmInfo = getParent();
        if (algorithmInfo == null) {
            return getName();
        }
        algorithmStr = algorithmInfo.getName();
        if (algorithmStr == null) {
            algorithmStr = "";
        }
        if (wavelength == NULL_WAVELENGTH) {
            wavelengthStr = null;
        } else {
            wavelengthStr = getName();
        }
        if (wavelengthStr == null) {
            return algorithmInfo.getFullName();
        }

        BaseInfo productInfo = algorithmInfo.getParent();
        if (productInfo != null) {
            productStr = productInfo.getName();
            if (productStr == null) {
                productStr = "";
            }
        }

        StringBuilder result = new StringBuilder();
        if (!productStr.isEmpty()) {
            result.append(productStr);
        }
        if (!wavelengthStr.isEmpty()) {
//            if (result.length() > 0) {
//                result.append("_");
//            }
            result.append(wavelengthStr);
        }
        if (!algorithmStr.isEmpty()) {
//            if (result.length() > 0) {
//                result.append("_");
//            }
            result.append(algorithmStr);
        }
        return result.toString();
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

    @Override
    public String toString() {
        if (toStringShowProductName == true) {
            return getFullName();
        } else {
            return getName();
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

    @Override
    public boolean isWavelengthDependent() {
        if(wavelength != NULL_WAVELENGTH) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isWavelengthIndependent() {
        if(wavelength == NULL_WAVELENGTH) {
            return true;
        }
        return false;
    }


}
