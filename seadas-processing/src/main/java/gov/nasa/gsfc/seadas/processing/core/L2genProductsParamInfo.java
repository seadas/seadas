package gov.nasa.gsfc.seadas.processing.core;

import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genAlgorithmInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genProductCategoryInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genBaseInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genProductInfo;
import org.esa.beam.util.StringUtils;

import java.util.*;
import java.util.HashSet;

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
        }
    }


    protected void addProductInfo(L2genProductInfo productInfo) {
        productInfos.add(productInfo);
    }

    protected void clearProductInfos() {
        productInfos.clear();
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


