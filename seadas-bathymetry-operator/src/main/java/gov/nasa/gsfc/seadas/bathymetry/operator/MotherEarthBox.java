package gov.nasa.gsfc.seadas.bathymetry.operator;

import org.esa.beam.framework.datamodel.GeoPos;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 10/31/13
 * Time: 11:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class MotherEarthBox {

    EarthBox earthBoxNW;
    EarthBox earthBoxNE;
    EarthBox earthBoxSW;
    EarthBox earthBoxSE;

    public MotherEarthBox() {
        earthBoxNW = new EarthBox();
        earthBoxNE = new EarthBox();
        earthBoxSW = new EarthBox();
        earthBoxSE = new EarthBox();
    }

    public void add(GeoPos geoPos) {

        if (geoPos.lat >= 0) {
            if (geoPos.lon >= 0) {
                earthBoxNE.add(geoPos);
            } else {
                earthBoxNW.add(geoPos);
            }
        } else {
            if (geoPos.lon >= 0) {
                earthBoxSE.add(geoPos);
            } else {
                earthBoxSW.add(geoPos);
            }
        }
    }


}
