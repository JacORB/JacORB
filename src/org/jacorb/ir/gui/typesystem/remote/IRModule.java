package org.jacorb.ir.gui.typesystem.remote;

/**
 * 
 */


import java.util.*;
 import org.omg.CORBA.*;
 import javax.swing.tree.*;

public class IRModule extends IRContainer 
{
  
    /**
     * Default-Konstruktor: wird von TypeSystem.createNode(...) benutzt
     */
    public IRModule ( ) {
	super();
    }
    /**
     * @param irObject org.omg.CORBA.IRObject
     */
    public IRModule ( IRObject irObject) {
	super(irObject);
    }
    /**
     * @return java.lang.String[]
     */
    public String[] allowedToAdd() {
	String[] result = {	IRModule.nodeTypeName(),
                                IRInterface.nodeTypeName(),
                                IRConstant.nodeTypeName(),
                                IRTypedef.nodeTypeName(),
                                IRException.nodeTypeName()};
	return result;
    }
    /**
     * @return java.lang.String
     */
    public static String nodeTypeName() {
	return "module";
    }
}











