package net.chrbo.xml.xsdgen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BinaryOperator;

public class ElementTree {
  private List<ElementTree> childList = new LinkedList<ElementTree>();
  private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
  private boolean isAllowedCData = false;
  private String minOccurs = "1";
  private String maxOccurs = "1";
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
  
  public ElementTree setOptional() {
    minOccurs = "0";
    return this;
  }
  
  public ElementTree setMultiplesAllowed() {
    maxOccurs = "unbounded";
    return this;
  }
  
  public String getXsdElement() {
    StringBuilder out = new StringBuilder();
    out.append(getElementTag());
    String typeTags = getTypeTags();
    if (!typeTags.isEmpty()) {
      out.append('\n').append(getTypeTags());
    }
    out.append("\n</xs:element>");
    return out.toString();
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
      out.append(getComplexType());
    } else {
      //out.append("<xs:simpleType>");
      //out.append("\n</xs:simpleType>");
    }
    
    return out.toString();
  }

  private String getComplexType() {
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

  public void addAttribute(Attribute attribute) {
    attributes.put(attribute.name(), attribute);
  }

  public void addChild(ElementTree tree) {
    if (hasChild(tree.name)) {
      ElementTree child = getChild(tree.name); 
      replaceChild(child.name, mergeTrees(child, tree).setMultiplesAllowed());
    } else { 
      childList.add(tree);
    }
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

  private ElementTree mergeTrees(ElementTree elementTree, ElementTree tree) {
    
    ElementTree nTree = new ElementTree(elementTree.name);
    
    BinaryOperator<String> max = (a, b) -> { 
      String unbounded = "unbounded";
      if (a.equals(unbounded) || b.equals(unbounded) ) {
        return unbounded;
      } else {
        return String.valueOf(Integer.max(Integer.valueOf(a), Integer.valueOf(b)));
      }
    };
    
    BinaryOperator<String> min = (a, b) -> { 
      String unbounded = "unbounded";
      if (a.equals(unbounded)) {
        return b;
      }
      if (b.equals(unbounded)) {
        return a;
      } 
      return String.valueOf(Integer.min(Integer.valueOf(a), Integer.valueOf(b)));
    };
    
    if (elementTree.isAllowedCData || tree.isAllowedCData) {
      nTree.allowCData();
    }
    nTree.minOccurs = min.apply(elementTree.minOccurs, tree.minOccurs);
    nTree.maxOccurs = max.apply(elementTree.maxOccurs, tree.maxOccurs);
    nTree.attributes.putAll(elementTree.attributes);
    nTree.attributes.putAll(tree.attributes);
    
    List<ElementTree> aList = elementTree.childList;
    List<ElementTree> bList = tree.childList;
    List<ElementTree> nList = nTree.childList;
    ListIterator<ElementTree> aIt = aList.listIterator();
    ListIterator<ElementTree> bIt = bList.listIterator();
    while (aIt.hasNext()) {
      ElementTree a = aIt.next();
      int pos = getPos(a.name, bList.listIterator(bIt.nextIndex()));
      if (pos < 0) {
        a.setOptional();
        nList.add(a);
      } else {
        bList.subList(bIt.nextIndex(), pos).stream()
          .map( (ElementTree e) -> { e.setOptional(); return e; } )
          .forEach( e -> { nList.add(e); });
        bIt = bList.listIterator(pos); 
        nList.add(mergeTrees(a, bIt.next()));
      }
    }
    if (bIt.hasNext()) {
      bList.subList(bIt.nextIndex(), bList.size()).stream()
        .map( ElementTree::setOptional )
        .forEach( e -> { nList.add(e); } );
    }
    return nTree;
  }

  private int getPos(String name, ListIterator<ElementTree> start) {
    int pos = -1;
    while (start.hasNext() && pos == -1) {
      if (start.next().name.equals(name)) {
        pos = start.previousIndex();
      }
    }
    return pos;
  }
}
