package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;

/**
 * 
 */

public class IRFixed 
    extends IRNode
{
    short digits;
    short scale;

   /**
     * IRAliasDef constructor comment.
     */
    protected IRFixed() 
    {
	super();
    
    }

    /**
     * IRAliasDef constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRFixed(org.omg.CORBA.IRObject irObject) 
    {
	super(irObject);
	FixedDef fixedDef = FixedDefHelper.narrow((org.omg.CORBA.Object)irObject);
        digits = fixedDef.digits();
        scale = fixedDef.scale();
        //	setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(aliasDef.original_type_def()));
    }

    /**
     * @return java.lang.String
     */

    public static String nodeTypeName() 
    {
	return "fixed";
    }

    /**
     * @return java.lang.String
     */

    public String description() 
    {
	String result = "fixed\ndigits:\t" + digits + "\nscale:\t" + scale;
	return result;
    }

}





