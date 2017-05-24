package gov.nasa.gsfc.seadas.processing.processor;

import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.ocssw.OCSSW;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 7/15/13
 * Time: 11:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class MultilevelProcessorModel extends ProcessorModel {
    public MultilevelProcessorModel(String name, String parXMLFileName, OCSSW ocssw) {
        super(name, parXMLFileName, ocssw);
    }


//    public String[] getProgramCmdArray() {
//        final String PAR_EQUAL = "par=";
//
//        String[] commandArray = super.getProgramCmdArray();
//
//        for (int i = 0; i < commandArray.length; i++) {
//
//            if (commandArray[i] != null && commandArray[i].startsWith(PAR_EQUAL)) {
//                commandArray[i] = commandArray[i].substring(PAR_EQUAL.length());
//            }
//        }
//
//        return commandArray;
//    }


}
