package org.jacorb.ir.gui.typesystem.remote;


/**
 * IRRepository wird gebraucht, um GUI erlaubte Child-Klassen mitzuteilen
 * 
 */
 
 import java.util.*;
 import org.omg.CORBA.*;
 import javax.swing.tree.*;

public class IRRepository 
    extends IRContainer 
{       
    
    /**
     * Default-Konstruktor: wird von TypeSystem.createNode(...) benutzt
     */

    public IRRepository ( ) {
	super();
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */

    public IRRepository ( IRObject irObject) 
    {
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

    public static String nodeTypeName() 
    {
	return "Repository";
    }
}





