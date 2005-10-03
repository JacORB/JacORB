/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
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

        name = "AMI_" + parent.name + "Handler";
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
                StringBuffer typeName = new StringBuffer(n1.typeName());
                int nameStart = typeName.lastIndexOf(".") + 1;
                typeName.insert(nameStart, "AMI_");
                typeName.append("Handler");
                n2.typeName  = typeName.toString();
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

    /**
     * Creates the ReplyHandler operations for the given operation of the
     * parent interface, and puts them into the body of this ReplyHandler.
     */
    private void createOperationsFor (OpDecl d)
    {
        // Create the parameter list for the NO_EXCEPTION reply operation
        List paramDecls = new ArrayList();
        if (!(d.opTypeSpec.type_spec instanceof VoidTypeSpec))
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
        body.addDefinition (new OpDecl (this, d.name, paramDecls));
        body.addDefinition 
          (new OpDecl (this, d.name + "_excep", excepParameterList()));
    }
    
    /**
     * Creates the ReplyHandler operations for the given attribute declaration
     * of the parent interface, and puts them into the body of this ReplyHandler.
     */
    private void createOperationsFor (AttrDecl d)
    {
        for (Iterator i = d.declarators.v.iterator(); i.hasNext();)
        {
            SimpleDeclarator decl = (SimpleDeclarator)i.next();
            body.addDefinition 
              (new OpDecl (this, "get_" + decl.name,
                           parameterList (d.param_type_spec, "ami_return_val")));
            body.addDefinition
              (new OpDecl (this, "get_" + decl.name + "_excep", 
                           excepParameterList()));
            if (!d.readOnly)
            {
                body.addDefinition
                  (new OpDecl (this, "set_" + decl.name, new ArrayList()));
                body.addDefinition
                  (new OpDecl (this, "set_" + decl.name + "_excep",
                               excepParameterList()));
            }
        }                  
    }

    /**
     * Returns a parameter list with a single "in" argument that has
     * the given type and name.
     */
    private List parameterList(TypeSpec type, String name)
    {
        List result = new ArrayList();
        result.add (new ParamDecl (ParamDecl.MODE_IN, type, name));
        return result;
    }
    
    private List excepParameterList()
    {
        return parameterList (new ExceptionHolderTypeSpec (new_num()),
                              "excep_holder");
    }

    public String id()
    {
        return "IDL:" + full_name().replace('.', '/') + ":1.0";
    }
        
    public void parse()
    {
        if (!NameTable.isDefined ("org.omg.Messaging.ReplyHandler"))
        {
            try
            {
                NameTable.define ("org.omg.Messaging.ReplyHandler", "type");
                TypeMap.typedef ("org.omg.Messaging.ReplyHandler", 
                                 new ReplyHandlerTypeSpec (IdlSymbol.new_num()));
            } 
            catch (Exception e)
            {
                throw new RuntimeException (e.getMessage());
            }
        }

        ConstrTypeSpec ctspec = new ConstrTypeSpec (this);
        try 
        {
            NameTable.define (full_name(), "interface");
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined e)
        {
            parser.error( "Interface " + typeName() + " already defined", token );
        }
        
        body.parse();
    }
    
    public void print (PrintWriter ps)
    {
        printInterface();
        printOperations();
        printStub();
        printHelper();
        printImplSkeleton();
        printTieSkeleton();
    }
            

}



