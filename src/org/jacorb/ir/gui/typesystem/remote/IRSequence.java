package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;

/**
 * 
 */

public class IRSequence 
    extends IRNodeWithType 
{
   
    /**
     * IRSequence constructor comment.
     */

    protected IRSequence() 
    {
	super();
    }

    /**
     * IRSequence constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRSequence(org.omg.CORBA.IRObject irObject)
    {
	super(irObject);
	SequenceDef sequenceDef = SequenceDefHelper.narrow((org.omg.CORBA.Object)irObject);
	setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(sequenceDef.element_type_def()));
	setName("sequence<"+getAssociatedType()+">");
    }
}





