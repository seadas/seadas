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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import org.jaitools.jts.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * A container for the segments collected by ContourOpImage. 
 * It will return them as merged lines eventually applying simplification procedures 
 * 
 * @author Andrea Aime - GeoSolutions
 * @since 1.1
 * @version $Id$
 */
class Segments {
    static final int MAX_SIZE = 16348; // this amounts to 130KB storage
    boolean simplify;
    double[] ordinates;
    int idx = 0;
    List<LineString> result = new ArrayList<LineString>();
    
    public Segments(boolean simplify) {
        this.simplify = simplify;
    }
    
    /**
     * Adds a segment to the mix
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void add(double x1, double y1, double x2, double y2) {
        if(ordinates == null) {
            ordinates = new double[512];
        } else if((idx + 4) > ordinates.length) {
            // reallocate
            double[] temp = new double[ordinates.length * 2];
            System.arraycopy(ordinates, 0, temp, 0, ordinates.length);
            ordinates = temp;
        }
        ordinates[idx++] = x1;
        ordinates[idx++] = y1;
        ordinates[idx++] = x2;
        ordinates[idx++] = y2;
        
        if(idx >= MAX_SIZE) {
            merge();
        }
    }
    
    /**
     * Returns the merged and eventually simplified segments
     * @return
     */
    public List<LineString> getMergedSegments() {
        if(idx > 0) {
            merge();
            // release the part of the storage we don't need anymore
            ordinates = null;
        }
        return result;
    }

    /**
     * Merges and eventually simplifies all of the segments collected so far with the 
     * linestring collected so far
     */
    void merge() {
        // merge all the segments
        LineMerger merger = new LineMerger();
        for (int i = 0; i < idx;) {
            Coordinate p1 = new Coordinate(ordinates[i++], ordinates[i++]);
            Coordinate p2 = new Coordinate(ordinates[i++], ordinates[i++]);
            if(!p1.equals2D(p2)) {
                merger.add(Utils.getGeometryFactory().createLineString(new Coordinate[] {p1, p2}));
            }
        }
        // reset the counter, we offloaded all segments
        idx = 0;
        // add to the mix all lines that have not been merged so far
        // (we know linear rings are already complete so we skip them)
        for (LineString ls : result) {
            merger.add(ls);
        }
        // eventually simplify and add back the merged line strings
        Collection<LineString> mergedLines = merger.getMergedLineStrings();
        result.clear();
        if(simplify) {
            for (LineString merged : mergedLines) {
                if(simplify) {
                    merged = Utils.removeCollinearVertices(merged);
                }
                result.add(merged);
            }
        } else {
            result.addAll(mergedLines);
        }
    }
}
