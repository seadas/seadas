package gov.nasa.gsfc.seadas.processing.utilities;

import java.util.Arrays;

/**
 * Created by aabduraz on 7/25/16.
 */
public class SeadasArrayUtils {
    /**
     * Concatenating two arrays
     *
     * @param first  First array to be concatenated
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
     * Concatenating an arbitrary number of arrays
     *
     * @param first First array in the list of arrays
     * @param rest  Rest of the arrays in the list to be concatenated
     * @param <T>
     * @return
     */
    public static <T> T[] concatAll(T[] first, T[]... rest) {
        int totalLength = first.length;
        for (T[] array : rest) {
            if (array != null) {
                totalLength += array.length;
            }

        }
        T[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (T[] array : rest) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }

}
