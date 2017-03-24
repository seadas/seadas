package gov.nasa.gsfc.seadas.processing.common;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

public class XmlReader {

    public final String PROCESSOR_OPTION_XML_NODE_NAME = "option";
    Document dom;

    public XmlReader() {
    }

    public Document parseXmlFile(InputStream inputStream) {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            dom = db.parse(inputStream);


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return dom;
    }

    public Element parseAndGetRootElement(InputStream inputStream) {
        parseXmlFile(inputStream);
        return dom.getDocumentElement();
    }


    /**
     * I take a xml element and the tag name, look for the tag and get
     * the text content
     * i.e for <employee><name>John</name></employee> xml snippet if
     * the Element points to employee node and tagName is name I will return John
     *
     * @param ele
     * @param tagName
     * @return
     */
    public static String getTextValue(Element ele, String tagName) {
        String textVal = null;
        NodeList nl = ele.getElementsByTagName(tagName);
        if (nl != null && nl.getLength() > 0) {
            Element el = (Element) nl.item(0);

            if (el.hasChildNodes()) {
                textVal = el.getFirstChild().getNodeValue();
            }

        }

        return textVal;
    }

    public static String getAttributeTextValue(Element ele, String attributeName) {
        String textVal = null;

        textVal = ele.getAttribute(attributeName);
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     *
     * @param ele
     * @param tagName
     * @return
     */
    public static int getIntValue(Element ele, String tagName) {
        //in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }

        /**
     * Calls getTextValue and returns a boolean value
     *
     * @param ele
     * @param tagName
     * @return
     */
    public static boolean getBooleanValue(Element ele, String tagName) {
        return Boolean.parseBoolean(getTextValue(ele, tagName))  ;
    }

}
