package gov.nasa.gsfc.seadas.bathymetry.operator;

import org.esa.snap.core.datamodel.GeoPos;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 10/31/13
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class MotherEarthBox {

    // establish 4 locations on the earth to minimize any effects of dateline and pole crossing
    private EarthBox earthBoxNW = new EarthBox();
    private EarthBox earthBoxNE = new EarthBox();
    private EarthBox earthBoxSW = new EarthBox();
    private EarthBox earthBoxSE = new EarthBox();

    private EarthBox[] earthBoxes = {getEarthBoxNE(), getEarthBoxNW(), getEarthBoxSE(), getEarthBoxSW()};

    public MotherEarthBox() {
    }

    public void add(GeoPos geoPos) {

        if (geoPos.lat >= 0) {
            if (geoPos.lon >= 0) {
                getEarthBoxNE().add(geoPos);
            } else {
                getEarthBoxNW().add(geoPos);
            }
        } else {
            if (geoPos.lon >= 0) {
                getEarthBoxSE().add(geoPos);
            } else {
                getEarthBoxSW().add(geoPos);
            }
        }
    }


    public short getValue(GeoPos geoPos) {
        short bathymetryValue;

        if (geoPos.lat >= 0) {
            if (geoPos.lon >= 0) {
                bathymetryValue = earthBoxNE.getValue(geoPos);
            } else {
                bathymetryValue = earthBoxNW.getValue(geoPos);
            }
        } else {
            if (geoPos.lon >= 0) {
                bathymetryValue = earthBoxSE.getValue(geoPos);
            } else {
                bathymetryValue = earthBoxSW.getValue(geoPos);
            }
        }

        return bathymetryValue;
    }

    public short getWaterSurfaceValue(GeoPos geoPos) {
        short waterSurfaceValue;

        if (geoPos.lat >= 0) {
            if (geoPos.lon >= 0) {
                waterSurfaceValue = earthBoxNE.getWaterSurfaceValue(geoPos);
            } else {
                waterSurfaceValue = earthBoxNW.getWaterSurfaceValue(geoPos);
            }
        } else {
            if (geoPos.lon >= 0) {
                waterSurfaceValue = earthBoxSE.getWaterSurfaceValue(geoPos);
            } else {
                waterSurfaceValue = earthBoxSW.getWaterSurfaceValue(geoPos);
            }
        }

        return waterSurfaceValue;
    }

    public EarthBox getEarthBoxNW() {
        return earthBoxNW;
    }

    public EarthBox getEarthBoxNE() {
        return earthBoxNE;
    }

    public EarthBox getEarthBoxSW() {
        return earthBoxSW;
    }

    public EarthBox getEarthBoxSE() {
        return earthBoxSE;
    }

    public EarthBox[] getEarthBoxes() {
        return earthBoxes;
    }

    public ArrayList<EarthBox> getFilledEarthBoxes() {
        ArrayList<EarthBox> filledBoxesArrayList = new ArrayList<EarthBox>();

        for (EarthBox earthBox : earthBoxes) {
            if (earthBox.getMaxLat() != EarthBox.NULL_COORDINATE) {
                filledBoxesArrayList.add(earthBox);
            }
        }

        return filledBoxesArrayList;
    }
}
