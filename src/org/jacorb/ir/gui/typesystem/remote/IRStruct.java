package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;
import org.jacorb.ir.gui.typesystem.*;

/**
 * 
 */
public class IRStruct 
    extends IRNode 
    implements AbstractContainer 
{

    /**
     * IRStruct constructor comment.
     */
    protected IRStruct() {
        super();
    }

    /**
     * IRStruct constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */

    protected IRStruct(org.omg.CORBA.IRObject irObject) {
        super(irObject);
    }

    /**
     * contents method comment.
     */

    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents() 
    {
        StructDef structDef = StructDefHelper.narrow((org.omg.CORBA.Object)this.irObject);
        StructMember[] members = structDef.members();   
        Contained[] contents = structDef.contents( org.omg.CORBA.DefinitionKind.dk_all, false); 

        org.jacorb.ir.gui.typesystem.TypeSystemNode[] result = 
            new org.jacorb.ir.gui.typesystem.TypeSystemNode[members.length + contents.length];

        for (int i = 0; i < members.length; i++) 
        {
            result[i] = RemoteTypeSystem.createTypeSystemNode( members[i] );
        }
        for (int i = 0; i < contents.length; i++) 
        {
            result[members.length + i] = 
                RemoteTypeSystem.createTypeSystemNode( contents[i] );
        }
        return result;  
    }

    /**
     * @return java.lang.String
     */

    public static String nodeTypeName() 
    {
        return "struct";
    }
}











