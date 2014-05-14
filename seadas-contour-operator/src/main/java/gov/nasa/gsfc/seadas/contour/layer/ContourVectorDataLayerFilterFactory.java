package gov.nasa.gsfc.seadas.contour.layer;

import com.bc.ceres.glayer.Layer;
import com.bc.ceres.glayer.LayerFilter;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.product.VectorDataLayer;
import org.esa.beam.framework.ui.product.VectorDataLayerFilterFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 4/17/14
 * Time: 4:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContourVectorDataLayerFilterFactory extends VectorDataLayerFilterFactory {
        public static LayerFilter createContourFilter() {
            return new TypeNameFilter(Product.GEOMETRY_FEATURE_TYPE_NAME);
        }

        private static class TypeNameFilter implements LayerFilter {

            private final String featureTypeName;

            private TypeNameFilter(String featureTypeName) {
                this.featureTypeName = featureTypeName;
            }

            @Override
            public boolean accept(Layer layer) {
                return layer instanceof VectorDataLayer
                        && featureTypeName.equals(((VectorDataLayer) layer).getVectorDataNode().getFeatureType().getTypeName());
            }
        }

        private static class NodeFilter implements LayerFilter {

            private final VectorDataNode vectorDataNode;

            private NodeFilter(VectorDataNode vectorDataNode) {
                this.vectorDataNode = vectorDataNode;
            }

            @Override
            public boolean accept(Layer layer) {
                return layer instanceof VectorDataLayer
                        && (((VectorDataLayer) layer).getVectorDataNode() == vectorDataNode);
            }
        }
    }

