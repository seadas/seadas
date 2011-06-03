package gov.nasa.obpg.seadas.sandbox.layer;

import com.bc.ceres.core.ExtensionFactory;
import com.bc.ceres.glayer.LayerTypeRegistry;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.product.VectorDataLayerType;

/**
 * An extension factory that returns a {@link TextLayerType}
 * when BEAM requests a {@link VectorDataLayerType} for a given
 * {@link VectorDataNode}.
 * <p/>
 * The factory will only return an instance of {@code TextLayerType}, if
 * the {@link VectorDataNode}'s feature type has a "text" or "label" attribute.
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
public class VectorDataNodeToTextLayerTypeAdapter implements ExtensionFactory {

    @Override
    public Object getExtension(Object object, Class<?> extensionType) {
        VectorDataNode node = (VectorDataNode) object;
        if (node.getFeatureType().getDescriptor("text") != null
                || node.getFeatureType().getDescriptor("label") != null) {
            return LayerTypeRegistry.getLayerType(TextLayerType.class);
        }
        return null;
    }

    @Override
    public Class<?>[] getExtensionTypes() {
        return new Class<?>[] {VectorDataLayerType.class};
    }

}
