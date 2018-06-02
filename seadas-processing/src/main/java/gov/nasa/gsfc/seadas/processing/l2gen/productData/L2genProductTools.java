package gov.nasa.gsfc.seadas.processing.l2gen.productData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.esa.snap.core.util.StringUtils;

/**
 * Created with IntelliJ IDEA.
 * User: knowles
 * Date: 5/28/13
 * Time: 2:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genProductTools {

    // this method takes the l2prod list and for every product matching the dependent wavelength naming convention
    // where the delimitor is "_" and an element of the product is an integer, then an "nnn" product will
    // be added to the list.
    // i.e.  "chlor_a aot_869" will become "chlor_a aot_869 aot_nnn"
    //       "chlor_a my_345_string_467_product" will become "chlor_a my_345_string_467_product my_nnn_string_467_product my_345_string_nnn_product"

    static public final String L2PROD_DELIMITER = " ";
    static public final String L2PROD_WAVELENGTH_DELIMITER = "_";
    static public final String SHORTCUT_NAMEPART_VISIBLE = "vvv";
    static public final String SHORTCUT_NAMEPART_IR = "iii";
    static public final String SHORTCUT_NAMEPART_ALL = "nnn";
    static public final int WAVELENGTH_FOR_IFILE_INDEPENDENT_MODE = -2;


    public static enum  ShortcutType {
        VISIBLE,
        IR,
        ALL
    }

    public static String convertShortcutType(L2genProductTools.ShortcutType shortcutType) {
        if (shortcutType == L2genProductTools.ShortcutType.ALL) {
            return L2genProductTools.SHORTCUT_NAMEPART_ALL;
        } else if (shortcutType == L2genProductTools.ShortcutType.IR) {
            return L2genProductTools.SHORTCUT_NAMEPART_IR;
        } else if (shortcutType == L2genProductTools.ShortcutType.VISIBLE) {
            return L2genProductTools.SHORTCUT_NAMEPART_VISIBLE;
        } else {
            return null;
        }
    }

    public static String convertToNNNProductList(String productList) {

        if (1 == 1) {
            return productList;
        }


        if (productList == null) {
            return productList;
        }

        ArrayList<String> products = new ArrayList<String>();

        // account for other product delimiters
        if (productList.contains(",")) {
            productList = productList.replaceAll(",", L2PROD_DELIMITER);
        }


        for (String currentProduct : productList.split(L2PROD_DELIMITER)) {
            String[] productParts = currentProduct.trim().split(L2PROD_WAVELENGTH_DELIMITER);

            // always add the current product
            products.add(currentProduct);

            // now add any wavelength shortcut variants of the current product
            StringBuilder currentUntamperedProductPrefix = new StringBuilder(productParts[0]);

            for (int i = 1; i < productParts.length; i++) {
                if (isInteger(productParts[i])) {
                    // possible wavelength found at index i
                    StringBuilder currentNNNProduct = new StringBuilder(currentUntamperedProductPrefix.toString());
                    currentNNNProduct.append(L2PROD_WAVELENGTH_DELIMITER + SHORTCUT_NAMEPART_ALL);


                    // loop through remaining parts to add on the suffix
                    for (int k = i + 1; k < productParts.length; k++) {
                        currentNNNProduct.append(L2PROD_WAVELENGTH_DELIMITER);
                        currentNNNProduct.append(productParts[k]);
                    }

                    products.add(currentNNNProduct.toString());

                }

                // add this part and continue on looking for wavelength in the next index
                currentUntamperedProductPrefix.append(L2PROD_WAVELENGTH_DELIMITER);
                currentUntamperedProductPrefix.append(productParts[i]);
            }
        }

        return StringUtils.join(products, L2PROD_DELIMITER);
    }


    public static boolean isInteger(String string) {
        try {
            Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
}
