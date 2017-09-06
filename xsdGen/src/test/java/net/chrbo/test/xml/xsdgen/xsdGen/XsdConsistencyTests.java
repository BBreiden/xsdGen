package net.chrbo.test.xml.xsdgen.xsdGen;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

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
        {"<a><bb at1=\"1\"></bb></a>"},
        {"<a><bb at1=\"1\">text</bb></a>"}
    };
    Collection<Object[]> coll = Arrays.asList(input);    
    return coll;
  }
  
  public XsdConsistencyTests(String in) {
    this.in = in;
  }
  
  @Test
  public void runTest() throws ParserConfigurationException, SAXException, IOException {
    XsdGenerator gen = (new XsdGenerator()).parse(new ByteArrayInputStream(this.in.getBytes()));
    String res = gen.getXsd();
    assertTrue("xsd invalid. Input: " + in, TestHelperFunctions.isValidXsd(res));
    assertTrue("input does not conform xsd. Input: " + in, TestHelperFunctions.isValidXml(in, res));
  }

  

}
