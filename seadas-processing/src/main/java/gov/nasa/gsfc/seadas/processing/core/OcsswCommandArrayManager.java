package gov.nasa.gsfc.seadas.processing.core;

import java.util.Arrays;

/**
 * Created by aabduraz on 6/12/16.
 */
public abstract class OcsswCommandArrayManager {

    ProcessorModel processorModel;
    public String[] cmdArray;
    protected ParamList paramList;

    OcsswCommandArrayManager(ProcessorModel processorModel) {
        this.processorModel = processorModel;
        paramList = processorModel.getParamList();
    }


    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    abstract String[] getProgramCommandArray();
}
