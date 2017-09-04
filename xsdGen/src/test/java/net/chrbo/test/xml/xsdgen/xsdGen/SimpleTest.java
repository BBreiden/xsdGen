package net.chrbo.test.xml.xsdgen.xsdGen;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import net.chrbo.xml.xsdgen.XsdGenerator;

public class SimpleTest {
  
  public String getXsd(String s) throws ParserConfigurationException, SAXException, IOException {
    XsdGenerator gen = new XsdGenerator(new ByteArrayInputStream(s.getBytes()));
    return gen.getXsd();
  }

  @Test(expected = SAXException.class)
  public void InvalidXMLTest() throws ParserConfigurationException, SAXException, IOException {
    getXsd("HalloWelt");
  }
 
  @Test(expected = SAXException.class)
  public void InvalidXMLTest2() throws ParserConfigurationException, SAXException, IOException {
    getXsd("<test>");
  }
  
  @Test(expected = SAXException.class)
  public void InvalidXMLTest3() throws ParserConfigurationException, SAXException, IOException {
    getXsd("</test>");
  }
  
  @Test(expected = SAXException.class)
  public void InvalidXMLTest4() throws ParserConfigurationException, SAXException, IOException {
    getXsd("<test></tes>");
  }
  
  public void runTest(String in, String expected) throws ParserConfigurationException, SAXException, IOException {
    String res = getXsd(in);
    assertTrue("result: >>" + res + "<<\nexpected: >>" + expected + "<<",res.equals(expected));
  }

  @Test
  public void run() throws ParserConfigurationException, SAXException, IOException {
    String[] input = { "<test></test>",
        "<test attr1=\"1\"></test>"};
    String[] expected = { "\n<xs:element name=\"test\">\n<xs:simpleType>\n</xs:simpleType>\n</xs:element>",
        "\n<xs:element name=\"test\">\n<xs:complexType>\n<xs:attribute name=\"attr1\"/>\n</xs:complexType>\n</xs:element>"
    };
    
    for (int i=0; i<input.length; i++) {
      runTest(input[i], expected[i]);
    }
  }
}
