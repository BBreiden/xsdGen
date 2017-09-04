package net.chrbo.xml.xsdgen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ElementTree {
  private Map<String, ElementTree> children = new HashMap<String, ElementTree>();
  private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
  private boolean isAllowedCData = false;
  private Optional<String> minOccurs = Optional.empty();
  private Optional<String> maxOccurs = Optional.empty();
  private final String name;
  
  public ElementTree(String name) {
    this.name = name;
  }
  
  public boolean isAllowedCData() {
    return this.isAllowedCData;
  }
  
  public void allowCData() {
    isAllowedCData = true;
  }
  
  public void setOptional() {
    minOccurs = Optional.of("0");
  }
  
  public void setMultiplesAllowed() {
    maxOccurs = Optional.of("unbounded");
  }
  
  public String getXsdElement() {
    StringBuilder out = new StringBuilder();
    out.append('\n').append(getElementTag());
    out.append('\n').append(getTypeTags());
    out.append("\n</xs:element>");
    return out.toString();
  }

  private String getElementTag() {
    StringBuilder out = new StringBuilder();
    out.append("<xs:element name=\"").append(name)
    .append("\"");
    if (minOccurs.isPresent()) {
      out.append(" minOccurs=\"").append(minOccurs.get()).append("\"");
    }
    if (maxOccurs.isPresent()) {
      out.append(" maxOccurs=\"").append(maxOccurs.get()).append("\"");
    }
    return out.append(">").toString();
  }
  
  private String getTypeTags() {
    StringBuilder out = new StringBuilder();
    
    if (!children.isEmpty() || !attributes.isEmpty()) {
      out.append(getComplexType());
    } else {
      out.append("<xs:simpleType>");
      out.append("\n</xs:simpleType>");
    }
    
    return out.toString();
  }

  private String getComplexType() {
    StringBuilder out = new StringBuilder();
    out.append("<xs:complexType>");
    if (!children.isEmpty()) {
      out.append("\n<xs:all>");
      for (ElementTree e : children.values()) {
        out.append('\n').append(e.getXsdElement());
      }
      out.append("\n</xs:all>");
    }
    for (Attribute a : attributes.values()) {
      out.append('\n').append(a.getXsdElement());
    }
    return out.append("\n</xs:complexType>").toString();
  }

  public void addAttribute(Attribute attribute) {
    attributes.put(attribute.name(), attribute);
  }

  public void addChild(ElementTree tree) {
    if (children.containsKey(tree.name)) {
      mergeTrees(children.get(tree.name), tree);
      children.get(tree.name).setMultiplesAllowed();
    } else { 
      children.put(tree.name, tree);
    }
  }

  private void mergeTrees(ElementTree elementTree, ElementTree tree) {
    elementTree.isAllowedCData |= tree.isAllowedCData;
    elementTree.attributes.putAll(tree.attributes);
    
    for (String childName : elementTree.children.keySet()) {
      if (!tree.children.containsKey(childName)) {
        elementTree.children.get(childName).setOptional();
      } else {
        elementTree.addChild(tree);
      }
    }
    
    for (String childName : tree.children.keySet()) {
      if (!elementTree.children.containsKey(childName)) {
        tree.children.get(childName).setOptional();
        elementTree.addChild(tree);
      }
    }
    
  }
}
