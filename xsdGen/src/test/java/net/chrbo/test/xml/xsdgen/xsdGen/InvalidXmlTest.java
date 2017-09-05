package net.chrbo.test.xml.xsdgen.xsdGen;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import net.chrbo.xml.xsdgen.XsdGenerator;

@RunWith(Parameterized.class)
public class InvalidXmlTest {
  
  final String xml;
  
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { {"HalloWelt"}
      , {"<test>"}
      , {"</test>"}
      , {"<test></tes>"}
    });
  }
  
  public InvalidXmlTest(String s) {
    this.xml = s;
  }
  
  public String getXsd(String s) throws ParserConfigurationException, SAXException, IOException {
    XsdGenerator gen = new XsdGenerator(new ByteArrayInputStream(s.getBytes()));
    return gen.getXsd();
  }

  @Test(expected = SAXException.class)
  public void InvalidXMLTest() throws ParserConfigurationException, SAXException, IOException {
    getXsd(this.xml);
  }
  
}
