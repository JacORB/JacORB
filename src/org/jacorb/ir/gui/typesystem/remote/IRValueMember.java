package org.jacorb.ir.gui.typesystem.remote;

/**
 * 
 */
 
import java.util.*;
import org.omg.CORBA.*;
import javax.swing.tree.*;
 
public class IRValueMember 
    extends IRNodeWithType 
{
    /**
     * Default constructor, called from  TypeSystem.createNode(...)
     */
    public IRValueMember()
    {
        super();
    }

    /**
     * @param irObject org.omg.CORBA.IRObject
     */
    public IRValueMember(IRObject irObject) 
    {
        super(irObject);
        ValueMemberDef valueMemberDef = ValueMemberDefHelper.narrow(irObject);
        setAssociatedTypeSystemNode(RemoteTypeSystem.createTypeSystemNode(valueMemberDef.type_def()));
    }

    /**
     * @return java.lang.String
     */
    public String getInstanceNodeTypeName() 
    {
        String access;
        short visibility = ValueMemberDefHelper.narrow((org.omg.CORBA.Object)irObject).access();

        switch (visibility) {
            case PUBLIC_MEMBER.value:
                access = "public ";
                break;
            case PRIVATE_MEMBER.value:
                access = "private ";
                break;
            default:
                access = "<unknown visibility> ";
                break;
        }

        return access + super.getInstanceNodeTypeName();
    }

    /**
     * @return A string denoting the node type implemented here.
     */
    public static String nodeTypeName() 
    {
        return "valuemember";
    }
}
