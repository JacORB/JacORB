
package org.jacorb.ir.gui.typesystem;


/**
 * Existiert nur, um als cell-value in ein TableModel gesteckt zu werden.
 * Nur so kann GUI-Client von der selektierten Row auf die dazugehörige TypeSystemNode schließen.
 * (DefaultTableModel sieht es leider nicht vor, mit jeder Row ein Objekt zu assoziieren)
 * 
 */
public class NodeMapper {
	TypeSystemNode node;
	String string;



/**
 * This method was created by a SmartGuide.
 * @param node org.jacorb.ir.gui.typesystem.TypeSystemNode
 */
public NodeMapper ( TypeSystemNode node, String string) {
	this.node = node;
	this.string = string;
}
/**
 * This method was created by a SmartGuide.
 * @return org.jacorb.ir.gui.typesystem.TypeSystemNode
 */
public TypeSystemNode getNode() {
	return node;
}
/**
 * This method was created by a SmartGuide.
 * @return java.lang.String
 */
public String toString() {
	return string;
}
}







