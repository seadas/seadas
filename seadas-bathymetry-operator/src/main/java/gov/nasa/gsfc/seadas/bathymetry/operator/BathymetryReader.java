package gov.nasa.gsfc.seadas.bathymetry.operator;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 7/2/13
 * Time: 9:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class BathymetryReader {

    //  For test purposes here is the command line view:
    // OCSSWOldModel/run/data/common:   ncdump -h ETOPO1_ocssw.nc

    private NetcdfFile ncFile;

    float startLat = 0;
    float endLat = 0;
    float startLon = 0;
    float endLon = 0;

    float deltaLat;
    float deltaLon;
    float deltaLatStrange;
    float deltaLonStrange;

    int dimensionLat = 0;
    int dimensionLon = 0;

    Variable heightVariable;
    Variable waterSurfaceHeightVariable;

    private short missingValue;

    public BathymetryReader(File file) throws IOException {
        ncFile = NetcdfFile.open(file.getAbsolutePath());

        startLon = ncFile.findGlobalAttribute("left_lon").getNumericValue().floatValue();
        endLon = ncFile.findGlobalAttribute("right_lon").getNumericValue().floatValue();

        startLat = ncFile.findGlobalAttribute("lower_lat").getNumericValue().floatValue();
        endLat = ncFile.findGlobalAttribute("upper_lat").getNumericValue().floatValue();

        dimensionLat = ncFile.findDimension("lat").getLength();
        dimensionLon = ncFile.findDimension("lon").getLength();

        deltaLat = (endLat - startLat) / dimensionLat;
        deltaLon = (endLon - startLon) / dimensionLon;
        deltaLatStrange = (endLat - startLat) / (dimensionLat-1);
        deltaLonStrange = (endLon - startLon) / (dimensionLon-1);

        heightVariable = ncFile.findVariable("height");
        waterSurfaceHeightVariable = ncFile.findVariable("water_surface_height");

        missingValue = heightVariable.findAttribute("missing_value").getNumericValue().shortValue();

    }


    public void close() throws IOException {
        if (ncFile != null) {
            ncFile.close();
        }
    }

    public Array getHeightArray(int[] origin, int[] shape) {

        Array heightArray = null;

        try {
            heightArray = heightVariable.read(origin, shape);
        } catch (IOException e) {

        } catch (InvalidRangeException e) {

        }

        if (heightArray != null) {
            return heightArray;
        } else {
            return null;
        }
    }

    public Array getWaterSurfaceHeightArray(int[] origin, int[] shape) {

        Array heightArray = null;

        try {
            heightArray = waterSurfaceHeightVariable.read(origin, shape);
        } catch (IOException e) {

        } catch (InvalidRangeException e) {

        }

        if (heightArray != null) {
            return heightArray;
        } else {
            return null;
        }
    }

    public short getHeight(int latIndex, int lonIndex) {

        short height;
        int[] origin = new int[]{latIndex, lonIndex};
        int[] shape = new int[]{1, 1};

        try {
            height = heightVariable.read(origin, shape).getShort(0);
        } catch (IOException e) {
            return getMissingValue();
        } catch (InvalidRangeException e) {
            return getMissingValue();
        }

        return height;
    }


    public float getLon(int lonIndex) {

        if (lonIndex > dimensionLon - 1) {
            lonIndex = dimensionLon - 1;
        }

        if (lonIndex < 0) {
            lonIndex = 0;
        }

        return  startLon + lonIndex * deltaLonStrange;
    }

    public float getLat(int latIndex) {

        if (latIndex > dimensionLat - 1) {
            latIndex = dimensionLat - 1;
        }

        if (latIndex < 0) {
            latIndex = 0;
        }

        return  startLat + latIndex * deltaLatStrange;
    }

    public int getLatIndex(double lat) {
        int latIndex = (int) ((lat - startLat) / deltaLatStrange);

        if (latIndex > dimensionLat - 1) {
            latIndex = dimensionLat - 1;
        }

        if (latIndex < 0) {
            latIndex = 0;
        }

        return latIndex;
    }

    public int getLonIndex(double lon) {
        int lonIndex = (int) ((lon - startLon) / deltaLonStrange);

        if (lonIndex > dimensionLon - 1) {
            lonIndex = dimensionLon - 1;
        }

        if (lonIndex < 0) {
            lonIndex = 0;
        }

        return lonIndex;
    }

    public short getMissingValue() {
        return missingValue;
    }

}
