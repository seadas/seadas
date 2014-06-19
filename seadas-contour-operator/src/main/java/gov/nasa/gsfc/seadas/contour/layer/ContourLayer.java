package gov.nasa.gsfc.seadas.contour.layer;

import com.bc.ceres.binding.PropertySet;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.product.VectorDataLayer;
import org.esa.beam.framework.ui.product.VectorDataLayerType;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 2/20/14
 * Time: 2:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourLayer extends VectorDataLayer {

    public static final Color STROKE_COLOR = Color.ORANGE;
    public static final double STROKE_OPACITY = 0.8;
    public static final double STROKE_WIDTH = 2.0;
    public static final double FILL_OPACITY = 0.5;
    public static final Color FILL_COLOR = Color.WHITE;

    //private final Paint strokePaint;


    public ContourLayer(VectorDataLayerType vectorDataLayerType, VectorDataNode vectorDataNode, PropertySet configuration) {
        super(vectorDataLayerType, vectorDataNode, configuration);
//        String styleCss = vectorDataNode.getDefaultStyleCss();
//        DefaultFigureStyle style = new DefaultFigureStyle(styleCss);
//        style.fromCssString(styleCss);
//        style.setSymbolName("circle");
//        style.setStrokeColor(STROKE_COLOR);
//        style.setStrokeWidth(STROKE_WIDTH);
//        style.setStrokeOpacity(STROKE_OPACITY);
//        style.setFillColor(FILL_COLOR);
//        style.setFillOpacity(FILL_OPACITY);
//        strokePaint = style.getStrokePaint();
//        vectorDataNode.setDefaultStyleCss(style.toCssString());
    }
}
