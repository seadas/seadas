package gov.nasa.gsfc.seadas.processing.l2gen;

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
public class L2prodParamInfo extends ParamInfo {

    private ArrayList<ProductInfo> productInfos = new ArrayList<ProductInfo>();
    private ArrayList<ProductCategoryInfo> productCategoryInfos = new ArrayList<ProductCategoryInfo>();



    public L2prodParamInfo() {
        super(L2genData.L2PROD);
        setType(Type.STRING);
    }


    public void setValue(String value) {
        putValueIntoProductInfos(value);
        updateValue();
    }


    public void updateValue() {
        super.setValue(getValueFromProductInfos());
    }


    private String getValueFromProductInfos() {
        ArrayList<String> l2prod = new ArrayList<String>();

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;
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
            for (String prodEntry : value.split(" ")) {
                prodEntry.trim();
                inProducts.add(prodEntry);
            }

            //----------------------------------------------------------------------------------------------------
            // For every product in ProductInfoArray set selected to agree with inProducts
            //----------------------------------------------------------------------------------------------------

            for (ProductInfo productInfo : productInfos) {
                for (BaseInfo aInfo : productInfo.getChildren()) {
                    AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;
                    algorithmInfo.setL2prod(inProducts);
                }
            }
        }
    }


    public void addProductInfo(ProductInfo productInfo) {
        productInfos.add(productInfo);
    }

    public void clearProductInfos() {
        productInfos.clear();
    }

    public void sortProductInfos(Comparator<ProductInfo> comparator) {
        Collections.sort(productInfos, comparator);
    }

    public void resetProductInfos() {

        for (ProductInfo productInfo : productInfos) {
            productInfo.setSelected(false);
            for (BaseInfo aInfo : productInfo.getChildren()) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;
                algorithmInfo.reset();


//                algorithmInfo.setSelected(false);
//                if (algorithmInfo.getParameterType() != AlgorithmInfo.ParameterType.NONE) {
//                    algorithmInfo.clearChildren();
//                    for (WavelengthInfo wavelengthInfo : waveLimiterInfos) {
//                        boolean addWavelength = false;
//
//                        if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
//                            addWavelength = true;
//                        } else if (wavelengthInfo.getWavelength() >= WavelengthInfo.INFRARED_LOWER_LIMIT &&
//                                algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR) {
//                            addWavelength = true;
//                        } else if (wavelengthInfo.getWavelength() <= WavelengthInfo.VISIBLE_UPPER_LIMIT &&
//                                algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE) {
//                            addWavelength = true;
//                        }
//
//                        if (addWavelength) {
//                            WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
//                            newWavelengthInfo.setParent(algorithmInfo);
//                            newWavelengthInfo.setDescription(algorithmInfo.getDescription() + ", at " + newWavelengthInfo.getWavelengthString());
//                            algorithmInfo.addChild(newWavelengthInfo);
//                        }
//                    }
//                }
//

            }
        }
    }

    public void setProductCategoryInfos() {
        for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
            productCategoryInfo.clearChildren();
        }

        for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
            for (String categorizedProductName : productCategoryInfo.getProductNames()) {
                for (ProductInfo productInfo : productInfos) {
                    if (categorizedProductName.equals(productInfo.getName())) {
                        productCategoryInfo.addChild(productInfo);
                    }
                }
            }
        }

        for (ProductInfo productInfo : productInfos) {
            boolean found = false;

            for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
                for (String categorizedProductName : productCategoryInfo.getProductNames()) {
                    if (categorizedProductName.equals(productInfo.getName())) {
                        found = true;
                    }
                }
            }

            if (!found) {
                for (ProductCategoryInfo productCategoryInfo : productCategoryInfos) {
                    if (productCategoryInfo.isDefaultBucket()) {
                        productCategoryInfo.addChild(productInfo);
                    }
                }
            }
        }
    }


    public ArrayList<ProductCategoryInfo> getProductCategoryInfos() {
        return productCategoryInfos;
    }

    public void addProductCategoryInfo(ProductCategoryInfo productCategoryInfo) {
        productCategoryInfos.add(productCategoryInfo);
    }

    public void clearProductCategoryInfos() {
        productCategoryInfos.clear();
    }


}


