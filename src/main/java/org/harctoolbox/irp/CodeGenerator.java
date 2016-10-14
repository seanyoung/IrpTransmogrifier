/*
Copyright (C) 2016 Bengt Martensson.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or (at
your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.
*/

package org.harctoolbox.irp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.harctoolbox.ircore.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 */
public class CodeGenerator {

    private static final Logger logger = Logger.getLogger(CodeGenerator.class.getName());

    /**
     * Name space for XLST (1.0 and 2.0)
     */
    public final static String xsltNamespace = "http://www.w3.org/1999/XSL/Transform";
    public final static String cdataElements = "Irp Documentation";
    private final static String ending = ".xml";
    private final static String defaultCharSet = "UTF-8";
    private Document source;
    private final String protocolName;
    private final boolean dumpIntermediates;

    public CodeGenerator(Document document, boolean dumpIntermediates) throws FileNotFoundException, TransformerException {
        this.dumpIntermediates = dumpIntermediates;
        this.source = document;
        protocolName = document.getDocumentElement().getAttribute("name");
        if (dumpIntermediates)
            XmlUtils.printDOM(new File(protocolName + ending), source);
    }

    public void transform(Document stylesheet, String suffix) throws TransformerException, FileNotFoundException {
        if (stylesheet == null)
            throw new NullPointerException("stylesheet must not be null");

        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        Transformer tr = factory.newTransformer(new DOMSource(stylesheet));
        Document newDoc = XmlUtils.newDocument();
        tr.transform(new DOMSource(source), new DOMResult(newDoc));
        if (dumpIntermediates)
            XmlUtils.printDOM(new FileOutputStream(protocolName + suffix + ending), newDoc, defaultCharSet, cdataElements);
        source = newDoc;
    }

    public void transform(String filename, String suffix) throws TransformerException, IOException, SAXException {
        transform(XmlUtils.openXmlFile(new File(filename)), suffix);
    }

    public void printDOM(OutputStream ostr, String charsetName) throws TransformerException, IOException {
        printDOM(ostr, (Document) null, charsetName);
    }

    public void printDOM(OutputStream ostr, File stylesheet, String charsetName) throws TransformerException, IOException, SAXException {
        printDOM(ostr, XmlUtils.openXmlFile(stylesheet), charsetName);
    }

    public void printDOM(OutputStream ostr, Document stylesheet, /*HashMap<String, String>parameters,*/
            String charsetName) throws IOException, TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        Transformer tr;
        if (stylesheet == null) {
            tr = factory.newTransformer();
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, charsetName);

        } else {
            NodeList nodeList = stylesheet.getDocumentElement().getElementsByTagNameNS(xsltNamespace, "output");
            if (nodeList.getLength() > 0) {
                Element e = (Element) nodeList.item(0);
                e.setAttribute("encoding", charsetName);
            }
            tr = factory.newTransformer(new DOMSource(stylesheet));
        }
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        tr.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS, cdataElements);
        tr.transform(new DOMSource(source), new StreamResult(ostr));
    }
}
