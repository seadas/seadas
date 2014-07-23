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

import javax.media.jai.ROI;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The image factory for the Contour operator.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ContourRIF implements RenderedImageFactory {

    /**
     * Creates a new instance of ContourOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the parameters
     *        "roi", "band", "outsideValues" and "insideEdges"
     *
     * @param renderHints rendering hints (ignored)
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        Object obj = null;
        
        ROI roi = (ROI) paramBlock.getObjectParameter(ContourDescriptor.ROI_ARG);
        int band = paramBlock.getIntParameter(ContourDescriptor.BAND_ARG);
        
        List<Double> contourLevels = null;
        Double interval = null;
        
        Collection levels = (Collection) paramBlock.getObjectParameter(ContourDescriptor.LEVELS_ARG);
        if (levels != null && !levels.isEmpty()) {
            contourLevels = new ArrayList<Double>();
            for (Object val : levels) {
                contourLevels.add(((Number)val).doubleValue());
            }
        } else {
            // No contour levels - use interval parameter
            obj = paramBlock.getObjectParameter(ContourDescriptor.INTERVAL_ARG);
            interval = ((Number)obj).doubleValue();
        }
        
        Collection noDataValues = (Collection) paramBlock.getObjectParameter(ContourDescriptor.NO_DATA_ARG);
        Boolean strictNodata = (Boolean) paramBlock.getObjectParameter(ContourDescriptor.STRICT_NO_DATA_ARG);
        Boolean simplify = (Boolean) paramBlock.getObjectParameter(ContourDescriptor.SIMPLIFY_ARG);
        Boolean smooth = (Boolean) paramBlock.getObjectParameter(ContourDescriptor.SMOOTH_ARG);

        return new ContourOpImage(paramBlock.getRenderedSource(0), 
                roi, band, contourLevels, interval, noDataValues,
                strictNodata, simplify, smooth);
    }
}
