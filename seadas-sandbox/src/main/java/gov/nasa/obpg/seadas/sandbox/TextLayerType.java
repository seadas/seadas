package gov.nasa.obpg.seadas.sandbox;

import com.bc.ceres.binding.PropertySet;
import org.esa.beam.framework.datamodel.VectorDataNode;
import org.esa.beam.framework.ui.product.VectorDataLayerType;

/**
 * The descriptor and factory for {@link TextLayer}s.
 *
 * @author Norman Fomferra
 * @since SeaDAS 7.0
 */
@SuppressWarnings({"UnusedDeclaration"})
public class TextLayerType extends VectorDataLayerType {

    @Override
    protected TextLayer createLayer(VectorDataNode vectorDataNode, PropertySet configuration) {
        return new TextLayer(this, vectorDataNode, configuration);
    }

}

