package gov.nasa.gsfc.seadas.processing.l2gen;

import org.esa.beam.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

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


    public L2prodParamInfo(String value) {
        super(L2genData.L2PROD);
        setType(Type.STRING);
        setValue(value);
    }


    public String getValue() {
        ArrayList<String> prodArrayList = new ArrayList<String>();

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                if (aInfo.hasChildren()) {
                    AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                    if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.ALL)) {
                        prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.ALL));
                    } else {
                        if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.IR)) {
                            prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.IR));
                        }
                        if (algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.VISIBLE)) {
                            prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.VISIBLE));
                        }

                        for (BaseInfo wInfo : aInfo.getChildren()) {
                            WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.VISIBLE) && !algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.VISIBLE)) {
                                if (wInfo.isSelected()) {
                                    prodArrayList.add(wavelengthInfo.getFullName());
                                }
                            }

                            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.INFRARED) && !algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.IR)) {
                                if (wInfo.isSelected()) {
                                    prodArrayList.add(wavelengthInfo.getFullName());
                                }
                            }
                        }
                    }
                } else {
                    if (aInfo.isSelected()) {
                        prodArrayList.add(aInfo.getFullName());
                    }
                }
            }
        }

        return StringUtils.join(prodArrayList, " ");
    }


    public void setValue(String value) {

        if (value == null) {
            value = "";
        }

        // if product changed

        if (!value.equals(getValue())) {
            TreeSet<String> inProducts = new TreeSet<String>();
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



    public String getDefaultValue() {
        ArrayList<String> prodArrayList = new ArrayList<String>();

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;
                if (aInfo.hasChildren()) {
                    if (algorithmInfo.isDefaultSelectedShortcut(AlgorithmInfo.ShortcutType.ALL)) {
                        prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.ALL));
                    } else {
                        if (algorithmInfo.isDefaultSelectedShortcut(AlgorithmInfo.ShortcutType.IR)) {
                            prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.IR));
                        }
                        if (algorithmInfo.isDefaultSelectedShortcut(AlgorithmInfo.ShortcutType.VISIBLE)) {
                            prodArrayList.add(algorithmInfo.getShortcutFullname(AlgorithmInfo.ShortcutType.VISIBLE));
                        }

                        for (BaseInfo wInfo : aInfo.getChildren()) {
                            WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;

                            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.VISIBLE) && !algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.VISIBLE)) {
                                if (wavelengthInfo.isDefaultSelected()) {
                                    prodArrayList.add(wavelengthInfo.getFullName());
                                }
                            }

                            if (wavelengthInfo.isWaveType(WavelengthInfo.WaveType.INFRARED) && !algorithmInfo.isSelectedShortcut(AlgorithmInfo.ShortcutType.IR)) {
                                if (wavelengthInfo.isDefaultSelected()) {
                                    prodArrayList.add(wavelengthInfo.getFullName());
                                }
                            }
                        }
                    }
                } else {
                    if (algorithmInfo.isDefaultSelected()) {
                        prodArrayList.add(aInfo.getFullName());
                    }
                }
            }
        }

        return StringUtils.join(prodArrayList, " ");
    }


    public boolean isDefault() {
        boolean isDefault = true;

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                if (aInfo.hasChildren()) {
                    for (BaseInfo wInfo : aInfo.getChildren()) {
                        WavelengthInfo wavelengthInfo = (WavelengthInfo) wInfo;
                        if (wavelengthInfo.isSelected() != wavelengthInfo.isDefaultSelected()) {
                            isDefault = false;
                        }
                        ;
                    }
                } else {
                    AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;
                    if (algorithmInfo.isSelected() != algorithmInfo.isDefaultSelected()) {
                        isDefault = false;
                    }
                }
            }
        }

        return isDefault;
    }


    public void setDefaultValue(String defaultValue) {
        setValue(defaultValue);
        copyValueToDefault();


    }

    public void copyValueToDefault() {

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                if (aInfo.hasChildren()) {
                    for (BaseInfo wInfo : aInfo.getChildren()) {
                        ((WavelengthInfo) wInfo).setDefaultSelected(wInfo.isSelected());
                    }
                } else {
                    ((AlgorithmInfo) aInfo).setDefaultSelected(aInfo.isSelected());
                }
            }
        }
    }

    public void setToDefault() {
        // This method loops through the entire productInfoArray setting all the states to the default state

        boolean productChanged = false;

        for (ProductInfo productInfo : productInfos) {
            for (BaseInfo aInfo : productInfo.getChildren()) {
                if (aInfo.hasChildren()) {
                    for (BaseInfo wInfo : aInfo.getChildren()) {
                        if (wInfo.isSelected() != ((WavelengthInfo) wInfo).isDefaultSelected()) {
                            wInfo.setSelected(((WavelengthInfo) wInfo).isDefaultSelected());
                            productChanged = true;
                        }
                    }
                } else {
                    if (aInfo.isSelected() != ((AlgorithmInfo) aInfo).isDefaultSelected()) {
                        aInfo.setSelected(((AlgorithmInfo) aInfo).isDefaultSelected());
                        productChanged = true;
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

    public void resetProductInfos(boolean missionChanged, ArrayList<WavelengthInfo> waveLimiter) {

        for (ProductInfo productInfo : productInfos) {
            productInfo.setSelected(false);
            for (BaseInfo aInfo : productInfo.getChildren()) {
                aInfo.setSelected(false);
                AlgorithmInfo algorithmInfo = (AlgorithmInfo) aInfo;

                if (algorithmInfo.getParameterType() != AlgorithmInfo.ParameterType.NONE) {
                    if (missionChanged) {
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
                    } else {
                        for (BaseInfo wInfo : algorithmInfo.getChildren()) {
                            wInfo.setSelected(false);
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


