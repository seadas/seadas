package gov.nasa.obpg.seadas.sandbox.l2gen;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genXmlReader {

    private ArrayList<ProductInfo> waveIndependentProductInfoArray;
    private ArrayList<ProductInfo> waveDependentProductInfoArray;

    private static String ELEMNAME_PROD = "product";
    private static String ATTRIBNAME_PROD_NAME = "name";

    private static String ELEMNAME_ALG = "algorithm";
    private static String ATTRIBNAME_ALG_NAME = "name";

    private static String ELEMNAME_DESC = "description";
    private static String ELEMNAME_PARAMTYPE = "parameterType";
    private static String ELEMNAME_PREFIX = "prefix";
    private static String ELEMNAME_SUFFIX = "suffix";
    private static String ELEMNAME_DATATYPE = "dataType";
    private static String ELEMNAME_UNITS = "units";


    public L2genXmlReader() {
        waveIndependentProductInfoArray = new ArrayList<ProductInfo>();
        waveDependentProductInfoArray = new ArrayList<ProductInfo>();
    }

    public void parseXmlFile(String filename) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(filename);

        waveIndependentProductInfoArray.clear();
        waveDependentProductInfoArray.clear();


        NodeList prodNodelist = rootElement.getElementsByTagName(ELEMNAME_PROD);

        if (prodNodelist != null && prodNodelist.getLength() > 0) {
            for (int i = 0; i < prodNodelist.getLength(); i++) {


                Element prodElement = (Element) prodNodelist.item(i);

                String prodName = prodElement.getAttribute(ATTRIBNAME_PROD_NAME);

                //System.out.println("product=" + prodName);

                ProductInfo waveDependentProductInfo = null;
                ProductInfo waveIndependentProductInfo = null;
                // Boolean waveDependant = false;

                NodeList algNodelist = prodElement.getElementsByTagName(ELEMNAME_ALG);

                if (algNodelist != null && algNodelist.getLength() > 0) {
                    for (int j = 0; j < algNodelist.getLength(); j++) {

                        Element algElement = (Element) algNodelist.item(j);

                        AlgorithmInfo algorithmInfo = new AlgorithmInfo();

                        algorithmInfo.setProductName(prodName);

                        if (algElement.hasAttribute(ATTRIBNAME_ALG_NAME)) {
                            String algorithmName = algElement.getAttribute(ATTRIBNAME_ALG_NAME);
                            algorithmInfo.setName(algorithmName);
                        //    System.out.println("   algorithm=" + algorithmName);
                        } //                   else {
                          //  System.out.println("   algorithm=null");

                       // }

                        String description = XmlReader.getTextValue(algElement, ELEMNAME_DESC);
                        algorithmInfo.setDescription(description);

                        String parameterTypeStr = XmlReader.getTextValue(algElement, ELEMNAME_PARAMTYPE);
                        algorithmInfo.setParameterType(parameterTypeStr);

                        String suffix = XmlReader.getTextValue(algElement, ELEMNAME_SUFFIX);
                        algorithmInfo.setSuffix(suffix);

                        String prefix = XmlReader.getTextValue(algElement, ELEMNAME_PREFIX);
                        algorithmInfo.setPrefix(prefix);

                        String units = XmlReader.getTextValue(algElement, ELEMNAME_UNITS);
                        algorithmInfo.setUnits(units);

                        String dataType = XmlReader.getTextValue(algElement, ELEMNAME_DATATYPE);
                        algorithmInfo.setDataType(dataType);

//                        productInfo.addAlgorithmInfo(algorithmInfo);


                        if (algorithmInfo.getParameterType() != AlgorithmInfo.ParameterType.NONE) {
                            if (waveDependentProductInfo == null) {
                                waveDependentProductInfo = new ProductInfo(prodName);
                            }
                            waveDependentProductInfo.addAlgorithmInfo(algorithmInfo);
//                            waveDependentProductInfoArray.add(productInfo);

                        } else {
                            if (waveIndependentProductInfo == null) {
                                waveIndependentProductInfo = new ProductInfo(prodName);
                            }
                            waveIndependentProductInfo.addAlgorithmInfo(algorithmInfo);

//                            waveIndependentProductInfoArray.add(productInfo);
                        }

                    } // for algorithms

                    if (waveDependentProductInfo != null) {
                            waveDependentProductInfoArray.add(waveDependentProductInfo);
                    }
                    if (waveIndependentProductInfo != null) {
                            waveIndependentProductInfoArray.add(waveIndependentProductInfo);
                    }

                }


            } // for products
        }

        Collections.sort(waveDependentProductInfoArray, ProductInfo.CASE_SENSITIVE_ORDER);
        Collections.sort(waveIndependentProductInfoArray);



    }

    public ArrayList<ProductInfo> getWaveIndependentProductInfoArray() {
        return waveIndependentProductInfoArray;
    }

    public ArrayList<ProductInfo> getWaveDependentProductInfoArray() {
        return waveDependentProductInfoArray;
    }


}
