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
        pack_name = parent.pack_name;

        createInheritanceSpec (parent.inheritanceSpec);

        body = new InterfaceBody (new_num());
        body.set_name (name);
        body.my_interface = this;
        body.setEnclosingSymbol (this);
        body.inheritance_spec = this.inheritanceSpec;      

        createOperations (parent);
    }

    /**
     *  Creates an inheritance spec for this ReplyHandler, based
     *  on the inheritance spec of the parent interface.
     */
    private void createInheritanceSpec (SymbolList source)
    {
        inheritanceSpec = new SymbolList (new_num());
        if (source.v.isEmpty())
        {
            ScopedName n = new ScopedName (new_num());
            n.pack_name = "org.omg.Messaging";
            n.typeName  = "ReplyHandler";
            inheritanceSpec.v.add (n);
        }
        else
        {
            for (Iterator i = source.v.iterator(); i.hasNext();)
            {
                ScopedName n1 = (ScopedName)i.next();
                ScopedName n2 = new ScopedName (new_num());
                n2.pack_name = n1.pack_name;
                n2.typeName  = "AMI_" + n1.name + "ReplyHandler";
                inheritanceSpec.v.add (n2);
            }
        }
    }
    
    /**
     * Creates the operations of this ReplyHandler and puts them into the body.
     */
    private void createOperations (Interface parent)
    {
        for (Iterator i = parent.body.v.iterator(); i.hasNext();)
        {
              Declaration d = ((Definition)i.next()).get_declaration();
              if (d instanceof OpDecl)
              {
                    createOperationsFor ((OpDecl)d);
              }
              else if (d instanceof AttrDecl)
              {
                    createOperationsFor ((AttrDecl)d);
              }
        }      
    }
    
    private void createOperationsFor (OpDecl d)
    {
        List paramDecls = new ArrayList();
        if (!(d.opTypeSpec instanceof VoidTypeSpec))
        {
            paramDecls.add (new ParamDecl (ParamDecl.MODE_IN,
                                           d.opTypeSpec,
                                           "ami_return_val"));
        }    
        for (Iterator i = d.paramDecls.iterator(); i.hasNext();)
        {
            ParamDecl p = (ParamDecl)i.next();
            if (p.paramAttribute != ParamDecl.MODE_IN)
            {
                paramDecls.add (new ParamDecl (ParamDecl.MODE_IN,
                                               p.paramTypeSpec,
                                               p.simple_declarator));               
            }
        }   
        body.v.add (new Definition (new OpDecl (this, d.name, paramDecls)));
    }
    
    private void createOperationsFor (AttrDecl d)
    {
        
    }
    
    public void parse()
    {
        body.parse();
    }
    
    public void print (PrintWriter ps)
    {
        printInterface();
        printOperations();
    }
            
}
