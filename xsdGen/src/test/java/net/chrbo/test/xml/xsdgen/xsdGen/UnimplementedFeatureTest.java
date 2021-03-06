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
public class UnimplementedFeatureTest {
  
  final String xml;
  
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      {"<a><bb><ccc>c</ccc></bb><bb>a<ddd>d</ddd></bb></a>"}
    });
  }
  
  public UnimplementedFeatureTest(String s) {
    this.xml = s;
  }
  
  public String getXsd(String s) throws ParserConfigurationException, SAXException, IOException {
    XsdGenerator gen = (new XsdGenerator()).parse(new ByteArrayInputStream(s.getBytes()));
    return gen.getXsd();
  }

  @Test(expected = RuntimeException.class)
  public void InvalidXMLTest() throws ParserConfigurationException, SAXException, IOException {
    getXsd(this.xml);
  }
  
}
