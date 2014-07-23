/* 
 *  Copyright (c) 2010-2011, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   
package gov.nasa.gsfc.seadas.contour.operator;

import com.vividsolutions.jts.geom.LineString;
import org.jaitools.CollectionFactory;
import org.jaitools.jts.LineSmoother;
import org.jaitools.jts.SmootherControl;
import org.jaitools.jts.Utils;
import org.jaitools.media.jai.AttributeOpImage;
import org.jaitools.numeric.CompareOp;
import org.jaitools.numeric.Range;

import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import java.awt.image.RenderedImage;
import java.lang.ref.SoftReference;
import java.util.*;


/**
 * Generates contours for user-specified levels of values in the source image.
 * The contours are returned as a {@code Collection} of
 * {@link com.vividsolutions.jts.geom.LineString}s.
 * <p>
 * The interpolation algorithm used is that of Paul Bourke: originally published
 * in Byte magazine (1987) as the CONREC contouring subroutine written in
 * FORTRAN. The implementation here was adapted from Paul Bourke's C code for the
 * algorithm available at: 
 * <a href="http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/">
 * http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/</a>
 * <p>
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ContourOpImage extends AttributeOpImage {

    /*
     * Constants to identify vertices for each group of
     * data points being processed, as per the diagram
     * in the javadoc for getContourSegments method.
     */
    private static final int BL_VERTEX1 = 0;
    private static final int BR_VERTEX2 = 1;
    private static final int TR_VERTEX3 = 2;
    private static final int TL_VERTEX4 = 3;
    
    /** The source image band to process */
    private int band;
    
    /** Values at which to generate contour intervals */
    private List<Double> contourLevels;
    
    /** 
     * Interval between contours. This is used if specific contour
     * levels are not requested. Contours will be generated such that
     * the value of each is an integer multiple of this value.
     */
    private Double contourInterval;
    
    /** List of Numbers to treat as NO_DATA */
    private List<Double> noDataNumbers;
    /** List of Ranges to treat as NO_DATA */
    private List<Range<Double>> noDataRanges;

    /** Whether to use strict NODATA exclusion */
    private final boolean strictNodata;
    
    /** Output contour lines */
    private SoftReference<List<LineString>> cachedContours;
    
    /** Whether to simplify contour lines by removing coincident vertices */
    private final boolean simplify;
    
    /** Whether to apply Bezier smoothing to the contour lines */
    private final boolean smooth;

    /** 
     * Alpha parameter controlling Bezier smoothing
     * (see {@link LineSmoother})
     */
    private double smoothAlpha = 0.0;
    
    /**
     * Control object for Bezier smoothing. Note that length units here
     * are pixels.
     */
    private final SmootherControl smootherControl = new SmootherControl() {

        public double getMinLength() {
            return 0.1;
        }

        public int getNumVertices(double length) {
            return (int) Math.max(5, length * 10);
        }
    };
    

    /**
     * Constructor. Note that one of {@code levels} or {@code interval} must
     * be supplied. If both are supplied {@code interval} is ignored.
     * 
     * @param source the source image
     * 
     * @param roi an optional {@code ROI} to constrain the areas for which
     *     contours are generated
     * 
     * @param band the band of the source image to process
     * 
     * @param levels values for which to generate contours
     * 
     * @param interval interval between contour levels (ignored if {@code levels}
     *     is supplied)
     * 
     * @param noDataValues an optional {@code Collection} of values and/or {@code Ranges}
     *     to treat as NO_DATA
     * 
     * @param simplify whether to simplify contour lines by removing
     *     colinear vertices
     * 
     * @param strictNodata if {@code true} any NO_DATA values in a 2x2 data window will
     *     cause that window to be skipped; if {@code false} a single NO_DATA value 
     *     is permitted
     * 
     * @param smooth whether contour lines should be smoothed using
     *     Bezier interpolation
     */
    public ContourOpImage(RenderedImage source, 
            ROI roi, 
            int band,
            Collection<? extends Number> levels,
            Double interval,
            Collection<Object> noDataValues,
            boolean strictNodata,
            boolean simplify,
            boolean smooth) {
                
        super(source, roi);

        this.band = band;
        
        if (levels != null) {
            this.contourLevels = new ArrayList<Double>();
            // Use specific levels
            for (Number z : levels) {
                this.contourLevels.add(z.doubleValue());
            }
            Collections.sort(contourLevels);
            
        } else if (interval != null && !interval.isNaN()) {
            // Use requested interval with levels 'discovered' as the
            // image is scanned
            this.contourInterval = interval;
        } else {
            throw new IllegalArgumentException("At least one of levels or interval must be supplied");
        }
        
        this.noDataNumbers = CollectionFactory.list();
        this.noDataRanges = CollectionFactory.list();

        if (noDataValues != null) {
            // Only add values that are not in the default set:
            // NaN, +ve and -ve Inf, MaxValue
            for (Object oelem : noDataValues) {
                if (oelem instanceof Number) {
                    double dz = ((Number)oelem).doubleValue();
                    if (!(Double.isNaN(dz) ||
                          Double.isInfinite(dz) ||
                          Double.compare(dz, Double.MAX_VALUE) == 0)) {
                        this.noDataNumbers.add(dz);
                    }
                } else if (oelem instanceof Range) {
                    Range r = (Range) oelem;
                    Double min = r.getMin().doubleValue();
                    Double max = r.getMax().doubleValue();
                    Range<Double> rd = new Range<Double>(
                            min, r.isMinIncluded(), max, r.isMaxIncluded());
                    this.noDataRanges.add(rd);

                } else {
                    // This should have been picked up by validateParameters
                    // method in ContourDescriptor, but just in case...
                    throw new IllegalArgumentException(
                              "only Number and Range elements are permitted in the "
                            + "noDataValues Collection");
                }
            }
        } 

        this.strictNodata = strictNodata;
        this.simplify = simplify;
        this.smooth = smooth;

        // Set the precision to use for Geometry operations
        Utils.setPrecision(100.0);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected Object getAttribute(String name) {
        if (cachedContours == null || cachedContours.get() == null) {
            synchronized(this) {
                cachedContours = new SoftReference<List<LineString>>(createContours());
            }
        }
        
        return cachedContours.get();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected String[] getAttributeNames() {
        return new String[]{ContourDescriptor.CONTOUR_PROPERTY_NAME};
    }

    /**
     * Returns the class of the specified attribute. For
     * {@link ContourDescriptor#CONTOUR_PROPERTY_NAME} this will be {@code List}.
     */
    @Override
    protected Class<?> getAttributeClass(String name) {
        if (ContourDescriptor.CONTOUR_PROPERTY_NAME.equalsIgnoreCase(name)) {
            return List.class;
        }
        
        return super.getAttributeClass(name);
    }
    

    /**
     * Controls contour generation.
     * 
     * @return generated contours
     */
    private List<LineString> createContours() {
        // build the contour levels if necessary
        if(contourLevels == null) {
            contourLevels = buildContourLevels();
        }
        // aggregate all the segments
        Map<Integer, Segments> segments = getContourSegments();

        /*
         * Assemble contours into a simple list and assign values 
         */
        List<LineString> mergedContourLines = new ArrayList<LineString>();

        int levelIndex = 0;
        for (Double levelValue : contourLevels) {
            Segments levelSegments = segments.remove(levelIndex);
            if (levelSegments != null) {
                List<LineString> levelContours = levelSegments.getMergedSegments();
                for (LineString line : levelContours) {
                    line.setUserData(levelValue);
                }
                mergedContourLines.addAll(levelContours);
            }
            levelIndex++;
        }
        
        /*
         * Bezier smoothing of contours
         */
        if (smooth) {
            LineSmoother smoother = new LineSmoother(Utils.getGeometryFactory());
            smoother.setControl(smootherControl);
            
            final int N = mergedContourLines.size();
            for (int i = N - 1; i >= 0; i--) {
                LineString contour = mergedContourLines.remove(i);
                LineString smoothed = smoother.smooth(contour, smoothAlpha);
                mergedContourLines.add(smoothed);
            }
        }
        
        return mergedContourLines;
    }

    
    /**
     * Creates contour segments.
     * The algorithm used is CONREC, devised by Paul Bourke (see class notes).
     * <p>
     * The source image is scanned with a 2x2 sample window. The algorithm
     * then treats these values as corner vertex values for a square that is
     * sub-divided into four triangles. This results in an additional centre
     * vertex.
     * <p>
     * The following diagram, taken from the C implementation of CONREC,
     * shows how vertices and triangles are indexed:
     * <pre>
     *            vertex 4 +-------------------+ vertex 3
     *                     | \               / |
     *                     |   \    m=3    /   |
     *                     |     \       /     |
     *                     |       \   /       |
     *                     |  m=4    X   m=2   |   centre vertex is 0
     *                     |       /   \       |
     *                     |     /       \     |
     *                     |   /    m=1    \   |
     *                     | /               \ |
     *            vertex 1 +-------------------+ vertex 2
     * 
     * </pre>
     * Each triangle is then categorized on which of its vertices are below,
     * at or above the contour level being considered. Triangle vertices 
     * (m1, m2, m3) are indexed such that:
     * <ul>
     * <li> m1 is the square vertex with index == triangle index
     * <li> m2 is square vertex 0
     * <li> m3 is square vertex m+1 (or 1 when m == 4)
     * </ul>
     * The original CONREC algorithm produces some duplicate line segments
     * which is not a problem when only plotting contours. However, here we
     * try to avoid any duplication because this can confuse the merging of
     * line segments into JTS LineStrings later.
     * <p>
     * NODATA values are handled by ignoring all triangles that have any
     * NODATA vertices.
     * 
     * @return the generated contour segments
     */
    private Map<Integer, Segments> getContourSegments() {

        Map<Integer, Segments> segments = new HashMap<Integer, Segments>();

        double[] sample = new double[4];
        boolean[] nodata = new boolean[4];
        double[] h = new double[5];
        double[] xh = new double[5];
        double[] yh = new double[5];
        int[] sh = new int[5];
        double temp1, temp2, temp3, temp4;

        int[][][] configLookup = {
            {{0, 0, 8}, {0, 2, 5}, {7, 6, 9}},
            {{0, 3, 4}, {1, 3, 1}, {4, 3, 0}},
            {{9, 6, 7}, {5, 2, 0}, {8, 0, 0}}
        };
        
        final PlanarImage src = getSourceImage(0);
        
        RectIter iter1 = RectIterFactory.create(src, src.getBounds());
        RectIter iter2 = RectIterFactory.create(src, src.getBounds());
        moveIterToBand(iter1, this.band);
        moveIterToBand(iter2, this.band);
        iter1.startLines();
        iter2.startLines();
        iter2.nextLine();
        
        int y = (int) src.getBounds().getMinY();
        while(!iter2.finishedLines() && !iter1.finishedLines()) {
            iter1.startPixels();
            iter2.startPixels();
            
            sample[BR_VERTEX2] = iter1.getSampleDouble();
            nodata[BR_VERTEX2] = isNoData(sample[BR_VERTEX2]);

            sample[TR_VERTEX3] = iter2.getSampleDouble();
            nodata[TR_VERTEX3] = isNoData(sample[TR_VERTEX3]);
            
            iter1.nextPixel();
            iter2.nextPixel();
            int x = (int) src.getBounds().getMinX() + 1;
            while (!iter1.finishedPixels() && !iter2.finishedPixels()) {
                sample[BL_VERTEX1] = sample[BR_VERTEX2];
                nodata[BL_VERTEX1] = nodata[BR_VERTEX2];

                sample[BR_VERTEX2] = iter1.getSampleDouble();
                nodata[BR_VERTEX2] = isNoData(sample[BR_VERTEX2]);

                sample[TL_VERTEX4] = sample[TR_VERTEX3];
                nodata[TL_VERTEX4] = nodata[TR_VERTEX3];

                sample[TR_VERTEX3] = iter2.getSampleDouble();
                nodata[TR_VERTEX3] = isNoData(sample[TR_VERTEX3]);

                boolean processSquare = true;
                boolean hasSingleNoData = false;
                for (int i = 0; i < 4 && processSquare; i++) {
                    if (nodata[i]) {
                        if (strictNodata || hasSingleNoData) {
                            processSquare = false;
                            break;
                        } else {
                            hasSingleNoData = true;
                        }
                    }
                }

                if (processSquare) {
                    if (nodata[BL_VERTEX1]) {
                        temp1 = temp3 = sample[TL_VERTEX4];
                    } else if (nodata[TL_VERTEX4]) {
                        temp1 = temp3 = sample[BL_VERTEX1];
                    } else {
                        temp1 = Math.min(sample[BL_VERTEX1], sample[TL_VERTEX4]);
                        temp3 = Math.max(sample[BL_VERTEX1], sample[TL_VERTEX4]);
                    }

                    if (nodata[BR_VERTEX2]) {
                        temp2 = temp4 = sample[TR_VERTEX3];
                    } else if (nodata[TR_VERTEX3]) {
                        temp2 = temp4 = sample[BR_VERTEX2];
                    } else {
                        temp2 = Math.min(sample[BR_VERTEX2], sample[TR_VERTEX3]);
                        temp4 = Math.max(sample[BR_VERTEX2], sample[TR_VERTEX3]);
                    }
                    double dmin = Math.min(temp1, temp2);
                    double dmax = Math.max(temp3, temp4);

                    final int size=contourLevels.size();
                    for (int levelIndex = 0; levelIndex < size; levelIndex++) {
                        double levelValue = contourLevels.get(levelIndex);
                        if (levelValue < dmin || levelValue > dmax) {
                            continue;
                        }

                        Segments zlist = segments.get(levelIndex);
                        if (zlist == null) {
                            zlist = new Segments(simplify);
                            segments.put(levelIndex, zlist);
                        }

                        if (!nodata[TL_VERTEX4]) {
                            h[4] = sample[TL_VERTEX4] - levelValue;
                            xh[4] = x - 1;
                            yh[4] = y + 1;
                            sh[4] = Double.compare(h[4], 0.0);
                        }

                        if (!nodata[TR_VERTEX3]) {
                            h[3] = sample[TR_VERTEX3] - levelValue;
                            xh[3] = x;
                            yh[3] = y + 1;
                            sh[3] = Double.compare(h[3], 0.0);
                        }

                        if (!nodata[BR_VERTEX2]) {
                            h[2] = sample[BR_VERTEX2] - levelValue;
                            xh[2] = x;
                            yh[2] = y;
                            sh[2] = Double.compare(h[2], 0.0);
                        }

                        if (!nodata[BL_VERTEX1]) {
                            h[1] = sample[BL_VERTEX1] - levelValue;
                            xh[1] = x - 1;
                            yh[1] = y;
                            sh[1] = Double.compare(h[1], 0.0);
                        }

                        h[0] = 0.0;
                        int nh = 0;
                        for (int i = 0; i < 4; i++) {
                            if (!nodata[i]) {
                                h[0] += h[i+1];
                                nh++ ;
                            }
                        }

                        // Just in case
                        if (nh < 3) {
                            throw new IllegalStateException(
                                    "Internal error: number data vertices = " + nh);
                        }

                        h[0] /= nh;
                        xh[0] = x - 0.5;
                        yh[0] = y + 0.5;
                        sh[0] = Double.compare(h[0], 0.0);

                        /* Scan each triangle in the box */
                        int m1, m2, m3;
                        for (int m = 1; m <= 4; m++) {
                            m1 = m;
                            m2 = 0;
                            m3 = m == 4 ? 1 : m + 1;

                            if (nodata[m1 - 1] || nodata[m3 - 1]) {
                                // skip this triangle with a NODATA vertex
                                continue;
                            }

                            int config = configLookup[sh[m1] + 1][sh[m2] + 1][sh[m3] + 1];
                            if (config == 0) {
                                continue;
                            }

                            double x0 = 0.0, y0 = 0.0, x1 = 0.0, y1 = 0.0;
                            boolean addSegment = true;
                            switch (config) {
                                /* Line between vertices 1 and 2 */
                                case 1:
                                    x0 = xh[m1];
                                    y0 = yh[m1];
                                    x1 = xh[m2];
                                    y1 = yh[m2];
                                    break;

                                /* Line between vertices 2 and 3 */
                                case 2:
                                    x0 = xh[m2];
                                    y0 = yh[m2];
                                    x1 = xh[m3];
                                    y1 = yh[m3];
                                    break;

                                /*
                                 * Line between vertices 3 and 1.
                                 * We only want to generate this segment
                                 * for triangles m=2 and m=3, otherwise
                                 * we will end up with duplicate segments.
                                 */
                                case 3:
                                    if (m == 2 || m == 3) {
                                        x0 = xh[m3];
                                        y0 = yh[m3];
                                        x1 = xh[m1];
                                        y1 = yh[m1];
                                    } else {
                                        addSegment = false;
                                    }
                                    break;

                                /* Line between vertex 1 and side 2-3 */
                                case 4:
                                    x0 = xh[m1];
                                    y0 = yh[m1];
                                    x1 = sect(m2, m3, h, xh);
                                    y1 = sect(m2, m3, h, yh);
                                    break;

                                /* Line between vertex 2 and side 3-1 */
                                case 5:
                                    x0 = xh[m2];
                                    y0 = yh[m2];
                                    x1 = sect(m3, m1, h, xh);
                                    y1 = sect(m3, m1, h, yh);
                                    break;

                                /* Line between vertex 3 and side 1-2 */
                                case 6:
                                    x0 = xh[m3];
                                    y0 = yh[m3];
                                    x1 = sect(m1, m2, h, xh);
                                    y1 = sect(m1, m2, h, yh);
                                    break;

                                /* Line between sides 1-2 and 2-3 */
                                case 7:
                                    x0 = sect(m1, m2, h, xh);
                                    y0 = sect(m1, m2, h, yh);
                                    x1 = sect(m2, m3, h, xh);
                                    y1 = sect(m2, m3, h, yh);
                                    break;

                                /* Line between sides 2-3 and 3-1 */
                                case 8:
                                    x0 = sect(m2, m3, h, xh);
                                    y0 = sect(m2, m3, h, yh);
                                    x1 = sect(m3, m1, h, xh);
                                    y1 = sect(m3, m1, h, yh);
                                    break;

                                /* Line between sides 3-1 and 1-2 */
                                case 9:
                                    x0 = sect(m3, m1, h, xh);
                                    y0 = sect(m3, m1, h, yh);
                                    x1 = sect(m1, m2, h, xh);
                                    y1 = sect(m1, m2, h, yh);
                                    break;
                            }

                            if (addSegment) {
                                zlist.add(x0, y0, x1, y1);
                            }
                        }
                    }
                }
                
                iter1.nextPixel();
                iter2.nextPixel();
                x++;
            }
            
            iter1.nextLine();
            iter2.nextLine();
            y++;
        }
        
        return segments;
    }
    

    /**
     * Calculate an X or Y ordinate for a contour segment end-point
     * relative to the difference in value between two sampling positions.
     * 
     * @param p1 index of the first sampling position
     * @param p2 index of the second sampling position
     * @param h source image values at sampling positions
     * @param coord X or Y ordinates of the 4 corner sampling positions
     * 
     * @return the calculated X or Y ordinate
     */
    private static double sect(int p1, int p2, double[] h, double[] coord) {
        return (h[p2] * coord[p1] - h[p1] * coord[p2]) / (h[p2] - h[p1]);
    }
    
    /**
     * Scans the image and builds the required contour values. 
     * <p>
     * Note: this method is only called when contour levels are being set 
     * according to a specified interval rather than user-supplied levels.
     * 
     * @return the contour levels
     */
    private List<Double> buildContourLevels() {
        double minVal = 0, maxVal = 0;
        boolean first = true;
        
        RectIter iter = RectIterFactory.create(getSourceImage(0), getBounds());
        boolean hasNonNan = false;
        
        moveIterToBand(iter, this.band);
        
        // scan all the pixels
        iter.startLines();
        while (!iter.finishedLines()) {
            iter.startPixels();
            while (!iter.finishedPixels()) {
                double val = iter.getSampleDouble();
                if (!Double.isNaN(val)) {
                    hasNonNan = true;
                    if (first) {
                        minVal = maxVal = val;
                        first = false;
                    } else if (val < minVal) {
                        minVal = val;
                    } else if (val > maxVal) {
                        maxVal = val;
                    }
                }
                iter.nextPixel();
            }
            iter.nextLine();
        }

        if (!hasNonNan) return Collections.emptyList();
        
        double z = Math.floor(minVal / contourInterval) * contourInterval;
        if (CompareOp.acompare(z, minVal) < 0) z += contourInterval;
        
        List<Double> result = new ArrayList<Double>();
        while (CompareOp.acompare(z, maxVal) <= 0) {
            result.add(z);
            z += contourInterval;
        }
        
        return result;
    }

    /**
     * Positions an image iterator at the specified band.
     * 
     * @param iter the iterator
     * @param targetBand the band 
     */
    private void moveIterToBand(RectIter iter, int targetBand) {
        int iband = 0;
        iter.startBands();
        
        while(iband < targetBand && !iter.nextBandDone()) {
            iband++;
        }
        
        if(iband != targetBand) {
            throw new IllegalArgumentException("Band " + targetBand + " not found, max band is " + iband);
        }
    }

    /**
     * Tests if a value should be treated as NODATA.
     * Values that are NaN, infinite or equal to Double.MAX_VALUE
     * are always treated as NODATA.
     * 
     * @param value the value to test
     * 
     * @return {@code true} if a NODATA value; {@code false} otherwise
     */
    private boolean isNoData(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) ||
            Double.compare(value, Double.MAX_VALUE) == 0) {
            return true;
        }

        for (Double d : noDataNumbers) {
            if (CompareOp.aequal(value, d)) {
                return true;
            }
        }

        for (Range r : noDataRanges) {
            if (r.contains(value)) {
                return true;
            }
        }

        return false;
    }
}
