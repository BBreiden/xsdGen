package net.chrbo.test.xml.xsdgen.xsdGen;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

import net.chrbo.xml.xsdgen.XsdGenerator;

/**
 * Tests several xml test cases by checking the generated xsd against the expectation, verifying that the generated xsd is valid and checking that 
 * the input xml is valid with respect to the generated xsd.
 * 
 * @author borisbreidenbach
 *
 */
@RunWith(Parameterized.class)
public class XsdTests extends XsdConsistencyTests {
  
  private final String exp; 
  
  @Parameterized.Parameters(name = "{index}: input {0}")
  public static Collection<Object[]> data() {
    final String header = "<?xml version=\"1.0\"?>\n<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n";
    final String footer = "\n</xs:schema>";
    
    String[] input = { "<test></test>",
        "<test attr1=\"1\"></test>",
        "<a>Hallo</a>",
        "<a><b at1='1'></b><b at2='2'></b></a>",
        "<a><bb><bbb></bbb><ccc></ccc></bb>"
          + "<bb><aaa></aaa><bbb></bbb></bb>"
          + "<bb><aaa></aaa><ccc></ccc></bb>" 
          + "<bb><bbb></bbb><ddd></ddd></bb></a>"
    };
    String[] expected = { "<xs:element name=\"test\" type=\"xs:string\">\n</xs:element>",
        "<xs:element name=\"test\">\n<xs:complexType>\n<xs:attribute name=\"attr1\"/>\n</xs:complexType>\n</xs:element>",
        "<xs:element name=\"a\" type=\"xs:string\">\n</xs:element>",
        "<xs:element name=\"a\">\n<xs:complexType>\n<xs:sequence>\n<xs:element name=\"b\" maxOccurs=\"unbounded\">\n<xs:complexType>\n<xs:attribute name=\"at2\"/>\n<xs:attribute name=\"at1\"/>\n</xs:complexType>\n</xs:element>\n</xs:sequence>\n</xs:complexType>\n</xs:element>",
        "<xs:element name=\"a\">\n<xs:complexType>\n<xs:sequence>"
          + "\n<xs:element name=\"bb\" maxOccurs=\"unbounded\">\n<xs:complexType>\n<xs:sequence>"
          + "\n<xs:element name=\"aaa\" minOccurs=\"0\" type=\"xs:string\">\n</xs:element>"
          + "\n<xs:element name=\"bbb\" minOccurs=\"0\" type=\"xs:string\">\n</xs:element>"
          + "\n<xs:element name=\"ccc\" minOccurs=\"0\" type=\"xs:string\">\n</xs:element>"
          + "\n<xs:element name=\"ddd\" minOccurs=\"0\" type=\"xs:string\">\n</xs:element>"
          + "\n</xs:sequence>\n</xs:complexType>\n</xs:element>"
          + "\n</xs:sequence>\n</xs:complexType>\n</xs:element>"
    };
    Collection<Object[]> coll = new ArrayList<Object[]>();    
    for (int i=0; i<input.length; i++) {
      coll.add(new Object[] {input[i], header + expected[i] + footer});
    }
    return coll;
  }
  
  public XsdTests(String in, String exp) {
    super(in);
    this.exp = exp;
  }
  
  @Test
  public void runTest() throws ParserConfigurationException, SAXException, IOException {
    XsdGenerator gen = new XsdGenerator(new ByteArrayInputStream(this.in.getBytes()));
    String res = gen.getXsd();
    assertTrue("result: >>" + res + "<<\nexpected: >>" + this.exp + "<<",res.equals(this.exp));
    assertTrue("xsd invalid. Input: " + in, isValidXsd(res));
    assertTrue("input does not conform xsd. Input: " + in, isValidXml(in, res));
  }

}
