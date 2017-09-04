package net.chrbo.xml.xsdgen;

public class Attribute {
  private final String name;
  public Attribute(String name) {
    this.name = name;
  }
  public String getXsdElement() {
    return "<xs:attribute name=\"" + name + "\"/>";
  }
  public String name() {
    return this.name;
  }
}
