package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;

/**
 * 
 */

public class IRString 
    extends IRNode
{
    int bound;

   /**
     * IRAliasDef constructor comment.
     */
    protected IRString() 
    {
	super();
    }

    /**
     * IRAliasDef constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRString(org.omg.CORBA.IRObject irObject) 
    {
	super(irObject);
	StringDef stringDef = StringDefHelper.narrow(irObject);
        bound = stringDef.bound();
		setName("string");
		setAbsoluteName("string");
    }

    /**
     * @return java.lang.String
     */

    public static String nodeTypeName() 
    {
	return "string";
    }

    /**
     * @return java.lang.String
     */

    public String description() 
    {
	String result = "string\nbound:\t" + bound;
	return result;
    }

}











