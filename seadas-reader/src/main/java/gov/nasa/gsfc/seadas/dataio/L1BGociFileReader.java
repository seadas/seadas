package gov.nasa.gsfc.seadas.dataio;

import org.esa.beam.dataio.netcdf.GenericNetCdfReader;
import ucar.nc2.iosp.hdf5.H5iosp;
import ucar.nc2.util.DebugFlagsImpl;

public class L1BGociFileReader extends GenericNetCdfReader {

    public L1BGociFileReader(L1BGociProductReaderPlugIn readerPlugIn) {
        super(readerPlugIn);
        H5iosp.setDebugFlags(new DebugFlagsImpl("HdfEos/turnOff"));
    }

}