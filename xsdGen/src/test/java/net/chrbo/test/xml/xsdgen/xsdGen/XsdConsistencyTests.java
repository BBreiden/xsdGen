package net.chrbo.test.xml.xsdgen.xsdGen;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

import net.chrbo.xml.xsdgen.XsdGenerator;

/**
 * Tests several xml test cases by verifying that the generated xsd is valid and checking that 
 * the input xml is valid with respect to the generated xsd.
 * 
 * @author borisbreidenbach
 *
 */
@RunWith(Parameterized.class)
public class XsdConsistencyTests {
  
  protected final String in; 
  
  @Parameterized.Parameters(name = "{index}: input {0}")
  public static Collection<Object[]> data() {
    
    Object[][] input = { 
        {"<a><bb at1=\"1\"><ccc>c</ccc></bb><bb at2=\"2\"><ddd></ddd></bb></a>"},
        {"<a><bb><ccc>c</ccc></bb><bb>a<ddd>d</ddd></bb></a>"}
    };
    Collection<Object[]> coll = Arrays.asList(input);    
    return coll;
  }
  
  public XsdConsistencyTests(String in) {
    this.in = in;
  }
  
  @Test
  public void runTest() throws ParserConfigurationException, SAXException, IOException {
    XsdGenerator gen = new XsdGenerator(new ByteArrayInputStream(this.in.getBytes()));
    String res = gen.getXsd();
    assertTrue("xsd invalid. Input: " + in, isValidXsd(res));
    assertTrue("input does not conform xsd. Input: " + in, isValidXml(in, res));
  }

  /**
   * Checks if xml is a valid XML with respect to the given xsd.
   * 
   * @param xml to be ckeched
   * @param xsd valid xsd
   * @return true, if xml is valid
   */
  protected boolean isValidXml(String xml, String xsd) {
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
  protected boolean isValidXsd(String xsd) {
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
