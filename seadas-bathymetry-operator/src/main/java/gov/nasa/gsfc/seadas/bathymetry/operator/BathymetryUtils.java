package gov.nasa.gsfc.seadas.bathymetry.operator;

public class BathymetryUtils {

    private BathymetryUtils() {
    }

    /**
     * Computes the side length of the images to be generated for the given resolution.
     *
     * @param resolution The resolution.
     *
     * @return The side length of the images to be generated.
     */
    public static int computeSideLength(int resolution) {
        final int pixelXCount = 40024000 / resolution;
        final int pixelXCountPerTile = pixelXCount / 360;
        // these two lines needed to create a multiple of 8
        final int temp = pixelXCountPerTile / 8;
        return temp * 8;
    }

    /**
     * Creates the name of the img file for the given latitude and longitude.
     *
     * @param lat latitude in degree
     * @param lon longitude in degree
     *
     * @return the name of the img file
     */
    public static String createImgFileName(float lat, float lon) {
        final boolean geoPosIsWest = lon < 0;
        final boolean geoPosIsSouth = lat < 0;
        StringBuilder result = new StringBuilder();
        final String eastOrWest = geoPosIsWest ? "w" : "e";
        result.append(eastOrWest);
        int positiveLon = (int) Math.abs(Math.floor(lon));
        if (positiveLon >= 10 && positiveLon < 100) {
            result.append("0");
        } else if (positiveLon < 10) {
            result.append("00");
        }
        result.append(positiveLon);

        final String northOrSouth = geoPosIsSouth ? "s" : "n";
        result.append(northOrSouth);

        final int positiveLat = (int) Math.abs(Math.floor(lat));
        if (positiveLat < 10) {
            result.append("0");
        }
        result.append(positiveLat);
        result.append(".img");

        return result.toString();
    }
}
