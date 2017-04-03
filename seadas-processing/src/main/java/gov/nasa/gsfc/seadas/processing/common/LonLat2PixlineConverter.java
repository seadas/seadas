package gov.nasa.gsfc.seadas.processing.common;

import gov.nasa.gsfc.seadas.processing.core.*;
import gov.nasa.gsfc.seadas.processing.core.ocssw.*;

import java.util.HashMap;

/**
 * Created by aabduraz on 7/7/16.
 */
public class LonLat2PixlineConverter {

    public static String LON_LAT_2_PIXEL_PROGRAM_NAME = "lonlat2pixel";
    public static final String START_LINE_PARAM_NAME = "sline";
    public static final String END_LINE_PARAM_NAME = "eline";
    public static final String START_PIXEL_PARAM_NAME = "spixl";
    public static final String END_PIXEL_PARAM_NAME = "epixl";

    private String sline;
    private String eline;
    private String spixl;
    private String epixl;

    OcsswCommandArrayManager commandArrayManager;

    ProcessorModel lonLat2PixelTranslatorProcessorModel;

    HashMap<String, String> pixelValues;

    public LonLat2PixlineConverter(ProcessorModel processorModel){
        lonLat2PixelTranslatorProcessorModel = processorModel;
    }


    public boolean computePixelsFromLonLat(){

        boolean converted = false;

        if (OCSSWOldModel.isLocal()) {
            commandArrayManager = new LocalOcsswCommandArrayManager(lonLat2PixelTranslatorProcessorModel);
            pixelValues = OCSSWRunnerOld.executeLocalLonLat2Pixel(commandArrayManager.getProgramCommandArray(), commandArrayManager.getIfileDir());
        } else {
            commandArrayManager = new RemoteOcsswCommandArrayManager(lonLat2PixelTranslatorProcessorModel);
            pixelValues = OCSSWRunnerOld.executeRemoteLonLat2Pixel(commandArrayManager.getProgramCommandArray());
        }

        if (pixelValues !=null) {
            setSline(pixelValues.get(START_LINE_PARAM_NAME));
            setEline(pixelValues.get(END_LINE_PARAM_NAME));
            setSpixl(pixelValues.get(START_PIXEL_PARAM_NAME));
            setEpixl(pixelValues.get(END_PIXEL_PARAM_NAME));
            converted = true;
        }
        return  converted;
    }

    public String getSline() {
        return sline;
    }

    public void setSline(String sline) {
        this.sline = sline;
    }

    public String getEline() {
        return eline;
    }

    public void setEline(String eline) {
        this.eline = eline;
    }

    public String getSpixl() {
        return spixl;
    }

    public void setSpixl(String spixl) {
        this.spixl = spixl;
    }

    public String getEpixl() {
        return epixl;
    }

    public void setEpixl(String epixl) {
        this.epixl = epixl;
    }
}
