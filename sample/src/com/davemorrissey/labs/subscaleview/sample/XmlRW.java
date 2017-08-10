package com.davemorrissey.labs.subscaleview.sample;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;
import org.w3c.dom.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Elbit on 8/10/2017.
 */

public class XmlRW {

    private String sProjectName;
    private String sSeries;
    private String sXlsPath;
    private String mXml;
    private ArrayList<String> DataV;

    XmlRW(String xml, String projectName, String series,  String xlsPath){
        sProjectName = projectName;
        sSeries = series;
        sXlsPath = xlsPath;
        mXml = xml;
    }

    XmlRW(String xml){
        sProjectName = "";
        sSeries = "";
        sXlsPath = "";
        mXml = xml;
    }

    public  ArrayList<String> readXML() {
        DataV = new ArrayList<String>();
        Document dom = null;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            try {
                dom = db.parse(mXml);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Element doc = dom.getDocumentElement();

            sProjectName = getTextValue(sProjectName, doc, "sProjectName");
            if (sProjectName != null) {
                if (!sProjectName.isEmpty())
                    DataV.add(sProjectName);
            }
            sSeries = getTextValue(sSeries, doc, "sSeries");
            if (sSeries != null) {
                if (!sSeries.isEmpty())
                    DataV.add(sSeries);
            }
            sXlsPath = getTextValue(sXlsPath, doc, "sXlsPath");
            if (sXlsPath != null) {
                if (!sXlsPath.isEmpty())
                    DataV.add(sXlsPath);
            }
            return DataV;

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void saveToXML() {
        Document dom;
        Element e = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            dom = db.newDocument();

            // create the root element
            Element rootEle = dom.createElement("Data");

            // create data elements and place them under root
            e = dom.createElement("sProjectName");
            e.appendChild(dom.createTextNode(sProjectName));
            rootEle.appendChild(e);

            e = dom.createElement("sSeries");
            e.appendChild(dom.createTextNode(sSeries));
            rootEle.appendChild(e);

            e = dom.createElement("sXlsPath");
            e.appendChild(dom.createTextNode(sXlsPath));
            rootEle.appendChild(e);


            dom.appendChild(rootEle);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.INDENT, "yes");
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "data.dtd");
                tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(mXml)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } catch (ParserConfigurationException pce) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
        }
    }

    private String getTextValue(String def, Element doc, String tag) {
        String value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }
}
