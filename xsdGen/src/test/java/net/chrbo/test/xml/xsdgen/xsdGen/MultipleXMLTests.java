package net.chrbo.test.xml.xsdgen.xsdGen;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

import net.chrbo.xml.xsdgen.XsdGenerator;

@RunWith(Parameterized.class)
public class MultipleXMLTests {

  protected final String[] in;

  @Parameterized.Parameters(name = "{index}")
  public static Collection<Object[]> data() {

    Object[][] input = { 
        {"<a><bb at1=\"1\"><ccc>cText</ccc></bb><bb at2=\"2\"><ccc>cText</ccc><ddd>dText</ddd></bb></a>",
        "<a><bb at1=\"1\"><ccc>cText</ccc><ccc></ccc></bb></a>",
        "<a><bb></bb><bb><ddd at3=\"3\"></ddd></bb></a>"}
    };
    Collection<Object[]> coll = Arrays.asList(input);    
    return coll;
  }

  public MultipleXMLTests(String in1, String in2, String in3) {
    this.in = new String[] {in1, in2, in3};
  }

  @Test
  public void runTest() throws ParserConfigurationException, SAXException, IOException {
    XsdGenerator gen = new XsdGenerator();
    Arrays.asList(in).forEach(s -> {
      try {
        gen.parse(s);
      } catch (ParserConfigurationException | SAXException | IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
    String res = gen.getXsd();
    assertTrue("xsd invalid. Input: " + in, TestHelperFunctions.isValidXsd(res));
    Arrays.asList(in).forEach(s -> 
      assertTrue("input does not conform xsd. Input: " + in, TestHelperFunctions.isValidXml(s, res))
    );
  }

}
