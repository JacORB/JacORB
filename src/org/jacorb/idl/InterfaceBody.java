package org.jacorb.idl;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author Gerald Brose
 *
 * directly known subclasses: ValueBody
 */

public class InterfaceBody
    extends IdlSymbol
{
    public Vector<Definition> v;
    public Interface my_interface;
    SymbolList inheritance_spec = null;
    private Operation[] methods = null;
    private String waitingName = "";

    /** list of parse threads created and either active or still blocked */
    private static Vector<ParseThread> parseThreads = new Vector<ParseThread>();


    public InterfaceBody( int num )
    {
        super( num );
        v = new Vector<Definition>();
    }

    public void commit()
    {
    }


    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
        for( Enumeration e = v.elements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setEnclosingSymbol( my_interface );
    }

    public void set_ancestors( SymbolList _inheritance_spec )
    {
        inheritance_spec = _inheritance_spec;
    }


    public void set_name( String n )
    {
        name = n;
        for( Enumeration e = v.elements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setPackage( name );
    }


    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        for( Enumeration e = v.elements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setPackage( s );
    }

    public void addDefinition (Declaration d)
    {
        this.v.add (new Definition (d));
    }

    public void parse()
    {
        escapeName();

        if( parser.logger.isLoggable(Level.ALL) )
            parser.logger.log(Level.ALL, "Interface Body parse " + full_name());

        if( inheritance_spec != null )
        {
            Object o = null;
            boolean pending = false;
            for( Enumeration e = inheritance_spec.v.elements();
                 e.hasMoreElements(); )
            {
                ScopedName scoped_name = (ScopedName)e.nextElement();

                if( parser.logger.isLoggable(Level.ALL) )
                    parser.logger.log(Level.ALL, "Trying to resolve " + scoped_name);


                o = parser.get_pending( scoped_name.resolvedName() );
                pending = pending || ( o != null );
            }
            if( pending )
            {
                parser.set_pending( full_name(), o );
                new ParseThread( this );
            }
            else
            {
                internal_parse();
                parser.remove_pending( full_name() );
            }
        }
        else
        {
            internal_parse();
            parser.remove_pending( full_name() );
            if( parser.logger.isLoggable(Level.ALL) )
                parser.logger.log(Level.ALL, "Interface Body done parsing " + full_name());
        }
    }

    public void internal_parse()
    {
        if( parser.logger.isLoggable(Level.ALL) )
            parser.logger.log(Level.ALL, "Interface Body internal_parse " + full_name());

        if( inheritance_spec != null )
        {
            try
            {
                NameTable.inheritFrom( full_name(), inheritance_spec );
            }
            catch( NameAlreadyDefined nad )
            {
                parser.fatal_error( "Name " + nad.getMessage() +
                        " already defined in  base interface(s)", token );
            }
        }
        Definition d = null;
        for( Enumeration<Definition> e = v.elements(); e.hasMoreElements(); )
        {
            d = e.nextElement();
            Declaration dec = d.get_declaration();
            if( is_pseudo )
                dec.set_pseudo();
            dec.parse();
        }
    }

    /**
     * print definitions that appeared in an interface scope
     * do not call print() in OpDecls and on Typedefs
     */

    public void print( PrintWriter ps )
    {
        if( ps != null )
            throw new RuntimeException( "Compiler Error, interface body cannot be printed thus!" );

        for( Enumeration<Definition> e = v.elements(); e.hasMoreElements(); )
        {
            Declaration d = e.nextElement().get_declaration();
            if( !( d instanceof OpDecl ) )
                d.print( ps );
        }
    }

    /** print signatures to the operations file */

    public void printOperationSignatures( PrintWriter ps )
    {
        if( v.size() > 0  )
        {
            ps.println( "\t/* operations  */" );
        }

        for( Enumeration<Definition> e = v.elements(); e.hasMoreElements(); )
        {
            Definition d = e.nextElement();
            if( d.get_declaration() instanceof OpDecl )
            {
                ( (OpDecl)d.get_declaration() ).printSignature( ps );
            }
            else if( d.get_declaration() instanceof AttrDecl )
            {
                for( Enumeration m = ( (AttrDecl)d.get_declaration() ).getOperations();
                     m.hasMoreElements(); )
                {
                    ( (Operation)m.nextElement() ).printSignature( ps );
                }
            }
        }
    }

    /** print signatures to the operations file */

    public void printConstants( PrintWriter ps )
    {
        if( v.size() > 0 )
        {
            ps.println( "\t/* constants */" );
        }

        for( Enumeration<Definition> e = v.elements(); e.hasMoreElements(); )
        {
            Definition d = e.nextElement();
            if( d.get_declaration() instanceof ConstDecl )
            {
                ( (ConstDecl)d.get_declaration() ).printContained( ps );
            }
        }
    }

    /** print only constant definitions to the interface file */

    public void printInterfaceMethods( PrintWriter ps )
    {
        for( Enumeration<Definition> e = v.elements(); e.hasMoreElements(); )
        {
            Definition d = e.nextElement();
            if( !( d.get_declaration() instanceof ConstDecl ) && is_pseudo() )
            {
                ( (IdlSymbol)d ).print( ps );
            }
        }
    }

    public Operation[] getMethods()
    {
        if( methods == null )
        {
            Hashtable table = new Hashtable();
            for( Enumeration<Definition> e = v.elements(); e.hasMoreElements(); )
            {
                Definition d = e.nextElement();
                if( d.get_declaration() instanceof OpDecl )
                {
                    table.put( ( (OpDecl)d.get_declaration() ).signature(), d.get_declaration() );
                }
                else if( d.get_declaration() instanceof AttrDecl )
                {
                    for( Enumeration elems = ( (AttrDecl)d.get_declaration() ).getOperations();
                         elems.hasMoreElements(); )
                    {
                        Operation op = (Operation)elems.nextElement();
                        table.put( op.signature(), op );
                    }
                }
            }
            for (Iterator i = my_interface.inheritanceSpec.v.iterator();
                 i.hasNext(); )
            {
                TypeSpec ts = ((ScopedName)i.next()).resolvedTypeSpec();
                if (ts instanceof AliasTypeSpec &&
                    ((AliasTypeSpec) ts).originalType() instanceof ConstrTypeSpec )
                {
                    ts = ((AliasTypeSpec) ts).originalType();
                }
                if (ts instanceof ConstrTypeSpec)
                {
                    Interface base = (Interface)((ConstrTypeSpec)ts).c_type_spec;
                    Operation[] base_ops = base.getBody().getMethods();
                    for( int j = 0; j < base_ops.length; j++ )
                    {
                        if( !table.contains( base_ops[ j ].signature() ) )
                            table.put( base_ops[ j ].signature(), base_ops[ j ] );
                    }

                }
            }
            Enumeration o = table.elements();
            methods = new Operation[ table.size() ];
            for( int i = 0; i < methods.length; i++ )
                methods[ i ] = (Operation)o.nextElement();
        }
        return methods;
    }


    /**
     * Print methods to the stub file
     */

    public void printStubMethods( PrintWriter ps,
                                  String classname,
                                  boolean is_local,
                                  boolean is_abstract)
    {
        Operation[] ops = getMethods();
        for( int i = 0; i < ops.length; i++ )
        {
            ops[ i ].printMethod( ps, classname, is_local, is_abstract );
        }

        if ( parser.generate_ami_callback &&
             !(my_interface instanceof ReplyHandler) )
        {
            for( int i = 0; i < ops.length; i++ )
                ops[ i ].print_sendc_Method( ps, classname );
        }
    }


    /** print methods to the skeleton file */

    public void printDelegatedMethods( PrintWriter ps )
    {
        Operation[] ops = getMethods();
        if( ops.length > 0 )
        {
            for( int i = 0; i < ops.length; i++ )
            {
                ops[ i ].printDelegatedMethod( ps );
            }
        }
    }


    /** print hash table that associates an operation string with an int */

    public void printOperationsHash( PrintWriter ps )
    {
        Operation[] ops = getMethods();
        if( ops.length == 0 )
        {
            return;
        }

        ps.print  ( "\tstatic private final java.util.HashMap<String,Integer>");
        ps.println( " m_opsHash = new java.util.HashMap<String,Integer>();" );

        ps.println( "\tstatic" );
        ps.println( "\t{" );

        for( int i = 0; i < ops.length; i++ )
        {
            /* Some operation names have been escaped with "_" to
               avoid name clashes with Java names. The operation name
               on the wire is the original IDL name, however, so we
               need to ask for the right name here. We need to take
               care not to scramble up "_set_/_get" accessor methods!
               (hence the check on instanceof OpDecl).
               */

            String name;

            // Bug894: Bit of a hack - if this OpDecl was created by the AMI code don't strip '_'
            if( ops[ i ] instanceof OpDecl && ops[ i ].opName().startsWith( "_" ) &&
                ! (((OpDecl)ops[i]).myInterface instanceof ReplyHandler))
            {
               name = ops[ i ].opName().substring( 1 );
            }
            else
            {
               name = ops[ i ].opName();
            }
            ps.println( "\t\tm_opsHash.put ( \"" + name + "\", Integer.valueOf(" + i + "));" );
        }

        ps.println( "\t}" );
    }

    /** print methods for impl-based skeletons */

    public void printSkelInvocations( PrintWriter ps )
    {
        Operation[] ops = getMethods();
        if( ops.length <= 0 )
        {
            ps.println( "\t\tthrow new org.omg.CORBA.BAD_OPERATION(method + \" not found\");" );
            return;
        }
        ps.println( "\t\t// quick lookup of operation" );
        ps.println( "\t\tjava.lang.Integer opsIndex = (java.lang.Integer)m_opsHash.get ( method );" );
        ps.println( "\t\tif ( null == opsIndex )" );
        ps.println( "\t\t\tthrow new org.omg.CORBA.BAD_OPERATION(method + \" not found\");" );

        ps.println( "\t\tswitch ( opsIndex.intValue() )" );
        ps.println( "\t\t{" );

        int nextIndex = 0;
        for( int i = 0; i < ops.length; i++ )
        {
            String name;
            if( ops[ i ] instanceof OpDecl && ops[ i ].opName().startsWith( "_" ) )
                name = ops[ i ].opName().substring( 1 );
            else
                name = ops[ i ].opName();

            ps.println( "\t\t\tcase " + nextIndex++ + ": // " + name );
            ps.println( "\t\t\t{" );
            ops[ i ].printInvocation( ps );
            ps.println( "\t\t\t\tbreak;" );
            ps.println( "\t\t\t}" );
        }

        ps.println( "\t\t}" );
        ps.println( "\t\treturn _out;" );
    }

    void getIRInfo( Hashtable irInfoTable )
    {
        for( Enumeration<Definition> e = v.elements(); e.hasMoreElements(); )
        {
            Definition d = e.nextElement();
            if( d.get_declaration() instanceof OpDecl )
            {
                ( (OpDecl)d.get_declaration() ).getIRInfo( irInfoTable );
            }
            else if( d.get_declaration() instanceof AttrDecl )
            {
                ( (AttrDecl)d.get_declaration() ).getIRInfo( irInfoTable );
            }
        }
    }

    /**
     */

    public void accept( IDLTreeVisitor visitor )
    {
        visitor.visitInterfaceBody( this );
    }


    static void clearParseThreads()
    {
       parseThreads.clear();
    }


    public class ParseThread extends Thread
    {
       private final InterfaceBody b;
       private boolean running = false;

       public ParseThread( InterfaceBody _b )
       {
          b = _b;
          setDaemon( true );
          parseThreads.addElement( this );
          parser.incActiveParseThreads();
          start();
       }

       public void run()
       {
          parser.set_pending( b.full_name(), b );
          Object o = null;
          for( Enumeration e = inheritance_spec.v.elements(); e.hasMoreElements(); )
          {
             waitingName = ( (ScopedName)( e.nextElement() ) ).resolvedName();
             o = parser.get_pending( waitingName );
             if( o != null )
             {
                try
                {
                   synchronized( o )
                   {
                      o.wait();
                      running = true;
                   }
                }
                catch( InterruptedException ie )
                {
                   parser.logger.log(Level.FINEST, "ParseThread " + this + " interrupted!");
                }
             }
          }
          b.internal_parse();

          exitParseThread();
       }

       /**
        * check whether this thread will eventually run
        * @return true if the thread can run or is currently running
        *         false if it is still blocked or has just returned from run()
        */

       public synchronized boolean isRunnable()
       {
          boolean result = running || checkWaitCondition();
          if( parser.logger.isLoggable(Level.WARNING) )
            parser.logger.log(Level.WARNING, "Thread is runnable: " + result);
          return result;
       }

       private synchronized void exitParseThread()
       {
          parser.remove_pending( b.full_name() );
          parser.decActiveParseThreads();
          parseThreads.removeElement( this );
          running = false;
       }

       /**
        * @return  true, if waiting condition is true,
        * i.e., if thread still needs to wait.
        */

       private boolean checkWaitCondition()
       {
          return ( parser.get_pending( waitingName ) == null );
       }

    }
}
