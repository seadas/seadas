package gov.nasa.gsfc.seadas.bathymetry.operator;


import org.esa.beam.framework.datamodel.GeoPos;
import ucar.ma2.Array;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 10/30/13
 * Time: 11:00 AM
 * To change this template use File | Settings | File Templates.
 */
public class EarthBox {

    public static final float NULL_COORDINATE = (float) -999;

    private float minLat = NULL_COORDINATE;
    private float maxLat = NULL_COORDINATE;
    private float minLon = NULL_COORDINATE;
    private float maxLon = NULL_COORDINATE;

    float deltaLat = NULL_COORDINATE;
    float deltaLon = NULL_COORDINATE;

    private int dimensionLat = 0;
    private int dimensionLon = 0;

    private Array valueUcarArray;
    private short[][] values;

    public EarthBox() {

    }

    public void setDeltaLon() {
        if (getMinLon() != NULL_COORDINATE) {
            deltaLon = (getMaxLon() - getMinLon()) / getDimensionLon();
        }
    }

    public void setDeltaLat() {
        if (getMinLat() != NULL_COORDINATE) {
            deltaLat = (getMaxLat() - getMinLat()) / getDimensionLat();
        }
    }

    public float getDeltaLat() {
        if (deltaLat == NULL_COORDINATE) {
            setDeltaLat();
        }

        return deltaLat;
    }

    public float getDeltaLon() {
        if (deltaLon == NULL_COORDINATE) {
            setDeltaLon();
        }

        return deltaLon;
    }

    public int getLatIndex(float lat) {
        int latIndex = (int) Math.round((lat - getMinLat()) / getDeltaLat());
        if (latIndex > dimensionLat -1) {
            latIndex = dimensionLat - 1;
        }

        if (latIndex < 0) {
            latIndex = 0;
        }
        return latIndex;
    }

    public int getLonIndex(float lon) {
        int lonIndex = (int) Math.round((lon - getMinLon()) / getDeltaLon());
        if (lonIndex > dimensionLon -1) {
            lonIndex = dimensionLon - 1;
        }

        if (lonIndex < 0) {
            lonIndex = 0;
        }
        return lonIndex;
    }

    public float getMinLat() {
        return minLat;
    }

    public void setMinLat(float minLat) {
        this.minLat = minLat;
        this.deltaLat = NULL_COORDINATE;
    }

    public float getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(float maxLat) {
        this.maxLat = maxLat;
        this.deltaLat = NULL_COORDINATE;
    }

    public float getMinLon() {
        return minLon;
    }

    public void setMinLon(float minLon) {
        this.minLon = minLon;
        this.deltaLon = NULL_COORDINATE;
    }

    public float getMaxLon() {
        return maxLon;
    }

    public void setMaxLon(float maxLon) {
        this.maxLon = maxLon;
        this.deltaLon = NULL_COORDINATE;
    }

    public int getDimensionLat() {
        return dimensionLat;
    }

    public void setDimensionLat(int dimensionLat) {
        this.dimensionLat = dimensionLat;
    }

    public int getDimensionLon() {
        return dimensionLon;
    }

    public void setDimensionLon(int dimensionLon) {
        this.dimensionLon = dimensionLon;
    }

    public void add(GeoPos geoPos) {
        float lat = geoPos.lat;
        float lon = geoPos.lon;

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
        return getValue(geoPos.lat, geoPos.lon);
    }

    public short getValue(float lat, float lon) {
        int latIndex = getLatIndex(lat);
        int lonIndex = getLonIndex(lon);

        return getValue(latIndex, lonIndex);
    }

    public short getValue(int latIndex, int lonIndex) {
        return values[latIndex][lonIndex];
    }

    public void setValueUcarArray(Array valueUcarArray) {
        values = (short[][]) valueUcarArray.copyToNDJavaArray();
        this.valueUcarArray = valueUcarArray;
    }
}
