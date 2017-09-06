package net.chrbo.xml.xsdgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Main {

  public static void main(String[] args) {
    for (String fn: args) {
      FileWriter out = null;
      try {
        XsdGenerator gen = (new XsdGenerator()).parse(new FileInputStream(new File(fn)));
        out = new FileWriter(new File(fn + ".xsd"));
        out.write(gen.getXsd());
      } catch (IOException e) {
        e.printStackTrace();
        continue;
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
        continue;
      } catch (SAXException e) {
        e.printStackTrace();
        continue;
      } finally {
        if (out != null) {
          try {
            out.close();
          } catch (IOException e) {
            e.printStackTrace();
            continue;
          }
        }
      }
    }
  }

}
