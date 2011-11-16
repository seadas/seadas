package gov.nasa.obpg.seadas.dataio.obpg;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.beam.framework.dataio.ProductIOException;
import org.esa.beam.framework.datamodel.*;
import ucar.nc2.Attribute;

import java.util.HashMap;
import java.util.List;

import static gov.nasa.obpg.seadas.dataio.obpg.ObpgUtils.SEAWIFS_L1A_TYPE;

/**
 * Created by IntelliJ IDEA.
 * User: seadas
 * Date: 11/14/11
 * Time: 2:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class SeadasMappedFileReader extends SeadasFileReader {


    SeadasMappedFileReader(SeadasProductReader productReader) {
        super(productReader);
    }


    @Override
    public Product createProduct() throws ProductIOException {


        return null;

    }


}
