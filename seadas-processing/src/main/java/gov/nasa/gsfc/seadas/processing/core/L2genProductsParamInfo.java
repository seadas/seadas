package gov.nasa.gsfc.seadas.processing.core;

import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genAlgorithmInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genProductCategoryInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genBaseInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genProductInfo;

import java.util.*;
import java.util.HashSet;
import org.esa.snap.core.util.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: knowles
 * Date: 4/26/12
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class L2genProductsParamInfo extends ParamInfo {

    private ArrayList<L2genProductInfo> productInfos = new ArrayList<L2genProductInfo>();
    private ArrayList<L2genProductCategoryInfo> productCategoryInfos = new ArrayList<L2genProductCategoryInfo>();

    private ArrayList<L2genProductInfo> integerProductInfos = new ArrayList<L2genProductInfo>();

    ArrayList<String> integerL2prodList = new ArrayList<String>();

    public L2genProductsParamInfo() {
        super(L2genData.L2PROD);
        setType(Type.STRING);
    }


    protected void setValue(String value) {
        putValueIntoProductInfos(value);
        updateValue();
    }


    protected void updateValue() {
        super.setValue(getValueFromProductInfos());
    }


    private String getValueFromProductInfos() {
        ArrayList<String> l2prod = new ArrayList<String>();

        for (L2genProductInfo productInfo : productInfos) {
            for (L2genBaseInfo aInfo : productInfo.getChildren()) {
                L2genAlgorithmInfo algorithmInfo = (L2genAlgorithmInfo) aInfo;
                ArrayList<String> l2prodsThisAlgorithm = algorithmInfo.getL2prod();

                for (String l2prodThisAlgorithm : l2prodsThisAlgorithm) {
                    l2prod.add(l2prodThisAlgorithm);
                }
            }
        }

        for (String integerL2prod : integerL2prodList) {
            l2prod.add(integerL2prod);
        }

        Collections.sort(l2prod);

        return StringUtils.join(l2prod, " ");
    }


    private void putValueIntoProductInfos(String value) {

        if (value == null) {
            value = "";
        }

        // if product changed

        if (!value.equals(getValue())) {
            HashSet<String> inProducts = new HashSet<String>();

            // todo this is the l2prod separators adjustment
            if (value.contains(",")) {
                value = value.replaceAll(",", " ");
            }

//            if (value.contains(";")) {
//                value = value.replaceAll(";", " ");
//            }
//
//
//            if (value.contains(":")) {
//                value = value.replaceAll(":", " ");
//            }

            for (String prodEntry : value.split(" ")) {
                prodEntry.trim();
                inProducts.add(prodEntry);
            }

            //----------------------------------------------------------------------------------------------------
            // For every product in ProductInfoArray set selected to agree with inProducts
            //----------------------------------------------------------------------------------------------------

            for (L2genProductInfo productInfo : productInfos) {
                for (L2genBaseInfo aInfo : productInfo.getChildren()) {
                    L2genAlgorithmInfo algorithmInfo = (L2genAlgorithmInfo) aInfo;
                    algorithmInfo.setL2prod(inProducts);
                }
            }

            //todo DAN was here
            //----------------------------------------------------------------------------------------------------
            // Throw anything valid remaining in the custom integer products
            //----------------------------------------------------------------------------------------------------

            integerL2prodList.clear();

            for (L2genProductInfo productInfo : integerProductInfos) {
                for (L2genBaseInfo aInfo : productInfo.getChildren()) {
                    L2genAlgorithmInfo algorithmInfo = (L2genAlgorithmInfo) aInfo;

                    String prefix = algorithmInfo.getPrefix();
                    String suffix = algorithmInfo.getSuffix();

                    for (String inProduct : inProducts) {
                        String remnants = inProduct;

                        if (inProduct.startsWith(prefix)) {
                            // Strip off prefix
                            remnants = inProduct.replaceFirst(prefix, "");

                            if (suffix != null) {
                                if (inProduct.endsWith(suffix)) {
                                    // Strip off suffix
                                    remnants = remnants.substring(0, (remnants.length() - suffix.length()));
                                    if (isInteger(remnants)) {
                                        integerL2prodList.add(inProduct);
                                    }
                                }
                            } else {
                                if (isInteger(remnants)) {
                                    integerL2prodList.add(inProduct);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }


    protected void addProductInfo(L2genProductInfo productInfo) {
        productInfos.add(productInfo);
    }

    protected void clearProductInfos() {
        productInfos.clear();
    }

    protected void addIntegerProductInfo(L2genProductInfo integerProductInfo) {
        integerProductInfos.add(integerProductInfo);
    }

    protected void clearIntegerProductInfos() {
        integerProductInfos.clear();
    }

    protected void sortProductInfos(Comparator<L2genProductInfo> comparator) {
        Collections.sort(productInfos, comparator);
    }

    protected void resetProductInfos() {

        for (L2genProductInfo productInfo : productInfos) {
            productInfo.setSelected(false);
            for (L2genBaseInfo aInfo : productInfo.getChildren()) {
                L2genAlgorithmInfo algorithmInfo = (L2genAlgorithmInfo) aInfo;
                algorithmInfo.reset();
            }
        }
    }

    protected void setProductCategoryInfos() {
        for (L2genProductCategoryInfo productCategoryInfo : productCategoryInfos) {
            productCategoryInfo.clearChildren();
        }

        for (L2genProductCategoryInfo productCategoryInfo : productCategoryInfos) {
            for (String categorizedProductName : productCategoryInfo.getProductNames()) {
                for (L2genProductInfo productInfo : productInfos) {
                    if (categorizedProductName.equals(productInfo.getName())) {
                        productCategoryInfo.addChild(productInfo);
                    }
                }
            }
        }

        for (L2genProductInfo productInfo : productInfos) {
            boolean found = false;

            for (L2genProductCategoryInfo productCategoryInfo : productCategoryInfos) {
                for (String categorizedProductName : productCategoryInfo.getProductNames()) {
                    if (categorizedProductName.equals(productInfo.getName())) {
                        found = true;
                    }
                }
            }

            if (!found) {
                for (L2genProductCategoryInfo productCategoryInfo : productCategoryInfos) {
                    if (productCategoryInfo.isDefaultBucket()) {
                        productCategoryInfo.addChild(productInfo);
                    }
                }
            }
        }
    }


    public ArrayList<L2genProductCategoryInfo> getProductCategoryInfos() {
        return productCategoryInfos;
    }

    protected void addProductCategoryInfo(L2genProductCategoryInfo productCategoryInfo) {
        productCategoryInfos.add(productCategoryInfo);
    }

    protected void clearProductCategoryInfos() {
        productCategoryInfos.clear();
    }


}


