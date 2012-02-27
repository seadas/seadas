package gov.nasa.obpg.seadas.sandbox.l2gen;

import com.kenai.jffi.Type;
import org.python.antlr.ast.Str;
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


    private L2genData l2genData;

    public L2genReader(L2genData l2genData) {
        this.l2genData = l2genData;
    }


    public void readParamCategoriesXml(InputStream stream) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        l2genData.clearParamCategoriesInfos();

        NodeList categoryNodelist = rootElement.getElementsByTagName("category");

        if (categoryNodelist != null && categoryNodelist.getLength() > 0) {
            for (int i = 0; i < categoryNodelist.getLength(); i++) {

                Element categoryElement = (Element) categoryNodelist.item(i);

                String name = categoryElement.getAttribute("name");
                String visible = XmlReader.getTextValue(categoryElement, "makeVisible");
                String defaultBucketString = XmlReader.getTextValue(categoryElement, "defaultBucket");

                ParamCategoriesInfo paramCategoriesInfo = new ParamCategoriesInfo(name);

                if (visible != null && visible.equals("1")) {
                    paramCategoriesInfo.setVisible(true);
                } else {
                    paramCategoriesInfo.setVisible(false);
                }

                if (defaultBucketString != null && defaultBucketString.equals("1")) {
                    paramCategoriesInfo.setDefaultBucket(true);
                } else {
                    paramCategoriesInfo.setDefaultBucket(false);
                }


                NodeList paramNodelist = categoryElement.getElementsByTagName("param");

                if (paramNodelist != null && paramNodelist.getLength() > 0) {
                    for (int j = 0; j < paramNodelist.getLength(); j++) {

                        Element paramElement = (Element) paramNodelist.item(j);

                        String param = paramElement.getTextContent();

                        paramCategoriesInfo.addParamName(param);
                    }

                    paramCategoriesInfo.sortParamNameInfos();
                }

                l2genData.addParamCategoriesInfo(paramCategoriesInfo);
            }
        }

        l2genData.sortParamCategoriesInfos();
    }


    public void readParamOptionsXml(InputStream stream) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        l2genData.clearParamOptionsInfos();

        NodeList optionNodelist = rootElement.getElementsByTagName("option");

        if (optionNodelist != null && optionNodelist.getLength() > 0) {
            for (int i = 0; i < optionNodelist.getLength(); i++) {

                Element optionElement = (Element) optionNodelist.item(i);

                String name = XmlReader.getTextValue(optionElement, "name");
                String value = XmlReader.getTextValue(optionElement, "value");
                String tmpType = XmlReader.getTextValue(optionElement, "type");
                String defaultValue = XmlReader.getTextValue(optionElement, "defaultValue");
                String description = XmlReader.getTextValue(optionElement, "description");
                String source = XmlReader.getTextValue(optionElement, "source");

                ParamOptionsInfo.Type type = null;

                if (tmpType != null) {
                    if (tmpType.toLowerCase().equals("bool")) {
                        type = ParamOptionsInfo.Type.BOOLEAN;
                    } else if (tmpType.toLowerCase().equals("int")) {
                        type = ParamOptionsInfo.Type.INT;
                    } else if (tmpType.toLowerCase().equals("float")) {
                        type = ParamOptionsInfo.Type.FLOAT;
                    } else if (tmpType.toLowerCase().equals("string")) {
                        type = ParamOptionsInfo.Type.STRING;
                    }
                }

                ParamOptionsInfo paramOptionsInfo = new ParamOptionsInfo(name, value, type);

                paramOptionsInfo.setDescription(description);
                paramOptionsInfo.setDefaultValue(defaultValue);
                paramOptionsInfo.setSource(source);

                NodeList validValueNodelist = optionElement.getElementsByTagName("validValue");

                if (validValueNodelist != null && validValueNodelist.getLength() > 0) {
                    for (int j = 0; j < validValueNodelist.getLength(); j++) {

                        Element validValueElement = (Element) validValueNodelist.item(j);

                        String validValueValue = XmlReader.getTextValue(validValueElement, "value");
                        String validValueDescription = XmlReader.getTextValue(validValueElement, "description");

                        ParamValidValueInfo paramValidValueInfo = new ParamValidValueInfo(validValueValue);

                        paramValidValueInfo.setDescription(validValueDescription);
                        paramValidValueInfo.setParent(paramOptionsInfo);
                        paramOptionsInfo.addValidValueInfo(paramValidValueInfo);
                    }

                    paramOptionsInfo.sortValidValueInfos();
                }

                l2genData.addParamOptionsInfo(paramOptionsInfo);
            }
        }

        l2genData.sortParamOptionsInfos(ParamOptionsInfo.SORT_BY_NAME);
    }


    public void readProductsXml(InputStream stream) {

        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        l2genData.clearProductInfos();

        NodeList prodNodelist = rootElement.getElementsByTagName("product");

        if (prodNodelist != null && prodNodelist.getLength() > 0) {
            for (int i = 0; i < prodNodelist.getLength(); i++) {

                Element prodElement = (Element) prodNodelist.item(i);

                String prodName = prodElement.getAttribute("name");
                ProductInfo productInfo = new ProductInfo(prodName);
                productInfo.setName(prodName);

                NodeList algNodelist = prodElement.getElementsByTagName("algorithm");

                if (algNodelist != null && algNodelist.getLength() > 0) {
                    for (int j = 0; j < algNodelist.getLength(); j++) {

                        Element algElement = (Element) algNodelist.item(j);

                        AlgorithmInfo algorithmInfo = new AlgorithmInfo();

                        if (algElement.hasAttribute("name")) {
                            String algorithmName = algElement.getAttribute("name");
                            algorithmInfo.setName(algorithmName);
                        }

                        String parameterTypeStr = XmlReader.getTextValue(algElement, "parameterType");
                        algorithmInfo.setParameterType(parameterTypeStr);

                        String suffix = XmlReader.getTextValue(algElement, "suffix");
                        algorithmInfo.setSuffix(suffix);


                        String description = XmlReader.getTextValue(algElement, "description");
                        algorithmInfo.setDescription(description);

                        String prefix = XmlReader.getTextValue(algElement, "prefix");
                        algorithmInfo.setPrefix(prefix);

                        String units = XmlReader.getTextValue(algElement, "units");
                        algorithmInfo.setUnits(units);

                        String dataType = XmlReader.getTextValue(algElement, "dataType");
                        algorithmInfo.setDataType(dataType);

                        productInfo.addChild(algorithmInfo);
                        algorithmInfo.setProductInfo(productInfo);

                    } // for algorithms


                    productInfo.sortChildren();
                    l2genData.addProductInfo(productInfo);
                }
            } // for products
        }

        l2genData.sortProductInfos(ProductInfo.CASE_INSENSITIVE_ORDER);
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
