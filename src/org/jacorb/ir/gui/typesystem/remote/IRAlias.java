package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;
import org.jacorb.ir.gui.typesystem.*;

/**
 * 
 */

public class IRAlias 
    extends IRNodeWithType 
    implements AbstractContainer 
{
    /**
     * IRAliasDef constructor comment.
     */

    protected IRAlias() 
    {
        super();
    
}
    /**
     * IRAliasDef constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRAlias(org.omg.CORBA.IRObject irObject) 
    {
        super(irObject);
        AliasDef aliasDef = AliasDefHelper.narrow((org.omg.CORBA.Object)irObject);
        setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(aliasDef.original_type_def()));       
    }

    /**
     * contents method comment.
     */

    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents() 
    {   
        org.jacorb.ir.gui.typesystem.TypeSystemNode[] result = 
            new org.jacorb.ir.gui.typesystem.TypeSystemNode[] { getAssociatedTypeSystemNode() };
        return result;  
    }

    /**
     * @return java.lang.String
     */

    public static String nodeTypeName() 
    {
        return "typedef";
    }
}











