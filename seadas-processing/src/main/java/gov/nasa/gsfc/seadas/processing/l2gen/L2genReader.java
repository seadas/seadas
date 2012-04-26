package gov.nasa.gsfc.seadas.processing.l2gen;

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


    public void readProductCategoryXml(InputStream stream) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        l2genData.clearProductCategoryInfos();

        NodeList categoryNodelist = rootElement.getElementsByTagName("category");

        if (categoryNodelist != null && categoryNodelist.getLength() > 0) {
            for (int i = 0; i < categoryNodelist.getLength(); i++) {

                Element categoryElement = (Element) categoryNodelist.item(i);

                String name = categoryElement.getAttribute("name");
                String visible = XmlReader.getTextValue(categoryElement, "makeVisible");
                String defaultBucketString = XmlReader.getTextValue(categoryElement, "defaultBucket");

                ProductCategoryInfo productCategoryInfo = new ProductCategoryInfo(name);

                if (visible != null && visible.equals("1")) {
                    productCategoryInfo.setVisible(true);
                } else {
                    productCategoryInfo.setVisible(false);
                }

                boolean defaultBucket;
                if (defaultBucketString != null && defaultBucketString.equals("1")) {
                    defaultBucket = true;
                } else {
                    defaultBucket = false;
                }

                productCategoryInfo.setDefaultBucket(defaultBucket);

                NodeList productNodelist = categoryElement.getElementsByTagName("product");

                if (productNodelist != null && productNodelist.getLength() > 0) {
                    for (int j = 0; j < productNodelist.getLength(); j++) {

                        Element productElement = (Element) productNodelist.item(j);

                        String product = productElement.getTextContent();

                        productCategoryInfo.addProductName(product);
                    }

                    //  productCategoryInfo.sortProductNames();
                }

                l2genData.addProductCategoryInfo(productCategoryInfo);
            }
        }

        //  l2genData.sortProductCategoryInfos();
    }


    public void readParamCategoryXml(InputStream stream) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        l2genData.clearParamCategoryInfos();

        NodeList categoryNodelist = rootElement.getElementsByTagName("category");

        if (categoryNodelist != null && categoryNodelist.getLength() > 0) {
            for (int i = 0; i < categoryNodelist.getLength(); i++) {

                Element categoryElement = (Element) categoryNodelist.item(i);

                String name = categoryElement.getAttribute("name");
                String visible = XmlReader.getTextValue(categoryElement, "makeVisible");
                String defaultBucketString = XmlReader.getTextValue(categoryElement, "defaultBucket");

                ParamCategoryInfo paramCategoryInfo = new ParamCategoryInfo(name);

                if (visible != null && visible.equals("1")) {
                    paramCategoryInfo.setVisible(true);
                } else {
                    paramCategoryInfo.setVisible(false);
                }

                if (defaultBucketString != null && defaultBucketString.equals("1")) {
                    paramCategoryInfo.setDefaultBucket(true);
                } else {
                    paramCategoryInfo.setDefaultBucket(false);
                }


                NodeList paramNodelist = categoryElement.getElementsByTagName("param");

                if (paramNodelist != null && paramNodelist.getLength() > 0) {
                    for (int j = 0; j < paramNodelist.getLength(); j++) {

                        Element paramElement = (Element) paramNodelist.item(j);

                        String param = paramElement.getTextContent();

                        paramCategoryInfo.addParamName(param);
                    }

                    //  paramCategoryInfo.sortParamNameInfos();
                }

                l2genData.addParamCategoryInfo(paramCategoryInfo);
            }
        }

        //  l2genData.sortParamCategoryInfos();
    }


    public void updateParamInfosWithXml(InputStream stream) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        NodeList optionNodelist = rootElement.getElementsByTagName("option");

        if (optionNodelist != null && optionNodelist.getLength() > 0) {
            for (int i = 0; i < optionNodelist.getLength(); i++) {

                Element optionElement = (Element) optionNodelist.item(i);

                String name = XmlReader.getTextValue(optionElement, "name");
                name = name.toLowerCase();
                String value = XmlReader.getTextValue(optionElement, "value");

                String nullValueOverrides[] = {l2genData.OFILE, l2genData.PAR, l2genData.GEOFILE};
                if (name != null) {
                    for (String nullValueOverride : nullValueOverrides) {
                        if (name.equals(nullValueOverride)) {
                            value = ParamInfo.NULL_STRING;
                        }
                    }

                    if (!name.equals(l2genData.IFILE)) {
                        l2genData.setParamValue(name, value);

                        if (name.equals(l2genData.L2PROD)) {
                            l2genData.setProductDefaults();
                        } else {
                            l2genData.setParamDefault(name);
                        }
                    }
                }
            }
        }
    }


    public void readParamInfoXml(InputStream stream) {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        l2genData.clearParamInfos();

        NodeList optionNodelist = rootElement.getElementsByTagName("option");

        if (optionNodelist != null && optionNodelist.getLength() > 0) {
            for (int i = 0; i < optionNodelist.getLength(); i++) {

                Element optionElement = (Element) optionNodelist.item(i);

                String name = XmlReader.getTextValue(optionElement, "name");
                name = name.toLowerCase();
                String tmpType = XmlReader.getTextValue(optionElement, "type");

                ParamInfo.Type type = null;

                if (tmpType != null) {
                    if (tmpType.toLowerCase().equals("bool")) {
                        type = ParamInfo.Type.BOOLEAN;
                    } else if (tmpType.toLowerCase().equals("int")) {
                        type = ParamInfo.Type.INT;
                    } else if (tmpType.toLowerCase().equals("float")) {
                        type = ParamInfo.Type.FLOAT;
                    } else if (tmpType.toLowerCase().equals("string")) {
                        type = ParamInfo.Type.STRING;
                    }
                }

                String value = XmlReader.getTextValue(optionElement, "value");

                String nullValueOverrides[] = {l2genData.IFILE, l2genData.OFILE, l2genData.PAR, l2genData.GEOFILE};
                if (name != null) {
                    for (String nullValueOverride : nullValueOverrides) {
                        if (name.equals(nullValueOverride)) {
                            value = ParamInfo.NULL_STRING;
                        }
                    }
                }


                String defaultValue = value;
                String description = XmlReader.getTextValue(optionElement, "description");
                String source = XmlReader.getTextValue(optionElement, "source");

                if (name.toLowerCase().equals(l2genData.L2PROD)) {
                    l2genData.setParamValue(l2genData.L2PROD, value);
                    l2genData.setProductDefaults();
                } else {

                    ParamInfo paramInfo = new ParamInfo(name, value, type);

                    paramInfo.setDescription(description);
                    paramInfo.setDefaultValue(defaultValue);
                    paramInfo.setSource(source);

                    boolean isBit = false;
                    if (name != null) {
                        if (name.equals("gas_opt") ||
                                name.equals("eval")) {
                            isBit = true;
                        }
                    }

                    paramInfo.setBit(isBit);

                    NodeList validValueNodelist = optionElement.getElementsByTagName("validValue");

                    if (validValueNodelist != null && validValueNodelist.getLength() > 0) {
                        for (int j = 0; j < validValueNodelist.getLength(); j++) {

                            Element validValueElement = (Element) validValueNodelist.item(j);

                            String validValueValue = XmlReader.getTextValue(validValueElement, "value");
                            String validValueDescription = XmlReader.getTextValue(validValueElement, "description");

                            ParamValidValueInfo paramValidValueInfo = new ParamValidValueInfo(validValueValue);

                            paramValidValueInfo.setDescription(validValueDescription);
                            paramValidValueInfo.setParent(paramInfo);
                            paramInfo.addValidValueInfo(paramValidValueInfo);
                        }

                        //   paramInfo.sortValidValueInfos();
                    }

                    l2genData.addParamInfo(paramInfo);
                }
            }
        }

        l2genData.sortParamInfos();
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
