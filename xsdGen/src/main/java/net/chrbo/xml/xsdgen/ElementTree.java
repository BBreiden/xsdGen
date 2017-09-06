package net.chrbo.xml.xsdgen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The ElementTree is used by XsdGenerator to extract the structure of the XML file in form a tree and then generate the XSD for the tree. 
 * 
 * The class represents an element of the XML file by keeping a list of attributes and possible child elements of this element. 
 * It is assumed that child elements have a unique name. Therefore if a new child is added (via addChild) and a child with
 * the same name already exists, both children are merged together (via mergeTrees).
 * Ultimately the class is used to generate the XSD entry for an element (via getXsdElement).
 *  
 * @author borisbreidenbach
 *
 */
class ElementTree {
  
  List<ElementTree> childList = new LinkedList<ElementTree>();
  Map<String, Attribute> attributes = new HashMap<String, Attribute>();
  boolean isAllowedCData = false;
  String minOccurs = "1";
  String maxOccurs = "1";
  final String name;
  
  public ElementTree(String name) {
    this.name = name;
  }
    
  public void allowCData() {
    isAllowedCData = true;
  }
  
  public String getXsdElement() {
    StringBuilder out = new StringBuilder();
    out.append(getElementTag());
    String typeTags = getTypeTags();
    if (!typeTags.isEmpty()) {
      out.append('\n').append(typeTags);
    }
    out.append("\n</xs:element>");
    return out.toString();
  }
  
  public void addAttribute(Attribute attribute) {
    attributes.put(attribute.name(), attribute);
  }

  public void addChild(ElementTree tree) {
    if (hasChild(tree.name)) {
      ElementTree child = getChild(tree.name); 
      replaceChild(child.name, TreeMerger.mergeTrees(child, tree).setMultiplesAllowed());
    } else { 
      childList.add(tree);
    }
  }

  ElementTree setOptional() {
    minOccurs = "0";
    return this;
  }
  
  private ElementTree setMultiplesAllowed() {
    maxOccurs = "unbounded";
    return this;
  }
  
  private String getElementTag() {
    StringBuilder out = new StringBuilder();
    out.append("<xs:element name=\"").append(name)
    .append("\"");
    if (!minOccurs.equals("1")) {
      out.append(" minOccurs=\"").append(minOccurs).append("\"");
    }
    if (!maxOccurs.equals("1")) {
      out.append(" maxOccurs=\"").append(maxOccurs).append("\"");
    }
    if (isSimpleElement()) {
      out.append(" type=\"xs:string\"");
    }
    return out.append(">").toString();
  }
  
  private boolean isSimpleElement() {
    return childList.isEmpty() && attributes.isEmpty();
  }
  
  private String getTypeTags() {
    StringBuilder out = new StringBuilder();
    
    if (!isSimpleElement()) {
      if (isAllowedCData && !childList.isEmpty()) {
        throw new RuntimeException("Combination of text data and elements currently not handled.");
      } else if (isAllowedCData && childList.isEmpty()) {
        out.append(getComplexTypeWithText());
      } else {
        out.append(getComplexTypeWithChildren());
      }
    } 
    
    return out.toString();
  }

  /**
   * Generates XSD element for an element with text and attributes
   * @return XSD element as String
   */
  private String getComplexTypeWithText() {
    StringBuilder out = new StringBuilder();
    out.append("<xs:complexType>")
    .append("<xs:simpleContent>\n<xs:extension base=\"xs:string\">");
    for (Attribute a : attributes.values()) {
      out.append('\n').append(a.getXsdElement());
    }  
    out.append("\n</xs:extension>\n</xs:simpleContent>\n</xs:complexType>");
    return out.toString();
  }

  /**
   * Generates XSD element for an element with children and maybe also attributes.
   * @return XSD element as String
   */
  private String getComplexTypeWithChildren() {
    StringBuilder out = new StringBuilder();
    out.append("<xs:complexType>");
    if (!childList.isEmpty()) {
      out.append("\n<xs:sequence>");
      for (ElementTree e : childList) {
        out.append('\n').append(e.getXsdElement());
      }
      out.append("\n</xs:sequence>");
    }
    for (Attribute a : attributes.values()) {
      out.append('\n').append(a.getXsdElement());
    }
    return out.append("\n</xs:complexType>").toString();
  }

    private void replaceChild(String name, ElementTree tree) {
    ListIterator<ElementTree> it = childList.listIterator();
    while (it.hasNext()) {
      if ( (it.next()).name.equals(name)) {
        it.set(tree);
        break;
      }
    }
  }

  private boolean hasChild(String name) {
    boolean found = false;
    for (ElementTree child: childList) {
      if (child.name.equals(name)) {
        found = true;
        break;
      }
    }
    return found;
  }

  private ElementTree getChild(String name) {
    ElementTree child = null;
    for (ElementTree c : childList) {
      if (c.name.equals(name)) {
        child = c;
        break;
      }
    }
    if (child == null) {
      throw new RuntimeException("This should not happen.");
    }
    return child;
  }

}
