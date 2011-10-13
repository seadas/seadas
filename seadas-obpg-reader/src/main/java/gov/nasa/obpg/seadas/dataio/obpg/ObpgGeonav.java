
/**
 *
 * ObpgGeonav - navigational functions for SeaWiFS (needed for SeaDAS to
 *    work with SeaWiFS Level1 files).  This code was converted from 
 *    Fortran code in the OCSSW library (specifically the interpnav_seawifs
 *    program), mostly geonav.f.  For explanation, see the journal article:
 *       Exact closed-form geolocation algorithm for Earth survey sensors
 *       by Patt, F.S. and W.W. Gregg
 *       International Journal of Remote Sensing
 *       1994, Volume 15, No. 18, pp. 3719-3734
 *    Within OBPG, hardcopies might be available locally.  It was found online at:
 *       http://www.informaworld.com/smpp/content~db=all~content=a778242783~frm=titlelink
 *
 * History:
 * What:                   Who:             When:
 * Original conversion     Matt Elliott     June - August, 2011
 *    from Fortran
 */

package gov.nasa.obpg.seadas.dataio.obpg;

import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

public class ObpgGeonav {
    // Constants - the Fortran version defines pi, but Math.PI can be used here.
    public static final float DEGREES_PER_RADIAN = (float) (180.0 / Math.PI);
    public static final double EARTH_RADIUS = 6378.137;                // often re
    public static final double EARTH_FLATTENING_FACTOR = 1.0/298.257;  // often f
    public static final double EARTH_MEAN_RADIUS = 6371.0;             // often rem
    public static final double EARTH_ROTATION_RATE = 7.29211585494E-5; //often omegae
    public static final double OMF2 = Math.pow((1.0 - EARTH_FLATTENING_FACTOR),
                                               2.0);          // Convenience constant

    private static final int   GAC_START_SCAN_PIXEL = 147;
    private static final int   LAC_START_SCAN_PIXEL = 1;
    private static final int   GAC_PIXELS_PER_SCAN = 248;
    private static final int   LAC_PIXELS_PER_SCAN = 1285;
    private static final int   GAC_PIXEL_INCREMENT = 4;
    private static final int   LAC_PIXEL_INCREMENT = 1;

    /* The sensorOffsetMatrix corresponds to navctl%msensoff and tiltCosVector
     * corresponds to navctl%tiltcos in the Fortran version.  Both variables are
     * part of the navctl structure, which is read from the navctl.dat file in
     * the Fortran.
     */
    float[][] sensorOffsetMatrix = new float[3][3];
    float[] tiltCosVector = new float[3];

    //private float      radeg = DEGREES_PER_RADIAN;  // radeg in the Fortran version

    enum DataType { GAC, LAC }

    // The following are input parameters in the Fortran geonav.f function:
    private float[]   scanPathCoef = new float[6];           // in Fortran version
    private int       pixIncr;                               // ninc in Fortran version
    private int       pixPerScan;                            // npix in Fortran version
    private int       scanStartPix;                          // nsta in Fortran version
    private float[]   orbPos = new float[3];                 // pos in Fortran version
    private float[][] sensorOrientation = new float[3][3];   // rm in Fortran version
    private float[]   sunUnitVec = new float[3];             // sun in Fortran version

    // Output parameters in Fortran version:
    float[] sena = new float[1285];
    float[] senz = new float[1285];
    float[] sola = new float[1285];
    float[] solz = new float[1285];
    float[] xlat = new float[1285];
    float[] xlon = new float[1285];

    private float[]   attAngle = new float[3];

    String dataFilePath = null;

    DataType dataType;

    double SINC = 0.0015911;

    public ObpgGeonav(float[] pos, float[][] rm, float[] coef, float[] sun, float[] aa, float tilt,
                      NetcdfFile ncFile) {
        this(pos, rm, sun, aa, tilt, ncFile);
        scanPathCoef[0] = coef[0];
        scanPathCoef[1] = coef[1];
        scanPathCoef[2] = coef[2];
        scanPathCoef[3] = coef[3];
        scanPathCoef[4] = coef[4];
        scanPathCoef[5] = coef[5];
    }

    public ObpgGeonav(float[] pos, float[][] rm, float[] sun, float[] aa, float tilt,
                      NetcdfFile ncFile) {

        /* The sensorOffsetMatrix values were copied from the navctl.dat file.
         * According to email from F. Patt, the values never changed during the
         * SeaWiFS mission, thus they are hard-coded here.
         */
        sensorOffsetMatrix[0][0] = 1.0f;
        sensorOffsetMatrix[0][1] = 0.0f;
        sensorOffsetMatrix[0][2] = 0.0f;
        sensorOffsetMatrix[1][0] = 0.0f;
        sensorOffsetMatrix[1][1] = 0.99999905f;
        sensorOffsetMatrix[1][2] = -0.00139626f;
        sensorOffsetMatrix[2][0] = 0.0f;
        sensorOffsetMatrix[2][1] = 0.00139626f;
        sensorOffsetMatrix[2][2] = 0.99999905f;

        // The tiltCosVector values were copied from the navctl.dat file.
        tiltCosVector[0] = 0.0f;
        tiltCosVector[1] = 0.0f;
        tiltCosVector[2] = 1.0f;

        attAngle[0] = aa[0];
        attAngle[0] = aa[1];
        attAngle[0] = aa[2];

        orbPos[0] = pos[0];
        orbPos[1] = pos[1];
        orbPos[2] = pos[2];
        sensorOrientation[0][0] = rm[0][0];
        sensorOrientation[0][1] = rm[0][1];
        sensorOrientation[0][2] = rm[0][2];
        sensorOrientation[1][0] = rm[1][0];
        sensorOrientation[1][1] = rm[1][1];
        sensorOrientation[1][2] = rm[1][2];
        sensorOrientation[2][0] = rm[2][0];
        sensorOrientation[2][1] = rm[2][1];
        sensorOrientation[2][2] = rm[2][2];

        float[][] attXfm = computeTransformMatrix(tilt);
        scanPathCoef = computeInterEllCoefs(attXfm, attAngle, tilt, orbPos);

        sunUnitVec[0] = sun[0];
        sunUnitVec[1] = sun[1];
        sunUnitVec[2] = sun[2];
        dataType = getSeawifsDataType(ncFile);
        if (dataType == DataType.GAC) {
            scanStartPix = GAC_START_SCAN_PIXEL;
            pixPerScan = GAC_PIXELS_PER_SCAN;
            pixIncr = GAC_PIXEL_INCREMENT;

            /* !!!Temporary override for testing/debugging!!! */
            scanStartPix = LAC_START_SCAN_PIXEL;
            pixPerScan = LAC_PIXELS_PER_SCAN;
            pixIncr = LAC_PIXEL_INCREMENT;
        } else {
            scanStartPix = LAC_START_SCAN_PIXEL;
            pixPerScan = LAC_PIXELS_PER_SCAN;
            pixIncr = LAC_PIXEL_INCREMENT;
        }
    }

    private float [] computeEastVector(float[] up, float upxy) {
        float[] eastVector = new float[3];
        eastVector[0] = -up[1] / upxy;
        eastVector[1] = up[0] / upxy;
        return eastVector;
    }

    private float[] computeInterEllCoefs(float[][] attTransform,
                                         float[] attAngle, float tilt,
                                         float[]p) {
        /**
         * Compute coefficients of the intersection ellipse in the scan plane;
         * partial adaptation of the ellxfm function in ellxfm.f
         */

        double rd = 1.0f / OMF2;
        float[] coef = new float[6];

        float[][] sm1 = new float[3][3];
        float[][] sm2 = new float[3][3];
        float[][] sm3 = new float[3][3];

        sm1 = computeEulerTransformMatrix(attAngle);
        sm2 = multiplyMatrices(sm1, attTransform);

        // the next two lines need work
        sm1 = multiplyMatrices(sensorOffsetMatrix, sm2);
        sm2 = computeEulerAxisMatrix(tiltCosVector, tilt);
        sm3 = multiplyMatrices(sm2, sm1);

        coef[0] = (float) (1.0f + (rd - 1.0f) * sm3[0][2] * sm3[0][2]);
        coef[1] = (float) ((rd - 1.0f) * sm3[0][2] * sm3[2][2] * 2.0f);
        coef[2] = (float) (1.0f + (rd - 1.0f) * sm3[2][2] * sm3[2][2]);
        coef[3] = (float) ((sm3[0][0] * p[0] + sm3[0][1] * p[1] + sm3[0][2] * p[2] * rd) * 2.0f);
        coef[4] = (float) ((sm3[2][0] * p[0] + sm3[2][1] * p[1] + sm3[2][2] * p[2] * rd) * 2.0f);
        coef[5] = (float) (p[0] * p[0] + p[1] * p[1] + p[2] * p[2] * rd - EARTH_RADIUS * EARTH_RADIUS);
        return coef;
    }

    private float[][] computeEulerAxisMatrix(float[] eulerAxisUnitVector, float phi) {
        /**
         * An adaptation of the eaxis function in eaxis.f
         */
        float cp = (float) Math.cos(phi / DEGREES_PER_RADIAN);
        float sp = (float) Math.sin(phi / DEGREES_PER_RADIAN);
        float omcp = 1.0f - cp;

        float[][] xm = new float[3][3];

        xm[0][0] = cp + eulerAxisUnitVector[0] * eulerAxisUnitVector[0] * omcp;
        xm[0][1] = eulerAxisUnitVector[0] * eulerAxisUnitVector[1] * omcp + eulerAxisUnitVector[2] * sp;
        xm[0][2] = eulerAxisUnitVector[0] * eulerAxisUnitVector[2] * omcp - eulerAxisUnitVector[1] * sp;
        xm[1][0] = eulerAxisUnitVector[0] * eulerAxisUnitVector[1] * omcp - eulerAxisUnitVector[2] * sp;
        xm[1][1] = cp + eulerAxisUnitVector[1] * eulerAxisUnitVector[1] * omcp;
        xm[1][2] = eulerAxisUnitVector[1] * eulerAxisUnitVector[2] * omcp + eulerAxisUnitVector[0] * sp;
        xm[2][0] = eulerAxisUnitVector[0] * eulerAxisUnitVector[2] * omcp + eulerAxisUnitVector[1] * sp;
        xm[2][1] = eulerAxisUnitVector[1] * eulerAxisUnitVector[2] * omcp - eulerAxisUnitVector[0] * sp;
        xm[2][2] = cp + eulerAxisUnitVector[2] * eulerAxisUnitVector[2] * omcp;
        return xm;
    }


    private float[][] computeEulerTransformMatrix(float angles[]) {
        /**
         * An adaptation of the euler function in euler.f
         */
        float[][] xm1 = new float[3][3];
        float[][] xm2 = new float[3][3];
        float[][] xm3 = new float[3][3];
        float[][] transformationMatrix = new float[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                xm1[i][j] = 0;
                xm2[i][j] = 0;
                xm3[i][j] = 0;
            }
        }

        float c1 = (float) Math.cos(angles[0] / DEGREES_PER_RADIAN);
        float s1 = (float) Math.sin(angles[0] / DEGREES_PER_RADIAN);
        float c2 = (float) Math.cos(angles[1] / DEGREES_PER_RADIAN);
        float s2 = (float) -Math.sin(angles[1] / DEGREES_PER_RADIAN);
        float c3 = (float) Math.cos(angles[2] / DEGREES_PER_RADIAN);
        float s3 = (float) -Math.sin(angles[2] / DEGREES_PER_RADIAN);

        xm1[0][0] = 1.0f;
        xm1[1][1] = c1;
        xm1[2][2] = c1;
        xm1[1][2] = s1;
        xm1[2][1] = -s1;
        xm2[1][1] = 1.0f;
        xm2[0][0] = c2;
        xm2[2][2] = c2;
        xm2[2][0] = s2;
        xm2[0][2] = -s2;
        xm3[2][2] = 1.0f;
        xm3[1][1] = c3;
        xm3[0][0] = c3;
        xm3[0][1] = s3;

        float[][] xmm = multiplyMatrices(xm2, xm3);
        transformationMatrix = multiplyMatrices(xm1, xmm);
        return transformationMatrix;
    }

    private float computeLatitude(float[] geovec) {
        float tmp = (float) (Math.sqrt(geovec[0] * geovec[0] +
                    geovec[1] * geovec[1]) * OMF2);
        float xlat = DEGREES_PER_RADIAN * (float) Math.atan2(geovec[2], tmp);
        return xlat;
    }

    /*private float computeLongitude() {
    }*/

    private double computeQ(double a, double b, double h, double r, double sinl) {
        //  Solve for magnitude of sensor-to-pixel vector and compute components
        double q = (-b - Math.sqrt(r)) / (2.0 * a);
        //  Add out-of-plane correction
        q = q * (1.0 + sinl * h / Math.sqrt(r));;
        return q;
    }

    float computeSensorAzimuth(float senz, double sn, double se) {
        float sena;
        // Check for zenith close to zero
        if (senz > 0.05f) {
            sena = (float) (DEGREES_PER_RADIAN * Math.atan2(se,sn));
        } else {
            sena = 0.0f;
        }
        if (sena < 0.0f) {
            sena = sena + 360.0f;
        }
        return sena;
    }

    float computeSensorZenith(double sn, double se, double sv) {
        return (float) (DEGREES_PER_RADIAN * Math.atan2(Math.sqrt(sn*sn+se*se),sv));
    }

    float computeSolarAzimuth(float solz, double sunn, double sune) {
        float sola;
        // Check for zenith close to zero
        if (solz > 0.05f) {
            sola = (float) (DEGREES_PER_RADIAN * Math.atan2(sune, sunn));
        } else {
            sola = 0.0f;
        }
        if (sola < 0.0f) {
            sola = sola + 360.0f;
        }
        return sola;
    }

    float computeSolarZenith(double sunn, double sune, double sunv) {
        return (float) (DEGREES_PER_RADIAN * Math.atan2(Math.sqrt(sunn * sunn + sune * sune), sunv));
    }

    private float[][] computeTransformMatrix(float tilt) {
        /**
         * Compute the ECEF-to-orbital tranformation matrix using the
         * sensor transformation matrix.  Corresponds to the get_xfm
         * function in the original Fortran.
         */
        float[][] xfmMatrix = new float[3][3];
        xfmMatrix[0][0] = 0.0f;
        xfmMatrix[0][1] = 0.0f;
        xfmMatrix[0][2] = 0.0f;
        xfmMatrix[1][0] = 0.0f;
        xfmMatrix[1][1] = 0.0f;
        xfmMatrix[1][2] = 0.0f;
        xfmMatrix[2][0] = 0.0f;
        xfmMatrix[2][1] = 0.0f;
        xfmMatrix[2][2] = 0.0f;

        float[][] sm1 = computeEulerAxisMatrix(tiltCosVector, tilt);
        float[][] sm2 = transposeMatrix(sm1);
        float[][] sm3 = multiplyMatrices(sm2, sensorOrientation);
        sm1 = transposeMatrix(sensorOffsetMatrix);
        sm2 = multiplyMatrices(sm1, sm3);
        sm3 = computeEulerTransformMatrix(attAngle);
        sm1 = transposeMatrix(sm3);
        xfmMatrix = multiplyMatrices(sm1, sm2);
        return xfmMatrix;
    }

    private float [] computeVerticalUnitVector(float[] geovec) {
        float [] up = new float[3];
        float uxy = geovec[0] * geovec[0] + geovec[1] * geovec[1];
        float temp = (float) Math.sqrt(geovec[2] * geovec[2] + OMF2 * OMF2 * uxy);
        up[0] = (float) (OMF2 * geovec[0] / temp);
        up[1] = (float) (OMF2 * geovec[1] / temp);
        up[2] = geovec[2] / temp;
        return up;
    }

    public static float[] crossProduct(float[] v1, float[] v2) {
        /**
         * Compute cross product of two (length 3) vectors (adapted from
         * crossp.f; also see:
         *     http://en.wikipedia.org/wiki/Cross_product
         * or a linear algebra text).  The array subscripts differ from the
         * definitional/Fortran subscripts due to Java using 0-based arrays, vs.
         * the definition/Fortran using 1-based arrays.
         */
        float[] v3;
        v3 = new float[3];
        /* */
        v3[0] = v1[1] * v2[2] - v1[2] * v2[1];
        v3[1] = v1[2] * v2[0] - v1[0] * v2[2];
        v3[2] = v1[0] * v2[1] - v1[1] * v2[0];
        return v3;
    }

    public void doComputations() {
        double   cosa[] = new double[1285];
        double   cosl;
        float[]  ea = new float[3];
        double   elev;
        float[]  geovec = new float[3];
        float[]  no = new float[3];
        float[]  rmtq = new float[3];
        double   se;
        double   sina[] = new double[1285];
        double   sinl;
        double   sn;
        double   sune = 0.0;
        double   sunn = 0.0;
        double   sunv = 0.0;
        double   sv;
        float[]  up = new float[3];

        //  Compute elevation (out-of-plane) angle
        elev = SINC * 1.2;
        sinl = Math.sin(elev);
        cosl = Math.cos(elev);
        for (int i = 0; i < 1285; i ++) {
            sina[i] = Math.sin((i - 642) * SINC) * cosl;
            cosa[i] = Math.cos((i - 642) * SINC) * cosl;
        }

        //  Compute correction factor for out-of-plane angle
        double h = (sensorOrientation[0][1] * orbPos[0]
                    + sensorOrientation[1][1] * orbPos[1]
                    + sensorOrientation[2][1] * orbPos[2] / OMF2) * 2.0;

        //  Compute sensor-to-surface vectors for all scan angles
        for (int i = 0; i < pixPerScan; i ++) {
            int in = pixIncr * (i) + scanStartPix - 1;
	        double a = scanPathCoef[0] * cosa[in] * cosa[in]  +
                       scanPathCoef[1] * cosa[in] * sina[in] +
                       scanPathCoef[2] * sina[in] * sina[in];
            double b = scanPathCoef[3] * cosa[in]
                       + scanPathCoef[4] * sina[in];
            double c = scanPathCoef[5];
            double r = b * b - 4.0 * c * a;  // begin solve quadratic equation

            //  Check for scan past edge of Earth
            if (r < 0.0) {
                xlat[i] = 999.0f;
                xlon[i] = 999.0f;
                solz[i] = 999.0f;
                sola[i] = 999.0f;
                senz[i] = 999.0f;
                sena[i] = 999.0f;
            } else {
                double q = computeQ(a, b, h, r, sinl);
                double Qx = q * cosa[in];
                double Qy = q * sinl;
                double Qz = q * sina[in];

                //  Transform vector from sensor to geocentric frame
                for (int j = 0; j < 3; j++) {
                    rmtq[j] = (float) (Qx * sensorOrientation[j][0]
                              + Qy * sensorOrientation[j][1]
                              + Qz * sensorOrientation[j][2]);
                    geovec[j] = rmtq[j] + orbPos[j];
                }

                // Compute geodetic latitude and longitude
                xlat[i] = computeLatitude(geovec);
                xlon[i] = DEGREES_PER_RADIAN * (float) Math.atan2(geovec[1], geovec[0]);

                // Compute the local vertical, East and North unit vectors
                up = computeVerticalUnitVector(geovec);
                float upxy = (float) (Math.sqrt(up[0] * up[0] + up[1] * up[1]));
                ea = computeEastVector(up, upxy);
                no = crossProduct(up,ea);

                // Compute components of spacecraft and sun vector in the
                // vertical (up), North (no), and East (ea) vectors frame
                sv = 0.0;
                sn = 0.0;
                se = 0.0;
                sunv = 0.0;
                sunn = 0.0;
                sune = 0.0;
                for (int j = 0; j < 3; j++) {
                    double s = -rmtq[j];
                    sv = sv + s * up[j];
                    sn = sn + s * no[j];
                    se = se + s * ea[j];
                    sunv = sunv + sunUnitVec[j] * up[j];
                    sunn = sunn + sunUnitVec[j] * no[j];
                    sune = sune + sunUnitVec[j] * ea[j];
                }

                // Compute the sensor zenith and azimuth
                senz[i] = computeSensorZenith(sn, se, sv);
                sena[i] = computeSensorAzimuth(senz[i], sn, se);
            }  // close (else part of) if (r < 0.0)

            // Compute the solar zenith and azimuth
            solz[i] = computeSolarZenith(sunn, sune, sunv);
            sola[i] = computeSolarAzimuth(solz[i], sunn, sune);
        } // close for (int i = 0; i < npix; i ++)
    } // close doComputations()

    public float[] getSensorAzimuth() {
        return sena;
    }

    public float[] getSensorZenith() {
        return senz;
    }

    public float[] getSolarAzimuth() {
        return sola;
    }

    public float[] getSolarZenith() {
        return solz;
    }

    public float[] getLatitude() {
        return xlat;
    }

    public float[] getLongitude() {
        return xlon;
    }

    public static int getNumberScanLines(NetcdfFile ncFile) {
        Attribute numScanLinesAttr = ncFile.findGlobalAttribute("Number of Scan Lines");
        int numScanLines = numScanLinesAttr.getNumericValue().intValue();
        return numScanLines;
    }

    public static ObpgGeonav.DataType getSeawifsDataType(NetcdfFile ncFile) {
        ObpgGeonav.DataType dataType = ObpgGeonav.DataType.LAC;
        Attribute dataTypeAttr = ncFile.findGlobalAttribute("Data Type");
        Attribute numScanLinesAttr = ncFile.findGlobalAttribute("Number of Scan Lines");
        if (dataTypeAttr.getStringValue().equals("GAC")) {
            dataType = ObpgGeonav.DataType.GAC;
        }        
        return dataType;
    }

    public static float[][] multiplyMatrices(float[][] m1, float[][] m2) {
        /**
         * Multiply two 3 x 3 matrices, adapted from matmpy.f.
         */
        float[][] p = new float[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                p[i][j] = 0.0f;
                for (int k = 0; k < 3; k++) {
                    p[i][j] = p[i][j] + m1[i][k] * m2[k][j];
                }
            }
        }
        return p;
    }

    public static float[][] transposeMatrix(float[][] matrix) {
        /**
         * Create the transpose of a 3 x 3 matrix, adapted from
         * xpose.f.
         */
        float[][] transpose = new float[3][3];
        for (int i = 0; i < 3; i ++ ) {
            for (int j = 0; i < 3; i ++) {
                transpose[j][i] = matrix[i][j];
            }
        }
        return transpose;
    }

}
