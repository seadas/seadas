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
    private ArrayList<WavelengthInfo> waveLimiter;


    public L2prodParamInfo(ArrayList<WavelengthInfo> waveLimiter) {
        super(L2genData.L2PROD);
        setType(Type.STRING);
        this.waveLimiter = waveLimiter;
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
                if (aInfo.hasChildren()) {
                    AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                    if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.ALL)) {
                        l2prod.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.ALL));
                    } else {
                        if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.IR)) {
                            l2prod.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.IR));
                        }
                        if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.VISIBLE)) {
                            l2prod.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.VISIBLE));
                        }

                        for (BaseInfo wInfo : aInfo.getChildren()) {
                            WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.VISIBLE) && !algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.VISIBLE)) {
                                if (wInfo.isSelected()) {
                                    l2prod.add(wavelengthInfo.getFullName());
                                }
                            }

                            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.INFRARED) && !algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.IR)) {
                                if (wInfo.isSelected()) {
                                    l2prod.add(wavelengthInfo.getFullName());
                                }
                            }
                        }
                    }
                } else {
                    if (aInfo.isSelected()) {
                        l2prod.add(aInfo.getFullName());
                    }
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

                    if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.NONE) {
                        if (inProducts.contains(algorithmInfo.getFullName())) {
                            algorithmInfo.setState(AlgorithmInfo.State.SELECTED);
                        } else {
                            algorithmInfo.setState(AlgorithmInfo.State.NOT_SELECTED);
                        }
                    } else {
                        for (BaseInfo wInfo : aInfo.getChildren()) {
                            WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                            if (inProducts.contains(wavelengthInfo.getFullName())) {
                                wavelengthInfo.setState(WavelengthInfo.State.SELECTED);
                            } else {
                                wavelengthInfo.setState(WavelengthInfo.State.NOT_SELECTED);
                            }
                        }

                        if (inProducts.contains(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.VISIBLE))) {
                            algorithmInfo.setStateShortcut(AlgorithmInfo.ShortcutType.VISIBLE, AlgorithmInfo.State.SELECTED);
                        }

                        if (inProducts.contains(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.IR))) {
                            algorithmInfo.setStateShortcut(AlgorithmInfo.ShortcutType.IR, AlgorithmInfo.State.SELECTED);
                        }

                        if (inProducts.contains(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.ALL))) {
                            algorithmInfo.setStateShortcut(AlgorithmInfo.ShortcutType.ALL, AlgorithmInfo.State.SELECTED);
                        }
                    }
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

    public void resetProductInfos(ArrayList<WavelengthInfo> waveLimiter) {

        for (ProductInfo productInfo : productInfos) {
            productInfo.setSelected(false);
            for (BaseInfo aInfo : productInfo.getChildren()) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;
                algorithmInfo.setSelected(false);
                if (algorithmInfo.getParameterType() != AlgorithmInfo.ParameterType.NONE) {
                    algorithmInfo.clearChildren();
                    for (WavelengthInfo wavelengthInfo : waveLimiter) {
                        boolean addWavelength = false;

                        if (algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.ALL) {
                            addWavelength = true;
                        } else if (wavelengthInfo.getWavelength() >= WavelengthInfo.INFRARED_LOWER_LIMIT &&
                                algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.IR) {
                            addWavelength = true;
                        } else if (wavelengthInfo.getWavelength() <= WavelengthInfo.VISIBLE_UPPER_LIMIT &&
                                algorithmInfo.getParameterType() == AlgorithmInfo.ParameterType.VISIBLE) {
                            addWavelength = true;
                        }

                        if (addWavelength) {
                            WavelengthInfo newWavelengthInfo = new WavelengthInfo(wavelengthInfo.getWavelength());
                            newWavelengthInfo.setParent(algorithmInfo);
                            newWavelengthInfo.setDescription(algorithmInfo.getDescription() + ", at " + newWavelengthInfo.getWavelengthString());
                            algorithmInfo.addChild(newWavelengthInfo);
                        }
                    }
                }
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


