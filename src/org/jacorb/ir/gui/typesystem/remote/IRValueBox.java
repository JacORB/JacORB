package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;
import org.jacorb.ir.gui.typesystem.*;

/**
 * 
 */
public class IRValueBox 
    extends IRNodeWithType 
    implements AbstractContainer 
{
    /**
     * IRValueBox constructor.
     */
    protected IRValueBox() 
    {
        super();
    }

    /**
     * IRValueBox constructor.
     *
     * @param irObject org.omg.CORBA.IRObject
     */
    protected IRValueBox(org.omg.CORBA.IRObject irObject) 
    {
        super(irObject);
        ValueBoxDef valueBoxDef = ValueBoxDefHelper.narrow((org.omg.CORBA.Object)irObject);
        setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(valueBoxDef.original_type_def()));       
    }

    /**
     * The contents here is the type that is boxed.
     */
    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents() 
    {   
        return new org.jacorb.ir.gui.typesystem.TypeSystemNode[] { getAssociatedTypeSystemNode() };
    }

    /**
     * @return A string denoting the node type implemented here.
     */
    public static String nodeTypeName() 
    {
        return "valuebox";
    }
}
