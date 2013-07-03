package gov.nasa.gsfc.seadas.bathymetry.operator;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 7/2/13
 * Time: 9:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class BathymetryReader {

    private NetcdfFile ncFile;
    List<Attribute> globalAttributes;
    List<Dimension> dimensions;

    double startLat = 0;
    double endLat = 0;
    double startLon = 0;
    double endLon = 0;

    int dimensionLat = 0;
    int dimensionLon = 0;

    public BathymetryReader(File file) throws IOException {
        ncFile = NetcdfFile.open(file.getAbsolutePath());
        globalAttributes = ncFile.getGlobalAttributes();
        dimensions = ncFile.getDimensions();

        init();
    }


    public void init() {
        for (Attribute attribute : globalAttributes) {
            int length = attribute.getLength();
            Object stringValue = attribute.getValue(0);


            if (attribute.getName().equals("right_lon")) {
                endLon = Double.parseDouble(attribute.getValue(0).toString());
            }
            if (attribute.getName().equals("upper_lat")) {
                startLat = Double.parseDouble(attribute.getValue(0).toString());
            }
            if (attribute.getName().equals("lower_lat")) {
                endLat = Double.parseDouble(attribute.getValue(0).toString());
            }
            if (attribute.getName().equals("left_lon")) {
                startLon = Double.parseDouble(attribute.getValue(0).toString());
            }

        }


        for (Dimension dimension : dimensions) {
            if (dimension.getName().equals("lon")) {
                dimensionLon = dimension.getLength();
            }

            if (dimension.getName().equals("lat")) {
                dimensionLat = dimension.getLength();
            }
        }
    }


    public void close() throws IOException {
        if (ncFile != null) {
            ncFile.close();
        }
    }


    /**
     * Returns the sample value at the given geo-position, regardless of the source resolution.
     *
     * @param lat The latitude value.
     * @param lon The longitude value.
     * @return bathymetry height
     *         <p/>
     *         <p/>
     *         look at seadasFileReader for netCDF examples.
     *         ncdump -h ETOPO1_ocssw.nc
     */
    public short getHeight(float lat, float lon) {

        double deltaLat = (endLat - startLat) / dimensionLat;
        double deltaLon = (endLon - startLon) / dimensionLon;

        int latIndex = (int) Math.round((lat - startLat) / deltaLat);
        int lonIndex = (int) Math.round((lon - startLon) / deltaLon);

        List<Variable> variables = ncFile.getVariables();

        Array heightUcarArray = null;
        short[] height = null;

        for (Variable variable : variables) {
            if (variable.getShortName().equals("height")) {
                try {
                    heightUcarArray = variable.read(new int[]{latIndex, lonIndex}, new int[]{1, 1});
                    height = (short[]) heightUcarArray.copyTo1DJavaArray();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (InvalidRangeException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                break;
            }
        }

        if (height == null) {
            return BathymetryMaskClassifier.INVALID_VALUE;
        }

        return height[0];
    }
}
