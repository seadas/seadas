package gov.nasa.gsfc.seadas.bathymetry.util;

import java.io.*;

/**
 */
public interface ImageDescriptor {

    int getImageWidth();

    int getImageHeight();

    int getTileWidth();

    int getTileHeight();

    File getAuxdataDir();

    String getZipFileName();
}
