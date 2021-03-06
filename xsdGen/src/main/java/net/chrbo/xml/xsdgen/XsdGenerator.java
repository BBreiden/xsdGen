package net.chrbo.xml.xsdgen;

import java.io.ByteArrayInputStream;
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

/**
 * The XsdGenerator class builds the XSD from an XML file. The XML is provided as an InputStream and the XSD is obtained as a String.
 * 
 * @author borisbreidenbach
 *
 */
public class XsdGenerator {

  private final String header = "<?xml version=\"1.0\"?>\n<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n";
  private final String footer = "\n</xs:schema>";
  private ElementTree tree;
  
  /** 
   * Constructor
   * @param is InputStream providing the XML
   * @throws ParserConfigurationException 
   * @throws IOException 
   * @throws SAXException 
   */
  public XsdGenerator parse(InputStream is) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document doc = db.parse(is);
    Element root = doc.getDocumentElement();
    ElementTree newTree = buildTree(root);
    if (tree==null) {
      tree = newTree;
    } else {
      tree = TreeMerger.mergeTrees(tree, newTree);
    }
    return this;
  }

  /** 
   * Constructor
   * @param s String providing the XML
   * @return 
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  public XsdGenerator parse(String s) throws ParserConfigurationException, SAXException, IOException {
    parse(new ByteArrayInputStream(s.getBytes()));
    return this;
  }
  
  /** 
   * Returns the XSD generated from the XML
   * @return xsd as String
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public String getXsd() throws ParserConfigurationException, SAXException, IOException {
    String t = "";
    if (tree!=null) {
      t = tree.getXsdElement();
    }
    return header + t + footer;
  }

  private ElementTree buildTree(Node root) {
    ElementTree tree = new ElementTree(root.getNodeName());

    if (root.hasAttributes()) {
      NamedNodeMap attr = root.getAttributes();
      for (int i=0; i<attr.getLength(); i++) {
        tree.addAttribute(new Attribute(attr.item(i).getNodeName()));
      }
    }

    NodeList nl = root.getChildNodes();
    if (isTextAllowed(nl)) {
      tree.allowCData();
    }
    for (int i=0; i<nl.getLength(); i++) {
      if (!isTextNode(nl.item(i))) {
        tree.addChild(buildTree(nl.item(i)));
      }
    }

    return tree;
  }

  private boolean isTextAllowed(NodeList nl) {
    boolean mixedAllowed = false;
    for (int i=0; i<nl.getLength(); i++) {
      mixedAllowed |= isTextNode(nl.item(i));
    }
    return mixedAllowed;
  }

  private boolean isTextNode(Node n) {
    return n.getNodeName().equals("#text");
  }
}
