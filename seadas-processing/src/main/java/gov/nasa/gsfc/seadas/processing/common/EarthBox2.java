package gov.nasa.gsfc.seadas.processing.common;


import org.esa.snap.core.datamodel.GeoPos;
import ucar.ma2.Array;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 10/30/13
 * Time: 11:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class EarthBox2 {

    public static final float NULL_COORDINATE = (float) -999;

    private float minLat = NULL_COORDINATE;
    private float maxLat = NULL_COORDINATE;
    private float minLon = NULL_COORDINATE;
    private float maxLon = NULL_COORDINATE;

    float deltaLat = NULL_COORDINATE;
    float deltaLon = NULL_COORDINATE;

    private int latDimensionLength = 0;
    private int lonDimensionLength = 0;

    private short[][] values;

    public EarthBox2() {

    }

    private void setDeltaLon() {
        if (getMinLon() != NULL_COORDINATE) {
            deltaLon = (getMaxLon() - getMinLon()) / getLonDimensionLength();
        }
    }

    private void setDeltaLat() {
        if (getMinLat() != NULL_COORDINATE) {
            deltaLat = (getMaxLat() - getMinLat()) / getLatDimensionLength();
        }
    }

    private float getDeltaLat() {
        if (deltaLat == NULL_COORDINATE) {
            setDeltaLat();
        }

        return deltaLat;
    }

    private float getDeltaLon() {
        if (deltaLon == NULL_COORDINATE) {
            setDeltaLon();
        }

        return deltaLon;
    }

    private int getLatIndex(float lat) {
        int latIndex = (int) Math.round((lat - getMinLat()) / getDeltaLat());
        if (latIndex > getLatDimensionLength() - 1) {
            latIndex = getLatDimensionLength() - 1;
        }

        if (latIndex < 0) {
            latIndex = 0;
        }
        return latIndex;
    }

    private int getLonIndex(float lon) {
        int lonIndex = (int) Math.round((lon - getMinLon()) / getDeltaLon());
        if (lonIndex > getLonDimensionLength() - 1) {
            lonIndex = getLonDimensionLength() - 1;
        }

        if (lonIndex < 0) {
            lonIndex = 0;
        }
        return lonIndex;
    }

    public float getMinLat() {
        return minLat;
    }

    private void setMinLat(float minLat) {
        this.minLat = minLat;
        this.deltaLat = NULL_COORDINATE;
    }

    public float getMaxLat() {
        return maxLat;
    }

    private void setMaxLat(float maxLat) {
        this.maxLat = maxLat;
        this.deltaLat = NULL_COORDINATE;
    }

    public float getMinLon() {
        return minLon;
    }

    private void setMinLon(float minLon) {
        this.minLon = minLon;
        this.deltaLon = NULL_COORDINATE;
    }

    public float getMaxLon() {
        return maxLon;
    }

    private void setMaxLon(float maxLon) {
        this.maxLon = maxLon;
        this.deltaLon = NULL_COORDINATE;
    }

    public int getLatDimensionLength() {
        return latDimensionLength;
    }

    private void setLatDimensionLength(int latDimensionLength) {
        this.latDimensionLength = latDimensionLength;
    }

    public int getLonDimensionLength() {
        return lonDimensionLength;
    }

    private void setLonDimensionLength(int lonDimensionLength) {
        this.lonDimensionLength = lonDimensionLength;
    }

    public void add(GeoPos geoPos) {
        float lat = (float) geoPos.lat;
        float lon = (float) geoPos.lon;

        add(lat, lon);
    }

    public void add(float lat, float lon) {
        if (lat > getMaxLat() || getMaxLat() == NULL_COORDINATE) {
            setMaxLat(lat);
        }

        if (lat < getMinLat() || getMinLat() == NULL_COORDINATE) {
            setMinLat(lat);
        }

        if (lon > getMaxLon() || getMaxLon() == NULL_COORDINATE) {
            setMaxLon(lon);
        }

        if (lon < getMinLon() || getMinLon() == NULL_COORDINATE) {
            setMinLon(lon);
        }
    }

    public short getValue(GeoPos geoPos) {
        return getValue((float)geoPos.lat, (float) geoPos.lon);
    }

    public short getValue(float lat, float lon) {
        int latIndex = getLatIndex(lat);
        int lonIndex = getLonIndex(lon);

        return getValue(latIndex, lonIndex);
    }

    public short getValue(int latIndex, int lonIndex) {
        return values[latIndex][lonIndex];
    }

    public void setValues(short[][] values) {
        this.values = values;

        setLatDimensionLength(values.length);
        setLonDimensionLength(values[0].length);
    }

    public void setValueUcarArray(Array valueUcarArray) {
        values = (short[][]) valueUcarArray.copyToNDJavaArray();
    }
}
