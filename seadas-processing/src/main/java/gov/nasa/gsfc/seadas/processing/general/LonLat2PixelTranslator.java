package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.processing.core.*;

import java.util.HashMap;

/**
 * Created by aabduraz on 7/7/16.
 */
public class LonLat2PixelTranslator {

    public static final String START_LINE_PARAM_NAME = "sline";
    public static final String END_LINE_PARAM_NAME = "eline";
    public static final String START_PIXEL_PARAM_NAME = "spixl";
    public static final String END_PIXEL_PARAM_NAME = "spixl";

    private String ifile;
    private double elon;
    private double wlon;
    private double slat;
    private double nlat;

    private int sline;
    private int eline;
    private int spixl;
    private int epixl;

    OcsswCommandArrayManager commandArrayManager;

    ProcessorModel lonLat2PixelTranslatorProcessorModel;

    HashMap<String, String> pixelValues;

    public LonLat2PixelTranslator(String programName){
        lonLat2PixelTranslatorProcessorModel = new ProcessorModel(programName);
    }


    public void computePixelsFromLonLat(double elon, double wlon, double slat, double nlat){

        addParamInfo("elon", new Double(elon).toString(), ParamInfo.Type.STRING, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, lonLat2PixelTranslatorProcessorModel.getParamList().getParamArray().size());
        addParamInfo("wlon", new Double(wlon).toString(), ParamInfo.Type.STRING, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, lonLat2PixelTranslatorProcessorModel.getParamList().getParamArray().size());
        addParamInfo("slat", new Double(slat).toString(), ParamInfo.Type.STRING, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, lonLat2PixelTranslatorProcessorModel.getParamList().getParamArray().size());
        addParamInfo("nlat", new Double(nlat).toString(), ParamInfo.Type.STRING, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, lonLat2PixelTranslatorProcessorModel.getParamList().getParamArray().size());

        if (OCSSW.isLocal()) {
            commandArrayManager = new LocalOcsswCommandArrayManager(lonLat2PixelTranslatorProcessorModel);
            pixelValues = OCSSWRunner.executeLocalLonLat2Pixel(commandArrayManager.getProgramCommandArray(), commandArrayManager.getIfileDir());
        } else {
            commandArrayManager = new RemoteOcsswCommandArrayManager(lonLat2PixelTranslatorProcessorModel);
            pixelValues = OCSSWRunner.executeRemoteLonLat2Pixel(commandArrayManager.getProgramCommandArray());
        }

        setSline(pixelValues.get(START_LINE_PARAM_NAME));
        setEline(pixelValues.get(END_LINE_PARAM_NAME));
        setSpixl(pixelValues.get(START_PIXEL_PARAM_NAME));
        setEpixl(pixelValues.get(END_PIXEL_PARAM_NAME));
    }

    private void addParamInfo(String paramName, String paramValue, ParamInfo.Type paramType, String usedAs, int order){
        ParamInfo paramInfo = new ParamInfo(paramName, paramValue, paramType);
        paramInfo.setOrder(order);
        paramInfo.setUsedAs(usedAs);
        lonLat2PixelTranslatorProcessorModel.addParamInfo(paramInfo);
    }


    public String getIfile() {
        return ifile;
    }

    public void setIfile(String ifileName) {
        this.ifile = ifileName;
        addParamInfo("ifile", ifileName, ParamInfo.Type.IFILE, ParamInfo.USED_IN_COMMAND_AS_ARGUMENT, 0);
    }

    public int getSline() {
        return sline;
    }

    public void setSline(String sline) {
        this.sline = new Integer(sline).intValue();
    }

    public int getEline() {
        return eline;
    }

    public void setEline(String eline) {
        this.eline = new Integer(eline).intValue();;
    }

    public int getSpixl() {
        return spixl;
    }

    public void setSpixl(String spixl) {
        this.spixl = new Integer(spixl).intValue();;
    }

    public int getEpixl() {
        return epixl;
    }

    public void setEpixl(String epixl) {
        this.epixl = new Integer(epixl).intValue();;
    }
}
