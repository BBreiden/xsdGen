package net.chrbo.xml.xsdgen;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XsdGenerator {

  private final InputStream is;
  private String xsd;
  
  public XsdGenerator(InputStream is) {
    this.is = is;
  }

  public String getXsd() throws ParserConfigurationException, SAXException, IOException {
    if (xsd == null) {
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = db.parse(is);
      Element root = doc.getDocumentElement();
      ElementTree tree = buildTree(root);
      xsd = tree.getXsdElement();
    }
    return xsd;
  }

  private ElementTree buildTree(Node root) {
    ElementTree tree = new ElementTree(root.getNodeName());
    
    NamedNodeMap attr = root.getAttributes();
    for (int i=0; i<attr.getLength(); i++) {
      tree.addAttribute(new Attribute(attr.item(i).getNodeName()));
    }
    
    NodeList nl = root.getChildNodes();
    if (isTextAllowed(nl)) {
      tree.allowCData();
    }
    for (int i=0; i<nl.getLength(); i++) {
      tree.addChild(buildTree(nl.item(i)));
    }
    
    return tree;
  }

  private boolean isTextAllowed(NodeList nl) {
    boolean mixedAllowed = false;
    for (int i=0; i<nl.getLength(); i++) {
      mixedAllowed |= nl.item(i).getNodeValue()=="#text";
    }
    return mixedAllowed;
  }

}
