package net.chrbo.xml.xsdgen;

import java.util.List;
import java.util.ListIterator;
import java.util.function.BinaryOperator;

class TreeMerger {

  public static ElementTree mergeTrees(ElementTree elementTree, ElementTree tree) {
    
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

  private static int getPos(String name, ListIterator<ElementTree> start) {
    int pos = -1;
    while (start.hasNext() && pos == -1) {
      if (start.next().name.equals(name)) {
        pos = start.previousIndex();
      }
    }
    return pos;
  }
}
