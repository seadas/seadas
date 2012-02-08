package gov.nasa.obpg.seadas.sandbox.l2gen;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
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


    public void readProductsXmlFile(InputStream stream) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        l2genData.clearProductInfoArray();

        NodeList prodNodelist = rootElement.getElementsByTagName(ELEMNAME_PROD);

        if (prodNodelist != null && prodNodelist.getLength() > 0) {
            for (int i = 0; i < prodNodelist.getLength(); i++) {

                Element prodElement = (Element) prodNodelist.item(i);

                String prodName = prodElement.getAttribute(ATTRIBNAME_PROD_NAME);
                ProductInfo productInfo = new ProductInfo(prodName);
                productInfo.setName(prodName);

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

                        productInfo.addChild(algorithmInfo);
                        algorithmInfo.setProductInfo(productInfo);

                    } // for algorithms

                    l2genData.addProductInfoArray(productInfo);
                }
            } // for products
        }

        l2genData.sortProductInfoArray(ProductInfo.CASE_INSENSITIVE_ORDER);
    }


    public String readFileIntoString(String filename) {

        StringBuilder stringBuilder = new StringBuilder();

        ArrayList<String> fileContentsInArrayList = readFileIntoArrayList(filename);

        for (String line : fileContentsInArrayList) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
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
