package gov.nasa.gsfc.seadas.ocssw;

import gov.nasa.gsfc.seadas.processing.common.ParFileManager;
import gov.nasa.gsfc.seadas.processing.core.ParamInfo;
import gov.nasa.gsfc.seadas.processing.core.ProcessorModel;
import gov.nasa.gsfc.seadas.processing.utilities.SeadasArrayUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by aabduraz on 6/12/16.
 */
public class LocalOcsswCommandArrayManager extends OcsswCommandArrayManager {

    ParFileManager parFileManager;

    public LocalOcsswCommandArrayManager(ProcessorModel processorModel) {
        super(processorModel);
        parFileManager = new ParFileManager(processorModel);
    }

    /**
     * this method returns a command array for execution.
     * the array is constructed using the paramList data and input/output files.
     * the command array structure is: full pathname of the program to be executed, input file name, params in the required order and finally the output file name.
     * assumption: order starts with 1
     *
     * @return
     */
    public String[] getProgramCommandArray() {

        String[] cmdArrayPrefix = processorModel.getCmdArrayPrefix();
        String[] cmdArraySuffix = processorModel.getCmdArraySuffix();
        String[] cmdArrayForParams;

        if (processorModel.acceptsParFile()) {
            cmdArrayForParams = parFileManager.getCmdArrayWithParFile();

        } else {
            cmdArrayForParams = getCmdArrayParam();
        }

        //The final command array is the concatination of commandArrayPrefix, cmdArrayForParams, and commandArraySuffix
        cmdArray = SeadasArrayUtils.concatAll(cmdArrayPrefix, cmdArrayForParams, cmdArraySuffix);

        // get rid of the null strings
        ArrayList<String> cmdList = new ArrayList<String>();
        for (String s : cmdArray) {
            if (s != null) {
                cmdList.add(s);
            }
        }
        cmdArray = cmdList.toArray(new String[cmdList.size()]);

        return cmdArray;
    }

    private String[] getCmdArrayParam() {

        paramList = processorModel.getParamList();

        String[] cmdArrayParam = new String[paramList.getParamArray().size()];

        Iterator itr = paramList.getParamArray().iterator();

        ParamInfo option;
        int optionOrder;
        String optionValue;

        while (itr.hasNext()) {
            option = (ParamInfo) itr.next();
            optionOrder = option.getOrder();
            optionValue = option.getValue();
            if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_ARGUMENT)) {
                if (option.getValue() != null && option.getValue().length() > 0) {
                    cmdArrayParam[optionOrder] = optionValue;
                }
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_OPTION) && !option.getDefaultValue().equals(option.getValue())) {
                cmdArrayParam[optionOrder] = option.getName() + "=" + optionValue;
            } else if (option.getUsedAs().equals(ParamInfo.USED_IN_COMMAND_AS_FLAG) && (option.getValue().equals("true") || option.getValue().equals("1"))) {
                if (option.getName() != null && option.getName().length() > 0) {
                    cmdArrayParam[optionOrder] = option.getName();
                }
            }
        }
        return cmdArrayParam;
    }
}
