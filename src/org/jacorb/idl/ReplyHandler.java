package org.jacorb.idl;

import java.io.*;
import java.util.*;

/**
 * A ReplyHandler receives replies of asynchronous invocations of 
 * another interface (we call this interface the "parent" of the
 * ReplyHandler).
 * 
 * @author Andre Spiegel
 * $Id$
 */
public class ReplyHandler extends Interface
{
    public ReplyHandler (Interface parent)
    {
        super (new_num());

        name = "AMI_" + parent.name + "ReplyHandler";
        inheritanceSpec = createInheritanceSpec (parent.inheritanceSpec);
        setPackage (parent.pack_name);
    }

    /**
     *  Creates an inheritance spec for this ReplyHandler, based
     *  on the inheritance spec of the parent interface.
     */
    private SymbolList createInheritanceSpec (SymbolList source)
    {
        SymbolList result = new SymbolList (new_num());
        if (source.v.isEmpty())
        {
            ScopedName n = new ScopedName (new_num());
            n.setId ("org.omg.Messaging.ReplyHandler");
            result.v.add (n);
        }
        else
        {
            for (Iterator i = source.v.iterator(); i.hasNext();)
            {
                ScopedName n1 = (ScopedName)i.next();
                ScopedName n2 = new ScopedName (new_num());
                n2.setId (n1.pack_name + "." + "AMI_" + n2.name + "ReplyHandler");
                result.v.add (n2);
            }
        }
        return result;
    }
    
    public void print (PrintWriter ps)
    {
        printInterface();
    }
            
}
