/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
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
 */

package org.jacorb.naming;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingHolder;
import org.omg.CosNaming.BindingIteratorHolder;
import org.omg.CosNaming.BindingListHolder;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

/**
 * This class allows listing all bindings in a naming context
 * to a PrintStream
 *
 * @author Gerald Brose
 */

public class ContextLister
{
    public NamingContext root_context;
    private HashSet<String> contexts = new HashSet<String>();
    private org.omg.CORBA.ORB orb;

    public ContextLister(org.omg.CORBA.ORB orb)
    {
        this.orb = orb;
        // initialise Naming Service via ORB
        try
        {
            org.omg.CORBA.Object obj =
                orb.resolve_initial_references("NameService");
            root_context = NamingContextHelper.narrow( obj );
        }
        catch( org.omg.CORBA.ORBPackage.InvalidName inex ) {
            inex.printStackTrace();
        }
        catch(org.omg.CORBA.SystemException corba_exception) {
            System.err.println(corba_exception);
        }

        if( root_context == null ) {
            System.err.println("No Naming Context available, giving up ...");
            System.exit( 1 );
        }
    }

    public ContextLister( org.omg.CORBA.ORB orb, String str )
    {
        this.orb = orb;
        // initialise Naming Service via stringified IOR
        try
        {
            org.omg.CORBA.Object obj = orb.string_to_object( str );
            root_context = NamingContextHelper.narrow( obj );
        }
        catch(org.omg.CORBA.SystemException corba_exception) {
            System.err.println(corba_exception);
        }

        if( root_context == null ) {
            System.err.println("No Naming Context available, giving up ...");
            System.exit( 1 );
        }
    }

    private void mark(NamingContext nc) {
        contexts.add ( orb.object_to_string(nc));
    }

    private boolean isMarked(NamingContext nc)
    {
        return contexts.contains(orb.object_to_string(nc));
    }


    public void list(java.io.PrintStream ps)
    {
        list( root_context, "   ", ps);
    }

    private void list( NamingContext n, String indent, java.io.PrintStream ps )
    {
        if( isMarked(n))
        {
           System.out.println ("Loop detected for " + n);
            return;
        }

        mark(n);

        try
        {
            BindingListHolder blsoh =
                new BindingListHolder(new Binding[0]);

            BindingIteratorHolder bioh =
                new BindingIteratorHolder();

            n.list( 0, blsoh, bioh );

            BindingHolder bh = new BindingHolder();

            if( bioh.value == null )
                return;

            while( bioh.value.next_one( bh ))
            {
                String stringName = Name.toString( bh.value.binding_name);
                ps.print( indent + stringName );
                if( bh.value.binding_type.value() == BindingType._ncontext )
                {
                    String _indent = indent + "\t";
                    ps.println("/");

                    NameComponent [] name = Name.toName(stringName);
                    NamingContext sub_context =
                        NamingContextHelper.narrow( n.resolve(name) );
                    list( sub_context, _indent, ps );
                }
                else
                    System.out.println();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static void main(String args[])
    {
        org.omg.CORBA.ORB orb =
            org.omg.CORBA.ORB.init(args,null);

        ContextLister ctxLister;
        PrintStream pw = System.out;
        String url = null;

        for( int i = 0; i < args.length; i += 2 )
        {
            try
            {
                if( args[i].startsWith("-f"))
                {
                    try
                    {
                        pw = new PrintStream( new FileOutputStream( args[i+1] ));
                    }
                    catch( IOException ioe)
                    {
                        System.err.println( ioe.getMessage() );
                        System.exit(1);
                    }
                    continue;
                }
                if( args[i].startsWith("-url"))
                {
                    url = args[i+1];
                }
            }
            catch( Exception e )
            {
                System.err.println("Usage: org.jacorb.naming.ContextLister [-url object url] [-f output file]");
                System.exit(1);

            }
        }

        if( url != null )
        {
            ctxLister = new ContextLister(orb, url);
        }
        else
        {
            ctxLister = new ContextLister(orb);
        }

        ctxLister.list( pw );
        orb.shutdown(true);
    }
}


