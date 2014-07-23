/* 
 *  Copyright (c) 2010, Michael Bedward. All rights reserved. 
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

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;
import javax.media.jai.util.Range;
import java.awt.image.renderable.ParameterBlock;
import java.util.Arrays;
import java.util.Collection;

/**
 * Describes the "Contour" operation in which contour lines are interpolated
 * from values in a source image band. The desired contour values can be 
 * specified either by supplying a {@code Collection} of values via the
 * "levels" parameter, or a single value via the "interval" parameter. In the
 * interval case, the resulting contour values will be integer multiples of 
 * the interval. If both parameters are supplied the "levels" parameter takes
 * preference.
 * <p>
 * Contours are returned as a destination image property in the form of
 * a {@code Collection} of {@link com.vividsolutions.jts.geom.LineString} objects.
 * The source image value associated with each contour can be retrieved
 * with the {@link com.vividsolutions.jts.geom.LineString#getUserData()} method.
 * <p>
 * Source image pixels are passed through to the destination image unchanged.
 * <p>
 * Three boolean parameters control the form of the generated contours:
 * <ol type="1">
 * <li> 
 * <b>simplify</b>: if {@code true} (the default) colinear vertices are removed
 * to reduce contour memory requirements.
 * <li>
 * <b>mergeTiles</b>: if {@code true} (the default) contour lines are merged into
 * {@code LineStrings} across image tile boundaries; if {@code false} mergine
 * is skipped for faster processing.
 * </li>
 * <li>
 * <b>smooth</b>: if {@code true} contours are smoothed using Bezier interpolation
 * before being returned; if {@code false} (the default) no smoothing is done.
 * This option will probably have little effect on the form of contours unless
 * the source image resolution is coarse.
 * </li>
 * </ol>
 * Example of use:
 * <pre><code>
 * RenderedImage src = ...
 * ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
 * pb.setSource("source0", src);
 * 
 * // For contours at specific levels set the levels parameter
 * List&lt;Double&gt; levels = Arrays.asList(new double[]{1.0, 5.0, 10.0, 20.0, 50.0, 100.0});
 * pb.setParameter("levels", levels);
 * 
 * // Alternatively, set a constant interval between contours
 * pb.setParameter("interval", 10.0);
 * 
 * RenderedOp dest = JAI.create("Contour", pb);
 * Collection&lt;LineString&gt; contours = (Collection&lt;LineString&gt;) 
 *         dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
 * 
 * for (LineString contour : contours) {
 *   // get this contour's value
 *   Double contourValue = (Double) contour.getUserData();
 *   ...
 * }
 * </code></pre>
 * 
 * The interpolation algorithm used is that of Paul Bourke: originally published
 * in Byte magazine (1987) as the CONREC contouring subroutine written in
 * FORTRAN. The implementation here was adapted from Paul Bourke's C code for the
 * algorithm available at: 
 * <a href="http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/">
 * http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/</a>
 * 
 * <p>
 * <b>Summary of parameters:</b>
 * <table border="1", cellpadding="3">
 * <tr>
 * <th>Name</th>
 * <th>Class</th>
 * <th>Default</th>
 * <th>Description</th>
 * </tr>
 * 
 * <tr>
 * <td>roi</td>
 * <td>ROI</td>
 * <td>null</td>
 * <td>An optional ROI defining the area to contour.</td>
 * </tr>
 * 
 * <tr>
 * <td>band</td>
 * <td>Integer</td>
 * <td>0</td>
 * <td>Source image band to process.</td>
 * </tr>
 * 
 * <tr>
 * <td>levels</td>
 * <td>Collection</td>
 * <td>null</td>
 * <td>The values for which to generate contours.</td>
 * </tr>
 * 
 * <tr>
 * <td>interval</td>
 * <td>Number</td>
 * <td>null</td>
 * <td>
 * The interval between contour values. This parameter is ignored if the
 * levels parameter has been supplied. With interval contouring the minimum
 * and maximum contour values will be integer multiples of the interval 
 * parameter value.
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>nodata</td>
 * <td>Collection</td>
 * <td>
 * The set: {Double.NaN, Double.POSITIVE_INFINITY, 
    Double.NEGATIVE_INFINITY, Double.MAX_VALUE}
 * </td>
 * <td>
 * Values to be treated as NO_DATA. A value can be either a Number or a 
 * {@link javax.media.jai.util.Range} (mixtures of both are permitted).
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>strictNodata</td>
 * <td>Boolean</td>
 * <td>Boolean.TRUE</td>
 * <td>
 * If true, any NODATA values in the 2x2 cell moving window used by the 
 * contouring algorithm will cause that part of the image to be skipped.
 * If false, a single NODATA value will be permitted in the window. This
 * probably only makes a noticeable difference with small images.
 * </td>
 * </tr>
 * 
 * <tr>
 * <td>simplify</td>
 * <td>Boolean</td>
 * <td>Boolean.TRUE</td>
 * <td>Whether to simplify contour lines by removing collinear vertices.</td>
 * </tr>
 * 
 * <tr>
 * <td>smooth</td>
 * <td>Boolean</td>
 * <td>Boolean.FALSE</td>
 * <td>
 * Whether to smooth contour lines using Bezier interpolation. This probably
 * only makes a noticeable difference with small images.
 * </td>
 * </tr>
 * </table>
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ContourDescriptor extends OperationDescriptorImpl {
    
    /**
     * Constant identifying the image property that will hold the generated contours.
     */
    public final static String CONTOUR_PROPERTY_NAME = "contours";
    
    static final int ROI_ARG = 0;
    static final int BAND_ARG = 1;
    static final int LEVELS_ARG = 2;
    static final int INTERVAL_ARG = 3;
    static final int NO_DATA_ARG = 4;
    static final int STRICT_NO_DATA_ARG = 5;
    static final int SIMPLIFY_ARG = 6;
    static final int SMOOTH_ARG = 7;

    private static final String[] paramNames = {
        "roi",
        "band",
        "levels",
        "interval",
        "nodata",
        "strictNodata",
        "simplify",
        "smooth"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.ROI.class,
         Integer.class,
         Collection.class,
         Number.class,
         Collection.class,
         Boolean.class,
         Boolean.class,
         Boolean.class
    };

    // package access for use by ContourOpImage
    static final Object[] paramDefaults = {
         (ROI) null,
         Integer.valueOf(0),
         (Collection) null,
         (Number) null,
         Arrays.asList(Double.NaN, Double.POSITIVE_INFINITY, 
            Double.NEGATIVE_INFINITY, Double.MAX_VALUE),
         Boolean.TRUE,
         Boolean.TRUE,
         Boolean.FALSE,
    };

    

    /**
     * Creates a new instance.
     */
    public ContourDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Contour"},
                    {"LocalName", "Contour"},
                    {"Vendor", "org.jaitools.media.jai"},
                    {"Description", "Traces contours based on source image values"},
                    {"DocURL", "http://code.google.com/p/jaitools/"},
                    {"Version", "1.1.0"},
                    
                    {"arg0Desc", paramNames[0] + " an optional ROI"},
                    
                    {"arg1Desc", paramNames[1] + " (Integer, default=0) " +
                              "the source image band to process"},
                    
                    {"arg2Desc", paramNames[2] + " (Collection<? extends Number>) " +
                              "values for which to generate contours"},
                    
                    {"arg3Desc", paramNames[3] + " (Number) " +
                              "interval between contour values (ignored if levels arg is supplied)"},
                    
                    {"arg4Desc", paramNames[4] + " (Collection) " +
                              "values to be treated as NO_DATA; elements can be Number and/or Range"},

                    {"arg5Desc", paramNames[5] + " (Boolean, default=true) " +
                              "if true, use strict NODATA exclusion; if false use accept some NODATA"},

                    {"arg6Desc", paramNames[6] + " (Boolean, default=true) " +
                              "whether to simplify contour lines by removing colinear vertices"},
                    
                    {"arg7Desc", paramNames[7] + " (Boolean, default=false) " +
                              "whether to smooth contour lines using Bezier interpolation"}
                },
                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes
                
                1,                                              // number of sources
                
                paramNames,
                paramClasses,
                paramDefaults,
                    
                null                                            // valid values (none defined)
                );    
    }

    /**
     * Validates supplied parameters.
     * 
     * @param modeName the rendering mode
     * @param pb the parameter block
     * @param msg a {@code StringBuffer} to receive error messages
     * 
     * @return {@code true} if parameters are valid; {@code false} otherwise
     */
    @Override
    protected boolean validateParameters(String modeName, ParameterBlock pb, StringBuffer msg) {
        final String nodataErrorMsg =
                "nodata parameter must be a Collection of Numbers and/or Ranges";

        boolean ok = super.validateParameters(modeName, pb, msg);
        
        if (ok) {
            Collection levels = (Collection) pb.getObjectParameter(LEVELS_ARG);
            Object objInterval = pb.getObjectParameter(INTERVAL_ARG);
            
            if (levels == null || levels.isEmpty()) {
                // No levels: check for a valid contour interval
                if (objInterval == null) {
                    ok = false;
                    msg.append("One of levels or interval parameters must be supplied");

                } else {
                    Double interval = ((Number) objInterval).doubleValue();
                    if (interval.isNaN() || interval.isInfinite()) {
                        ok = false;
                        msg.append("interval parameter must not be NaN or infinite");
                    }
                }
            } else {
                // Levels parameter supplied
                if (levels.isEmpty()) {
                    ok = false;
                    msg.append("levels parameter must be a Collection of one or more numbers");
                }
            }

            Object objc = pb.getObjectParameter(NO_DATA_ARG);
            if (objc != null) {
                if (!(objc instanceof Collection)) {
                    msg.append(nodataErrorMsg);
                    ok = false;
                } else {
                    Collection col = (Collection) objc;
                    for (Object oelem : col) {
                        if (!(oelem instanceof Number || oelem instanceof Range)) {
                            msg.append(nodataErrorMsg);
                            ok = false;
                            break;
                        }
                    }
                }
            }
        }

        return ok;
    }    
}
