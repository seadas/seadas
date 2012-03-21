package gov.nasa.gsfc.seadas.processing.general;

import gov.nasa.gsfc.seadas.ocssw.OCSSW;
import gov.nasa.gsfc.seadas.processing.l2gen.ParamInfo;
import org.esa.beam.visat.VisatApp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Aynur Abdurazik (aabduraz)
 * Date: 3/16/12
 * Time: 2:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class Processor {

    private String name;
    private String location;
    private ArrayList<ParamInfo> paramList;
    private boolean acceptsParFile;
    private String paramXmlFileName;

    public Processor(String name, String paramXmlFileName){
        this.name = name;
        this.paramXmlFileName = paramXmlFileName;
        location = computeProcessorLocation();
        paramList = ParamUtils.computeParamList(paramXmlFileName);
        acceptsParFile = false;
    }


    public String getName() {
        return name;
    }


    public String getLocation(){
        return location;
    }

    public ArrayList getParamList(){

        return paramList;
    }

    public void setAcceptsParFile(boolean acceptsParFile){
        this.acceptsParFile = acceptsParFile;
    }

    public boolean acceptsParFile(){
        return acceptsParFile;
    }

    private String computeProcessorLocation(){

        final File ocsswRoot;
        try {
            ocsswRoot = OCSSW.getOcsswRoot();
        } catch (IOException e) {
            VisatApp.getApp().showErrorDialog(name, e.getMessage());
            return null;
        }

        final String ocsswArch;
        try {
            ocsswArch = OCSSW.getOcsswArch();
        } catch (IOException e) {
            VisatApp.getApp().showErrorDialog(name, e.getMessage());
            return null;
        }

        final String location = ocsswRoot.getPath() + "/run/bin/" + ocsswArch + "/" + name;
        return location;
    }


    private void computeParFile(){

    }
}
