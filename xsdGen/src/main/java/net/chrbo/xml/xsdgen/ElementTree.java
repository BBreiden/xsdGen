package net.chrbo.xml.xsdgen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BinaryOperator;

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
  
  private List<ElementTree> childList = new LinkedList<ElementTree>();
  private Map<String, Attribute> attributes = new HashMap<String, Attribute>();
  private boolean isAllowedCData = false;
  private String minOccurs = "1";
  private String maxOccurs = "1";
  private final String name;
  
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
      out.append('\n').append(getTypeTags());
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
      replaceChild(child.name, mergeTrees(child, tree).setMultiplesAllowed());
    } else { 
      childList.add(tree);
    }
  }

  private ElementTree setOptional() {
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
      out.append(getComplexType());
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
