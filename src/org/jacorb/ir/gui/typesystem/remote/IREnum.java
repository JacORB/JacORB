package org.jacorb.ir.gui.typesystem.remote;

import org.omg.CORBA.*;
import org.jacorb.ir.gui.typesystem.*;

public class IREnum 
    extends IRNode 
    implements AbstractContainer 
{

    /**
     * IRStruct constructor comment.
     */
    protected IREnum() {
        super();
    }
    /**
     * IRStruct constructor comment.
     * @param irObject org.omg.CORBA.IRObject
     */
    protected IREnum(org.omg.CORBA.IRObject irObject) {
        super(irObject);
    }
    /**
     * contents method comment.
     */
    public org.jacorb.ir.gui.typesystem.ModelParticipant[] contents() 
    {
        EnumDef enumDef =
            EnumDefHelper.narrow((org.omg.CORBA.Object)this.irObject);
        String[] contents = enumDef.members();  
        org.jacorb.ir.gui.typesystem.TypeSystemNode[] result = 
            new org.jacorb.ir.gui.typesystem.TypeSystemNode[contents.length];
        for (int i=0; i<contents.length; i++) 
        {
            result[i] = RemoteTypeSystem.createTypeSystemNode(contents[i]);
        } // for
        return result;  
    }
    /**
     * This method was created by a SmartGuide.
     * @return java.lang.String
     */
    public static String nodeTypeName() {
        return "enum";
    }
}











