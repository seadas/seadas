package gov.nasa.gsfc.seadas.processing.core;

import java.io.File;
import java.util.Arrays;

/**
 * Created by aabduraz on 6/12/16.
 */
public abstract class OcsswCommandArrayManager {

    ProcessorModel processorModel;
    public String[] cmdArray;
    protected ParamList paramList;
    private File ifileDir;

    OcsswCommandArrayManager(ProcessorModel processorModel) {
        this.processorModel = processorModel;
        paramList = processorModel.getParamList();
        ifileDir = processorModel.getIFileDir();
    }

    /**
     * Concatenating two arrays
     * @param first First array to be concatenated
     * @param second Second array to be concatenated
     * @param <T>
     * @return
     */
    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     *Concatenating an arbitrary number of arrays
     * @param first First array in the list of arrays
     * @param rest Rest of the arrays in the list to be concatenated
     * @param <T>
     * @return
     */
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public abstract String[] getProgramCommandArray();

    public File getIfileDir() {
        return ifileDir;
    }

    public void setIfileDir(File ifileDir) {
        this.ifileDir = ifileDir;
    }
}
