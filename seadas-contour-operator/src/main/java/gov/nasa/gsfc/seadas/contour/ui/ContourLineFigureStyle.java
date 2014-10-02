package gov.nasa.gsfc.seadas.contour.ui;

import com.bc.ceres.binding.PropertyDescriptor;
import com.bc.ceres.swing.figure.FigureStyle;
import com.bc.ceres.swing.figure.support.DefaultFigureStyle;
import org.esa.beam.framework.datamodel.VectorDataNode;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 7/11/14
 * Time: 2:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourLineFigureStyle extends DefaultFigureStyle {
    public static final PropertyDescriptor STROKE_STYLE = createStrokeStyleDescriptor();

    private VectorDataNode vectorDataNode;

    public ContourLineFigureStyle(VectorDataNode vectorDataNode) {
         this.vectorDataNode = vectorDataNode;
    }

    private static PropertyDescriptor createStrokeColorDescriptor() {
        return createPropertyDescriptor("stroke-color", Color.class, null, false);
    }

    private static PropertyDescriptor createStrokeStyleDescriptor() {
        return createPropertyDescriptor("stroke", BasicStroke.class, null, false);
    }

    private static PropertyDescriptor createPropertyDescriptor(String propertyName, Class type, Object defaultValue, boolean notNull) {
        PropertyDescriptor descriptor = new PropertyDescriptor(propertyName, type);
        descriptor.setDefaultValue(defaultValue);
        descriptor.setNotNull(notNull);
        return descriptor;
    }

    public static FigureStyle createContourLineStyle(VectorDataNode vectorDataNode){
        Stroke dashedStroke = new BasicStroke((float) getGridLineWidthPixels(vectorDataNode), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{(float) getDashLengthPixels(vectorDataNode)}, 0);
        FigureStyle lineStyle = ContourLineFigureStyle.createLineStyle(new Color(255, 255, 255, 200),
                                                                           dashedStroke);
        lineStyle.setValue(STROKE_STYLE.getName(), dashedStroke);
        return lineStyle;
    }

    private static double getOpacity(Color strokeColor) {
        return Math.round(100.0 / 255.0 * strokeColor.getAlpha()) / 100.0;
    }

    private static double  getGridLineWidthPixels(VectorDataNode vectorDataNode) {
        double gridLineWidthPts = 0.8;

        return getPtsToPixelsMultiplier(vectorDataNode) * gridLineWidthPts;
    }

    private static double getDashLengthPixels(VectorDataNode vectorDataNode) {
        double dashLengthPts = 3;

        return getPtsToPixelsMultiplier(vectorDataNode) * dashLengthPts;
    }
    private static double getPtsToPixelsMultiplier(VectorDataNode vectorDataNode) {
         double ptsToPixelsMultiplier = -1.0;
        if (ptsToPixelsMultiplier == -1.0) {
            final double PTS_PER_INCH = 72.0;
            final double PAPER_HEIGHT = 11.0;
            final double PAPER_WIDTH = 8.5;
            double heightToWidthRatioPaper = (PAPER_HEIGHT) / (PAPER_WIDTH);
            double rasterHeight = vectorDataNode.getProduct().getSceneRasterHeight();
            double rasterWidth = vectorDataNode.getProduct().getSceneRasterWidth();
            double heightToWidthRatioRaster = rasterHeight / rasterWidth;

            if (heightToWidthRatioRaster > heightToWidthRatioPaper) {
                // use height
                ptsToPixelsMultiplier = (1 / PTS_PER_INCH) * (rasterHeight / (PAPER_HEIGHT));
            } else {
                // use width
                ptsToPixelsMultiplier = (1 / PTS_PER_INCH) * (rasterWidth / (PAPER_WIDTH));
            }
        }

        return ptsToPixelsMultiplier;
    }
}
