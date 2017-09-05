package net.chrbo.xml.xsdgen;

/**
 * Helper class representing an attribute of an XML element. E.g. href is an attribute of elemente a in <a href="...">.
 * This is a simplified representation, as only the name of the attribute is stored.
 * 
 * TODO add attribute type info
 * @author borisbreidenbach
 *
 */
class Attribute {
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
