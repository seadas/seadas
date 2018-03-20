package gov.nasa.gsfc.seadas.processing.core;

import gov.nasa.gsfc.seadas.processing.common.XmlReader;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genAlgorithmInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genProductCategoryInfo;
import gov.nasa.gsfc.seadas.processing.l2gen.productData.L2genProductInfo;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.ArrayList;

import static gov.nasa.gsfc.seadas.processing.core.ParamInfo.NULL_STRING;

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

                L2genProductCategoryInfo productCategoryInfo = new L2genProductCategoryInfo(name);

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
                String autoTab = XmlReader.getTextValue(categoryElement, "autoTab");
                String defaultBucketString = XmlReader.getTextValue(categoryElement, "defaultBucket");
                String ignore = XmlReader.getTextValue(categoryElement, "ignore");


                L2genParamCategoryInfo paramCategoryInfo = new L2genParamCategoryInfo(name);

                if (autoTab != null && autoTab.equals("1")) {
                    paramCategoryInfo.setAutoTab(true);
                } else {
                    paramCategoryInfo.setAutoTab(false);
                }

                if (ignore != null && ignore.equals("1")) {
                    paramCategoryInfo.setIgnore(true);
                } else {
                    paramCategoryInfo.setIgnore(false);
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

                if (name != null) {
                    name = name.toLowerCase();
                    String value = XmlReader.getTextValue(optionElement, "value");

                    if (value == null || value.length() == 0) {
                        value = XmlReader.getTextValue(optionElement, "default");
                    }

                    if (!name.equals(l2genData.IFILE) &&
                            !name.equals(l2genData.OFILE) &&
                            !name.equals(l2genData.GEOFILE) &&
                            !name.equals(l2genData.SUITE) &&
                            !name.equals(l2genData.PAR)) {
                        l2genData.setParamValueAndDefault(name, value);
                    }
                }
            }
        }
    }


    public void readParamInfoXml(InputStream stream) throws IOException {
        XmlReader reader = new XmlReader();
        Element rootElement = reader.parseAndGetRootElement(stream);

        l2genData.clearParamInfos();

        NodeList optionNodelist = rootElement.getElementsByTagName("option");

        if (optionNodelist != null && optionNodelist.getLength() > 0) {
            for (int i = 0; i < optionNodelist.getLength(); i++) {

                Element optionElement = (Element) optionNodelist.item(i);

                String name = XmlReader.getTextValue(optionElement, "name");
                if (name != null && name.length() > 0) {
                    name = name.toLowerCase();
                    String tmpType = optionElement.getAttribute("type");
                    //   String tmpType = XmlReader.getTextValue(optionElement, "type");

                    ParamInfo.Type type = null;

                    if (tmpType != null) {
                        if (tmpType.toLowerCase().equals("boolean")) {
                            type = ParamInfo.Type.BOOLEAN;
                        } else if (tmpType.toLowerCase().equals("int")) {
                            type = ParamInfo.Type.INT;
                        } else if (tmpType.toLowerCase().equals("float")) {
                            type = ParamInfo.Type.FLOAT;
                        } else if (tmpType.toLowerCase().equals("string")) {
                            type = ParamInfo.Type.STRING;
                        } else if (tmpType.toLowerCase().equals("ifile")) {
                            type = ParamInfo.Type.IFILE;
                        } else if (tmpType.toLowerCase().equals("ofile")) {
                            type = ParamInfo.Type.OFILE;
                        }
                    }

                    String value = XmlReader.getTextValue(optionElement, "value");

                    String description = XmlReader.getTextValue(optionElement, "description");
                    String source = XmlReader.getTextValue(optionElement, "source");


                    ParamInfo paramInfo;
                    if (name.equals(l2genData.L2PROD)) {
                        paramInfo = l2genData.createL2prodParamInfo(value);
                    } else {
                        paramInfo = new ParamInfo(name, value, type);
                    }

                    if (name.equals(L2genData.IFILE) ||
                            name.equals(L2genData.OFILE) ||
                            name.equals(L2genData.GEOFILE) ||
                            name.equals(L2genData.PAR)) {
                        paramInfo.setValue(NULL_STRING);
                        paramInfo.setDefaultValue(NULL_STRING);
                    } else if (name.equals(L2genData.SUITE)) {
                        paramInfo.setValue(l2genData.getDefaultSuite());
                        paramInfo.setDefaultValue(l2genData.getDefaultSuite());
                    } else {
                        paramInfo.setDefaultValue(paramInfo.getValue());
                    }

                    paramInfo.setDescription(description);
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
                            paramInfo.addValidValueInfo(paramValidValueInfo);
                        }
                    }

                    l2genData.addParamInfo(paramInfo);
                }
            }
        }

        // add on suite if it was missing from xml
        if (!l2genData.hasParamValue(L2genData.SUITE)) {
            ParamInfo suiteParamInfo = new ParamInfo(L2genData.SUITE, l2genData.getDefaultSuite(),
                    ParamInfo.Type.STRING, l2genData.getDefaultSuite());
            l2genData.addParamInfo(suiteParamInfo);
        }

        l2genData.sortParamInfos();
    }


    private String valueOverRides(String name, String value, File iFile, File geoFile, File oFile) {

        name = name.toLowerCase();
        if (name.equals(L2genData.IFILE)) {
            if (iFile != null) {
                value = iFile.toString();
            } else {
                value = NULL_STRING;
            }
        }

        if (name.equals(L2genData.GEOFILE)) {
            if (geoFile != null) {
                value = geoFile.toString();
            } else {
                value = NULL_STRING;
            }
        }

        if (name.equals(L2genData.OFILE)) {
            if (oFile != null) {
                value = oFile.toString();
            } else {
                value = NULL_STRING;
            }
        }

        if (name.equals(L2genData.PAR)) {
            value = NULL_STRING;
        }

        return value;
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
                L2genProductInfo productInfo = null;

                L2genProductInfo integerProductInfo = null;

                NodeList algNodelist = prodElement.getElementsByTagName("algorithm");

                if (algNodelist != null && algNodelist.getLength() > 0) {
                    for (int j = 0; j < algNodelist.getLength(); j++) {

                        Element algElement = (Element) algNodelist.item(j);

                        L2genAlgorithmInfo algorithmInfo = new L2genAlgorithmInfo(l2genData.waveLimiterInfos);

                        if (algElement.hasAttribute("name")) {
                            String algorithmName = algElement.getAttribute("name");
                            algorithmInfo.setName(algorithmName);
                        }


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

                        String parameterTypeStr = XmlReader.getTextValue(algElement, "parameterType");


                        algorithmInfo.setParameterType(parameterTypeStr);


                        if (algorithmInfo.getParameterType() == L2genAlgorithmInfo.ParameterType.INT) {
                            if (integerProductInfo == null) {
                                integerProductInfo = new L2genProductInfo(prodName);
                            }
                            integerProductInfo.setName(prodName);
                            integerProductInfo.addChild(algorithmInfo);
                            algorithmInfo.setProductInfo(integerProductInfo);
                        } else {
                            if (productInfo == null) {
                                productInfo = new L2genProductInfo(prodName);
                            }
                            productInfo.setName(prodName);
                            productInfo.addChild(algorithmInfo);
                            algorithmInfo.setProductInfo(productInfo);
                        }

                    } // for algorithms


                    if (productInfo != null) {
                        productInfo.sortChildren();
                        l2genData.addProductInfo(productInfo);
                    }
                    if (integerProductInfo != null) {
                        l2genData.addIntegerProductInfo(integerProductInfo);
                    }
                }
            } // for products
        }

        l2genData.sortProductInfos(L2genProductInfo.CASE_INSENSITIVE_ORDER);
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
        File file = null;
        if (filename != null) {
            file = new File(filename);
        }
        return readFileIntoArrayList(file);
    }

    public ArrayList<String> readFileIntoArrayList(File file) {
        return l2genData.getOcssw().readSensorFileIntoArrayList(file);
    }
}
