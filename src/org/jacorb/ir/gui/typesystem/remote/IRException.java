package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;
import org.jacorb.ir.gui.typesystem.*;

public class IRException 
    extends IRNode 
    implements AbstractContainer 
{
    /**
     * Default-Constructor used by TypeSystem.createNode(...)
     */

    public IRException () 
    {
	super();
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */

    public IRException ( IRObject irObject) 
    {
	super(irObject);
    }

    /**
     * contents method comment.
     */

    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents() 
    {
	ExceptionDef exceptionDef = ExceptionDefHelper.narrow((org.omg.CORBA.Object)this.irObject);
	StructMember[] contents = exceptionDef.members();	

	jacorb.ir.gui.typesystem.TypeSystemNode[] result = 
            new org.jacorb.ir.gui.typesystem.TypeSystemNode[contents.length];

	for (int i=0; i<contents.length; i++) 
        {
            result[i] = RemoteTypeSystem.createTypeSystemNode(contents[i]);
	}

	return result;	
    }

    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */

    public static String nodeTypeName() {
	return "exception";
    }
}





