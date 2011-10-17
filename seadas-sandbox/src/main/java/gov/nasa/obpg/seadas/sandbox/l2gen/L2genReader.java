package gov.nasa.obpg.seadas.sandbox.l2gen;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A ...
 *
 * @author Danny Knowles
 * @since SeaDAS 7.0
 */
public class L2genReader {

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

    private L2genData l2genData;

    public L2genReader(L2genData l2genData) {
        this.l2genData = l2genData;
    }


    public void readProductsXmlFile(String filename) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(filename);

        l2genData.clearWaveDependentProductInfoArray();
        l2genData.clearWaveIndependentProductInfoArray();


        NodeList prodNodelist = rootElement.getElementsByTagName(ELEMNAME_PROD);

        if (prodNodelist != null && prodNodelist.getLength() > 0) {
            for (int i = 0; i < prodNodelist.getLength(); i++) {

                Element prodElement = (Element) prodNodelist.item(i);

                String prodName = prodElement.getAttribute(ATTRIBNAME_PROD_NAME);

                ProductInfo waveDependentProductInfo = null;
                ProductInfo waveIndependentProductInfo = null;

                NodeList algNodelist = prodElement.getElementsByTagName(ELEMNAME_ALG);

                if (algNodelist != null && algNodelist.getLength() > 0) {
                    for (int j = 0; j < algNodelist.getLength(); j++) {

                        Element algElement = (Element) algNodelist.item(j);

                        AlgorithmInfo algorithmInfo = new AlgorithmInfo();


                        if (algElement.hasAttribute(ATTRIBNAME_ALG_NAME)) {
                            String algorithmName = algElement.getAttribute(ATTRIBNAME_ALG_NAME);
                            algorithmInfo.setName(algorithmName);
                        }

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

                        if (algorithmInfo.getParameterType() != AlgorithmInfo.ParameterType.NONE) {
                            if (waveDependentProductInfo == null) {
                                waveDependentProductInfo = new ProductInfo(prodName);
                            }

                            waveDependentProductInfo.addAlgorithmInfo(algorithmInfo);
                            waveDependentProductInfo.setName(prodName);
                            algorithmInfo.setProductInfo(waveDependentProductInfo);
                            algorithmInfo.setWavelengthDependent(true);

                        } else {
                            if (waveIndependentProductInfo == null) {
                                waveIndependentProductInfo = new ProductInfo(prodName);
                            }
                            waveIndependentProductInfo.addAlgorithmInfo(algorithmInfo);
                            waveIndependentProductInfo.setName(prodName);
                            algorithmInfo.setProductInfo(waveIndependentProductInfo);
                            algorithmInfo.setWavelengthDependent(false);
                        }

                    } // for algorithms

                    if (waveDependentProductInfo != null) {
                        l2genData.addWaveDependentProductInfoArray(waveDependentProductInfo);
                    }
                    if (waveIndependentProductInfo != null) {
                        l2genData.addWaveIndependentProductInfoArray(waveIndependentProductInfo);
                    }
                }
            } // for products
        }

        l2genData.sortWaveDependentProductInfoArray(ProductInfo.CASE_INSENSITIVE_ORDER);
        l2genData.sortWaveIndependentProductInfoArray(ProductInfo.CASE_INSENSITIVE_ORDER);
    }




    public String readFileIntoString (String filename){

        StringBuilder stringBuilder = new StringBuilder();

        ArrayList<String> fileContentsInArrayList = readFileIntoArrayList(filename);

        for (String line : fileContentsInArrayList) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }

        return  stringBuilder.toString();
    }


    public ArrayList<String> readFileIntoArrayList(String filename) {
        String lineData;
        ArrayList<String> fileContents = new ArrayList<String>();
        BufferedReader moFile = null;
        try {
            moFile = new BufferedReader(new FileReader(new File(filename)));
            while ((lineData = moFile.readLine()) != null) {

                fileContents.add(lineData);
            }
        } catch (IOException e) {
            ;
        } finally {
            try {
                moFile.close();
            } catch (Exception e) {
                //Ignore
            }
        }
        return fileContents;
    }

}
