package gov.nasa.gsfc.seadas.processing.general;

import org.esa.beam.framework.datamodel.GeoPos;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 10/31/13
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class MotherEarthBox2 {

    // establish 4 locations on the earth to minimize any effects of dateline and pole crossing
    private EarthBox2 earthBoxNW = new EarthBox2();
    private EarthBox2 earthBoxNE = new EarthBox2();
    private EarthBox2 earthBoxSW = new EarthBox2();
    private EarthBox2 earthBoxSE = new EarthBox2();

    private EarthBox2[] earthBoxes = {getEarthBoxNE(), getEarthBoxNW(), getEarthBoxSE(), getEarthBoxSW()};

    public MotherEarthBox2() {
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


    public EarthBox2 getEarthBoxNW() {
        return earthBoxNW;
    }

    public EarthBox2 getEarthBoxNE() {
        return earthBoxNE;
    }

    public EarthBox2 getEarthBoxSW() {
        return earthBoxSW;
    }

    public EarthBox2 getEarthBoxSE() {
        return earthBoxSE;
    }

    public EarthBox2[] getEarthBoxes() {
        return earthBoxes;
    }

    public ArrayList<EarthBox2> getFilledEarthBoxes() {
        ArrayList<EarthBox2> filledBoxesArrayList = new ArrayList<EarthBox2>();

        for (EarthBox2 earthBox : earthBoxes) {
            if (earthBox.getMaxLat() != EarthBox2.NULL_COORDINATE) {
                filledBoxesArrayList.add(earthBox);
            }
        }

        return filledBoxesArrayList;
    }
}
