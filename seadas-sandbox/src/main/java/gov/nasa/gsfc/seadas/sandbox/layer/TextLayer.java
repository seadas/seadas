package gov.nasa.gsfc.seadas.sandbox.layer;

import com.bc.ceres.binding.PropertySet;
import com.bc.ceres.grender.Rendering;
import com.vividsolutions.jts.geom.Geometry;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.product.VectorDataLayer;
import org.esa.beam.framework.ui.product.VectorDataLayerType;
import org.opengis.feature.simple.SimpleFeature;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * A text layer is used to display text labels.
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class TextLayer extends VectorDataLayer {

    public TextLayer(VectorDataLayerType vectorDataLayerType, VectorDataNode vectorDataNode, PropertySet configuration) {
        super(vectorDataLayerType, vectorDataNode, configuration);
    }

    @Override
    protected void renderLayer(Rendering rendering) {
        super.renderLayer(rendering);
        drawText(rendering);
    }

    private void drawText(Rendering rendering) {

        AffineTransform m2v = rendering.getViewport().getModelToViewTransform();
        Graphics2D g = rendering.getGraphics();

        double pModel[] = new double[2];
        double pView[] = new double[2];

        SimpleFeature[] features = getVectorDataNode().getFeatureCollection().toArray(new SimpleFeature[0]);

        for (SimpleFeature feature : features) {
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            com.vividsolutions.jts.geom.Point centroid = geometry.getCentroid();
            pModel[0] = centroid.getX();
            pModel[1] = centroid.getY();
            m2v.transform(pModel, 0, pView, 0, 1);

            g.translate(pView[0], pView[1]);

            String text = getText(feature);

            Font font = g.getFont();
            FontMetrics fontMetrics = g.getFontMetrics(font);
            Rectangle2D bounds = fontMetrics.getStringBounds(text, g);

            g.setPaint(new Color(255, 255, 255, 127));
            g.fill(bounds);
            g.setPaint(new Color(0, 0, 0, 127));
            g.draw(bounds);

            g.setPaint(Color.BLACK);
            g.drawString(text, (float) bounds.getX(), (float) (bounds.getY() + fontMetrics.getLeading() + fontMetrics.getAscent()));

            g.translate(-pView[0], -pView[1]);
        }
    }

    private String getText(SimpleFeature feature) {
        Object textObj = feature.getAttribute("text");
        if (textObj == null) {
            textObj = feature.getAttribute("label");
        } else {
            textObj = feature.getID();
        }
        return textObj.toString();
    }

}
