package gov.nasa.gsfc.seadas.processing.l2gen.productData;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genWavelengthInfo extends L2genBaseInfo {

    public static final int INFRARED_LOWER_LIMIT = 3000;
    public static final int VISIBLE_UPPER_LIMIT = 725;

    public static final int NULL_WAVELENGTH = -1;

    private int wavelength = NULL_WAVELENGTH;


    public static enum WaveType {
        VISIBLE, INFRARED, NEAR_INFRARED, NULL
    }


    public L2genWavelengthInfo(int wavelength, L2genAlgorithmInfo algorithmInfo) {
        super(Integer.toString(wavelength), algorithmInfo);
        this.wavelength = wavelength;
    }

    public L2genWavelengthInfo(int wavelength) {
        this(wavelength, null);
    }

    public L2genWavelengthInfo(String wavelengthStr) {
        super(wavelengthStr);
        try {
            this.wavelength = Integer.parseInt(wavelengthStr);
        } catch (Exception e) {
            this.wavelength = NULL_WAVELENGTH;
        }
    }

    public L2genAlgorithmInfo getAlgorithmInfo() {
        return (L2genAlgorithmInfo) getParent();
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

        L2genBaseInfo aInfo = getParent();

        if (aInfo != null) {
            String prefix = ((L2genAlgorithmInfo) aInfo).getPrefix();
            String suffix = ((L2genAlgorithmInfo) aInfo).getSuffix();

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


    public boolean isWaveType(WaveType waveType) {
        if (waveType == WaveType.INFRARED
                && wavelength >= INFRARED_LOWER_LIMIT) {
            return true;

        } else if (waveType == WaveType.VISIBLE &&
                wavelength <= VISIBLE_UPPER_LIMIT) {
            return true;
        } else if (waveType == WaveType.NEAR_INFRARED &&
                wavelength > VISIBLE_UPPER_LIMIT && wavelength < INFRARED_LOWER_LIMIT) {
            return true;
        } else {
            return false;
        }
    }


        public WaveType getWaveType() {
        if ( wavelength >= INFRARED_LOWER_LIMIT) {
            return WaveType.INFRARED;
        } else if (  wavelength <= VISIBLE_UPPER_LIMIT) {
            return WaveType.VISIBLE;
        } else if (wavelength > VISIBLE_UPPER_LIMIT && wavelength < INFRARED_LOWER_LIMIT) {
            return WaveType.NEAR_INFRARED;
        } else {
            return WaveType.NULL;
        }
    }
}
