package net.chrbo.test.xml.xsdgen.xsdGen;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

class TestHelperFunctions {
  private TestHelperFunctions() {}
  
  /**
   * Checks if xml is a valid XML with respect to the given xsd.
   * 
   * @param xml to be ckeched
   * @param xsd valid xsd
   * @return true, if xml is valid
   */
  public static boolean isValidXml(String xml, String xsd) {
    Schema schema;
    boolean isValidXml = false;
    try {
      schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
          .newSchema(new StreamSource(new StringReader(xsd)));
      Validator v = schema.newValidator();
      v.validate(new StreamSource(new StringReader(xml)));
      isValidXml = true;
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } 
    return isValidXml;
  }

  /**
   * Checks if xsd is a valid XML schema definition by creating a validator using the parameter. If the xsd in invalid, the construction fails and 
   * throws an exception.
   * 
   * @param xsd String containing xsd to check
   * @return true if xsd is valid
   */
  public static boolean isValidXsd(String xsd) {
    Schema schema;
    boolean isValidXsd = false;
    try {
      schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
          .newSchema(new StreamSource(new StringReader(xsd)));
      @SuppressWarnings("unused")
      Validator v = schema.newValidator();
      isValidXsd = true;
    } catch (SAXException e) {
      e.printStackTrace();
    } 
    return isValidXsd;
  }
}