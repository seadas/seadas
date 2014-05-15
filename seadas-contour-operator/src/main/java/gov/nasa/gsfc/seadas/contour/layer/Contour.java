package gov.nasa.gsfc.seadas.contour.layer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import gov.nasa.gsfc.seadas.ContourDescriptor;
import gov.nasa.gsfc.seadas.contour.util.Java2DConverter;
import org.esa.beam.framework.datamodel.*;
import org.esa.beam.util.Guardian;
import org.esa.beam.visat.VisatApp;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/27/14
 * Time: 3:18 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * A geometric representation of a contour lines specified in levels with values.
 */
public class Contour {


    private final GeneralPath[] _linePaths;
    private final TextGlyph[] _textGlyphs;

    private Contour(GeneralPath[] paths, TextGlyph[] textGlyphs) {
        _linePaths = paths;
        _textGlyphs = textGlyphs;
    }

    public GeneralPath[] getLinePaths() {
        return _linePaths;
    }

    public TextGlyph[] getTextGlyphs() {
        return _textGlyphs;
    }

    /**
     * Creates a contour for the given product.
     *
     * @param product          the product
     * @param contourIntervals contour levels specified by a user
     * @param selectedBand     band selected for contour computation
     * @return the contour or null, if it could not be created
     */
    public static Contour create(Product product,
                                 Band selectedBand,
                                 ArrayList<Double> contourIntervals,
                                 AffineTransform affineTransform) {
        Guardian.assertNotNull("product", product);

        final GeoCoding geoCoding = product.getGeoCoding();
        if (geoCoding == null || product.getSceneRasterWidth() < 16 || product.getSceneRasterHeight() < 16) {
            return null;
        }

        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", selectedBand.getSourceImage());
        pb.setParameter("levels", contourIntervals);
        RenderedOp dest = JAI.create("Contour", pb);
        Collection<LineString> contours = (Collection<LineString>) dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);

        final GeneralPath[] paths = createPaths(contours);
        final TextGlyph[] textGlyphs = createTextGlyphs(contours);

        return new Contour(paths, textGlyphs);
    }

    private static GeneralPath[] createPaths(Collection<LineString> contours) {
        LineString[] lineStrings = contours.toArray(new LineString[contours.size()]);
        MultiLineString multiLineString = new MultiLineString(lineStrings, lineStrings[0].getFactory());
        final ArrayList<GeneralPath> generalPathList = new ArrayList<GeneralPath>();
        AffineTransform toViewTransform = VisatApp.getApp().getSelectedProductSceneView().getBaseImageToViewTransform();
        VisatApp.getApp().getSelectedProduct().getGeoCoding().getImageToMapTransform();
        AffineTransform toModelTransform = VisatApp.getApp().getSelectedProductSceneView().getSceneImage().getRasters()[0].getSourceImage().getModel().getImageToModelTransform(0);
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.concatenate(toViewTransform);       //this has to be here
        //affineTransform.concatenate(toModelTransform);         //this doesn't work
        GeneralPath generalPath = new GeneralPath();
        Java2DConverter java2DConverter = new Java2DConverter(affineTransform);
        //GeneralPath generalPath = (GeneralPath)java2DConverter.toShape(multiLineString);
        try {
             generalPath = (GeneralPath)java2DConverter.toShape(multiLineString);
         } catch (NoninvertibleTransformException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
        for (LineString lineString : contours) {
            //convertPixeltoLonLat(lineString, geoCoding);
            try {

                generalPath = (GeneralPath) java2DConverter.toShape(lineString);
            } catch (NoninvertibleTransformException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            generalPathList.add(generalPath);
        }
        return generalPathList.toArray(new GeneralPath[generalPathList.size()]);
    }

    private static void convertPixeltoLonLat(LineString lineString, GeoCoding geoCoding) {
        Coordinate[] coordinates = lineString.getCoordinates();

        final PixelPos pixelPos = new PixelPos();
        final GeoPos geoPos = new GeoPos();

        int numCoor = coordinates.length;
        for (int i = 0; i < numCoor - 1; i++) {
            pixelPos.setLocation(coordinates[i].x, coordinates[i].y);
            geoCoding.getGeoPos(pixelPos, geoPos);
            coordinates[i].x = geoPos.lat;
            coordinates[i].y = geoPos.lon;
        }

    }

    private static TextGlyph[] createTextGlyphs(Collection<LineString> contours) {
        final List<TextGlyph> textGlyphList = new ArrayList<TextGlyph>();
        return textGlyphList.toArray(new TextGlyph[textGlyphList.size()]);
    }

    //    private static TextGlyph[] createTextGlyphs(List<List<Coord>> parallelList, List<List<Coord>> meridianList) {
//        final List<TextGlyph> textGlyphList = new ArrayList<TextGlyph>();
//        createParallelTextGlyphs(parallelList, textGlyphList);
//        createMeridianTextGlyphs(meridianList, textGlyphList);
//        return textGlyphList.toArray(new TextGlyph[textGlyphList.size()]);
//    }
//
//
//    private static void createParallelTextGlyphs(List<List<Coord>> parallelList,
//                                                 List<TextGlyph> textGlyphList) {
//        Coord coord1;
//        Coord coord2;
//        for (final List<Coord> parallel : parallelList) {
//            if (parallel.size() >= 3) {
//                coord1 = parallel.get(1);
//                coord2 = parallel.get(2);
//                if (isCoordPairValid(coord1, coord2)) {
//                    textGlyphList.add(createLatTextGlyph(coord1, coord2));
//                }
//            } else if (parallel.size() >= 2) {
//                coord1 = parallel.get(0);
//                coord2 = parallel.get(1);
//                if (isCoordPairValid(coord1, coord2)) {
//                    textGlyphList.add(createLatTextGlyph(coord1, coord2));
//                }
//            }
//        }
//    }
//
//    private static void createMeridianTextGlyphs(List<List<Coord>> meridianList,
//                                                 List<TextGlyph> textGlyphList) {
//        Coord coord1;
//        Coord coord2;
//        for (List<Coord> meridian : meridianList) {
//            if (meridian.size() >= 3) {
//                coord1 = meridian.get(1);
//                coord2 = meridian.get(2);
//                if (isCoordPairValid(coord1, coord2)) {
//                    textGlyphList.add(createLonTextGlyph(coord1, coord2));
//                }
//            } else if (meridian.size() >= 2) {
//                coord1 = meridian.get(0);
//                coord2 = meridian.get(1);
//                if (isCoordPairValid(coord1, coord2)) {
//                    textGlyphList.add(createLonTextGlyph(coord1, coord2));
//                }
//            }
//        }
//    }
//
//    private static boolean isCoordPairValid(Coord coord1, Coord coord2) {
//        return coord1.pixelPos.isValid() && coord2.pixelPos.isValid();
//    }
//
//    private static TextGlyph createLatTextGlyph(Coord coord1, Coord coord2) {
//        return createTextGlyph(coord1.geoPos.getLatString(), coord1, coord2);
//    }
//
//    private static TextGlyph createLonTextGlyph(Coord coord1, Coord coord2) {
//        return createTextGlyph(coord1.geoPos.getLonString(), coord1, coord2);
//    }
//
//    private static TextGlyph createTextGlyph(String text, Coord coord1, Coord coord2) {
//        final float angle = (float) Math.atan2(coord2.pixelPos.y - coord1.pixelPos.y,
//                coord2.pixelPos.x - coord1.pixelPos.x);
//        return new TextGlyph(text, coord1.pixelPos.x, coord1.pixelPos.y, angle);
//    }
//
    private static float limitLon(float lon) {
        while (lon < -180f) {
            lon += 360f;
        }
        while (lon > 180f) {
            lon -= 360f;
        }
        return lon;
    }

    /**
     * Not used, but useful for debugging: DON'T delete this method!
     *
     * @param geoCoding   The geo-coding
     * @param geoBoundary The geo-boundary
     * @return the geo-boundary
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private static GeneralPath createPixelBoundaryPath(final GeoCoding geoCoding, final GeoPos[] geoBoundary) {
        final GeneralPath generalPath = new GeneralPath();
        boolean restart = true;
        for (final GeoPos geoPos : geoBoundary) {
            geoPos.lon = limitLon(geoPos.lon);
            final PixelPos pixelPos = geoCoding.getPixelPos(geoPos, null);
            if (pixelPos.isValid()) {
                if (restart) {
                    generalPath.moveTo(pixelPos.x, pixelPos.y);
                } else {
                    generalPath.lineTo(pixelPos.x, pixelPos.y);
                }
                restart = false;
            } else {
                restart = true;
            }
        }
        return generalPath;
    }

    public static class TextGlyph {

        private final String text;
        private final float x;
        private final float y;
        private final float angle;

        public TextGlyph(String text, float x, float y, float angle) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.angle = angle;
        }

        public String getText() {
            return text;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getAngle() {
            return angle;
        }
    }

    private static class Coord {
        GeoPos geoPos;
        PixelPos pixelPos;

        public Coord(GeoPos geoPos, PixelPos pixelPos) {
            this.geoPos = geoPos;
            this.pixelPos = pixelPos;
        }
    }

}

