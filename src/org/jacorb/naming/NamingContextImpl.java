package org.jacorb.naming;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import java.net.*;
import java.util.*;
import java.io.*;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CosNaming.NamingContextExtPackage.*;

import org.jacorb.util.Environment;

/**
 *      The actual implementation for the CORBAService Naming
 * 
 *      @author Gerald Brose, FU Berlin
 *      @version $Id$
 *
 */

public class NamingContextImpl 
    extends NamingContextExtPOA
    implements java.io.Serializable
{
    /** table of all name bindings in this contexts, ie. name -> obj ref. */

    private Hashtable names = new Hashtable(); 

    /** table of all subordinate naming contexts, ie. name -> obj ref. */

    private Hashtable contexts = new Hashtable();

    /** the POAs used */
    transient private org.omg.PortableServer.POA poa;
    transient private org.omg.PortableServer.POA rootPoa;
    transient private org.omg.CORBA.ORB orb;

    private int child_count;
    private boolean destroyed = false;

    /** 
     *  bind a name (an array of name components) to an object
     */ 

    public void bind( NameComponent[] nc, org.omg.CORBA.Object obj) 
        throws NotFound, CannotProceed, InvalidName, AlreadyBound
    {   
        if( destroyed )
            throw new CannotProceed();

        if( nc == null || nc.length == 0 )
            throw new InvalidName();

        if( obj == null )
            throw new org.omg.CORBA.BAD_PARAM();

        Name n = new Name( nc );
        Name ctx = n.ctxName();
        NameComponent nb = n.baseNameComponent();
        if( ctx == null )
        {
            if( names.containsKey( n )) 
            {
                // if the name is still in use, try to ping the object
                org.omg.CORBA.Object s = (org.omg.CORBA.Object)names.get(n);
                if( s._non_existent() )
                {
                    rebind( n.components(), obj );
                    return;
                }
                throw new AlreadyBound();
            }
            else if( contexts.containsKey( n )) 
            {
                // if the name is still in use, try to ping the object
                org.omg.CORBA.Object s = (org.omg.CORBA.Object)contexts.get(n);
                if( s._non_existent() )
                {
                    unbind( n.components());
                }
                throw new AlreadyBound();
            }

            if(( names.put( n, obj )) != null )
                throw new CannotProceed( _this(), n.components() );

            org.jacorb.util.Debug.output(1,"Bound name: " + n.toString());
        } 
        else 
        {
            NameComponent[] ncx = new NameComponent[1];
            ncx[0] = nb;
            NamingContextExtHelper.narrow(resolve(ctx.components())).bind(ncx,obj);
        }
    }


    /**
     * Bind an object to a name that's already in use, i.e. rebind the name
     */

    public void rebind(NameComponent[] nc, org.omg.CORBA.Object obj) 
        throws NotFound, CannotProceed, InvalidName
    {
        if( destroyed )
            throw new CannotProceed();

        if( nc == null || nc.length == 0 )
            throw new InvalidName();

        if( obj == null )
            throw new org.omg.CORBA.BAD_PARAM();

        Name n = new Name( nc );
        Name ctx = n.ctxName();
        NameComponent nb = n.baseNameComponent();

        // the name is bound, but it is bound to a context,
        // the client should have been using rebind_context!

        if( contexts.containsKey( n ))
            throw new NotFound(NotFoundReason.not_object, new NameComponent[]{ nb } );

        // try remove an existing binding

        org.omg.CORBA.Object _o = (org.omg.CORBA.Object)names.remove( n );
        if( _o != null)
            _o._release();

        if( ctx == null )
        {
            // do the rebinding in this context
            
            names.put( n, obj );
            org.jacorb.util.Debug.output(1,"Re-bound name: " + n.toString());
        } 
        else 
        {
            // rebind in the correct context

            NameComponent[] ncx = new NameComponent[1];
            ncx[0] = nb;
            NamingContextExt nce = NamingContextExtHelper.narrow(resolve(ctx.components()));
            if( nce == null )
                throw new CannotProceed();
            nce.rebind(ncx,obj);
        }
    }


    /**
     * Bind an context to a name that's already in use, i.e. rebind the name
     */

    public void rebind_context(NameComponent[] nc, NamingContext obj) 
        throws NotFound, CannotProceed, InvalidName
    {
        if( destroyed )
            throw new CannotProceed();

        if( nc == null || nc.length == 0 )
            throw new InvalidName();

        if( obj == null )
            throw new org.omg.CORBA.BAD_PARAM();

        Name n = new Name( nc );
        Name ctx = n.ctxName();
        NameComponent nb = n.baseNameComponent();

        // the name is bound, but it is bound to an object,
        // the client should have been using rebind() !

        if( names.containsKey( n ))
            throw new NotFound(NotFoundReason.not_context,new NameComponent[]{ nb });

        // try to remove an existing context binding

        org.omg.CORBA.Object _o = (org.omg.CORBA.Object)contexts.remove( n );
        if( _o != null)
            _o._release();

        if( ctx == null )
        {
            contexts.put( n, obj );
            org.jacorb.util.Debug.output(1,"Re-Bound context: " + 
                                     n.baseNameComponent().id);
        }
    }



    /**
     * Bind a context to a name 
     */

    public void bind_context(NameComponent[] nc, 
                             NamingContext obj) 
        throws NotFound, CannotProceed, InvalidName, AlreadyBound
    {
        if( destroyed )
            throw new CannotProceed();

        Name n = new Name( nc );
        Name ctx = n.ctxName();
        NameComponent nb = n.baseNameComponent();

        if( ctx == null )
        {
            if( names.containsKey( n )) 
            {
                // if the name is still in use, try to ping the object
                org.omg.CORBA.Object s = (org.omg.CORBA.Object)names.get(n);
                if( s._non_existent() )
                {
                    unbind( n.components());
                }
                else
                    throw new AlreadyBound();
            }
            else if( contexts.containsKey( n )) 
            {
                // if the name is still in use, try to ping the object
                org.omg.CORBA.Object s = (org.omg.CORBA.Object)contexts.get(n);
                if( s._non_existent() )
                {
                    rebind_context( n.components(), obj );
                    return;
                }
                throw new AlreadyBound();
            }

            if( (contexts.put( n, obj )) != null ) 
                throw new CannotProceed( _this(), n.components());
            contexts.put( n, obj );

            org.jacorb.util.Debug.output(1,"Bound context: " + n.toString());
        }
        else 
        {
            NameComponent[] ncx = new NameComponent[1];
            ncx[0] = nb;
            NamingContextExtHelper.narrow(resolve(ctx.components())).bind_context(ncx,obj);
        }
    }

    public NamingContext bind_new_context(NameComponent[] nc ) 
        throws NotFound, CannotProceed, InvalidName, AlreadyBound
    {
        if( destroyed )
            throw new CannotProceed();

        if( nc == null || nc.length == 0 )
            throw new InvalidName();

        NamingContextExt ns = NamingContextExtHelper.narrow(new_context());
        bind_context( nc, ns ); 
        
        if( ns == null )
        {
            throw new CannotProceed();
        }
        return ns;
    }

    /**
     *  cleanup bindings, i.e. ping every object and remove bindings to
     *  non-existent objects
     */

    private void cleanup()
    {
        Vector deletionVector = new Vector();

        for( Enumeration n = names.keys(); n.hasMoreElements(); )
        {
            Name _n = (Name)n.nextElement();

            if( ((org.omg.CORBA.Object)names.get( _n ))._non_existent() )
            {
                org.jacorb.util.Debug.output(1,"removing name " + _n.baseNameComponent().id);
                deletionVector.addElement( _n );
            }
        }

        if( deletionVector.size() > 0 )
        {
            for( int i = deletionVector.size(); i-- > 0; )
                names.remove( (Name)deletionVector.elementAt(i));
            deletionVector.removeAllElements();
        }

        /* ping contexts */

        for( Enumeration c = contexts.keys(); c.hasMoreElements(); )
        {
            Name _n = (Name)c.nextElement();
            if( ((org.omg.CORBA.Object)contexts.get( _n ))._non_existent() )
            {
                org.jacorb.util.Debug.output(1,"removing context " + _n.baseNameComponent().id);
                deletionVector.addElement( _n );
            }
        }

        if( deletionVector.size() > 0 )
        {
            for( int i = deletionVector.size(); i-- > 0; )
                contexts.remove( (Name)deletionVector.elementAt(i));
            deletionVector.removeAllElements();
        }
    }


    public void destroy()
        throws NotEmpty
    {
        if( destroyed )
            return;

        if(!names.isEmpty() || !contexts.isEmpty() ) 
            throw new NotEmpty();
        else
        {
            names = null;
            contexts = null;
            destroyed = true;
        }
    }

    /** 
     *  @returns numer of bindings in this context
     */ 

    public int how_many()
    {
        if( destroyed )
            return 0;
        return names.size() + contexts.size();
    }

 
 
    /**
     *  list all bindings
     */

    private Binding[] list()
    {
        Binding[] result; 
        cleanup();

        int how_many = how_many();

        Enumeration n = names.keys();
        Enumeration c = contexts.keys();

        result = new Binding[how_many];
        for( ; n.hasMoreElements() && how_many > 0; how_many-- )
        {
            result[how_many-1] = 
                new Binding(((Name)n.nextElement()).components(), 
                            BindingType.nobject );
        }

        for( ; c.hasMoreElements() && how_many > 0; how_many-- )
        {
            result[how_many-1] = 
                new Binding(((Name)c.nextElement()).components(), 
                            BindingType.ncontext);
        }

        return result;
    }

    /**
     *  list all bindings
     */

    public void list(int how_many, BindingListHolder bl, BindingIteratorHolder  bi)
    {
        if( destroyed )
            return;

        Binding[] result; 

        cleanup();

        int size = how_many();

        Enumeration n = names.keys();
        Enumeration c = contexts.keys();

        if( how_many < size )
        {
            // counter for copies
            int how_many_ctr = how_many;

            // set up an array with "how_many" bindings

            result = new Binding[how_many];
            for( ; n.hasMoreElements() && how_many_ctr > 0; how_many_ctr-- )
            {
                result[how_many_ctr-1] = new Binding(((Name)n.nextElement()).components(), 
                                                 BindingType.nobject );
            }

            for( ; c.hasMoreElements() && how_many_ctr > 0; how_many_ctr-- )
            {
                result[how_many_ctr-1] = new Binding(((Name)c.nextElement()).components(), 
                                                 BindingType.ncontext);
            }

            // create a new BindingIterator for the remaining arrays

            size -= how_many;
            Binding[] rest = new Binding[ size ];
            for( ; n.hasMoreElements() && size > 0; size-- )
            {
                rest[size-1] = new Binding(((Name)n.nextElement()).components(), 
                                           BindingType.nobject );
            }

            for( ; c.hasMoreElements() && size > 0; size-- )
            {
                rest[size-1] = new Binding(((Name)c.nextElement()).components(), 
                                           BindingType.ncontext);
            }

            org.omg.CORBA.Object o = null;
            try
            {
                // Iterators are activated with the RootPOA (transient)
                byte[] oid = rootPoa.activate_object( new BindingIteratorImpl( rest ) );
                o = rootPoa.id_to_reference(oid);
            } 
            catch ( Exception ue )
            {
                ue.printStackTrace();
                System.exit(1);
            }
                        
            bi.value = BindingIteratorHelper.narrow(o);
        } 
        else 
        {
            result = new Binding[size];
            for( ; n.hasMoreElements() && size > 0; size-- )
            {
                result[size-1] = 
                    new Binding(((Name)n.nextElement()).components(), 
                                BindingType.nobject );
            }

            for( ; c.hasMoreElements() && size > 0; size-- )
            {
                result[size-1] = 
                    new Binding(((Name)c.nextElement()).components(), 
                                BindingType.ncontext);
            }
        }

        bl.value = result;
    }

    public NamingContext new_context()
    {
        if( destroyed )
            return null;

        org.omg.CORBA.Object ctx = null;
        try
        {
            byte[] oid = (new String(poa.servant_to_id(this)) +  
                          "_ctx" + (++child_count)).getBytes();

            ctx = poa.create_reference_with_id( oid, "IDL:omg.org/CosNaming/NamingContextExt:1.0");
        } 
        catch ( Exception ue )
        {
            ue.printStackTrace();
            System.exit(1);
        }
        return NamingContextExtHelper.narrow(ctx);
    }



    /** 
     * resolve a name
     */

    public org.omg.CORBA.Object resolve(NameComponent[] nc) 
        throws NotFound, CannotProceed, InvalidName
    {
        if( destroyed )
            throw new CannotProceed();

        if( nc == null || nc.length == 0 )
            throw new InvalidName();

        Name n = new Name( nc[0] );
        if( nc.length > 1 )
        {
            NamingContextExt next_context = 
                NamingContextExtHelper.narrow((org.omg.CORBA.Object)contexts.get(n));

            if ((next_context == null)||(next_context._non_existent()))
            {
                throw new NotFound(NotFoundReason.missing_node,nc);
            }

            NameComponent[] nc_prime = 
                new NameComponent[nc.length-1];

            for( int i = 1; i < nc.length; i++)
                nc_prime[i-1] = nc[i];

            return next_context.resolve(nc_prime);
        }
        else
        {
            org.omg.CORBA.Object result = null;

            result = (org.omg.CORBA.Object)contexts.get(n);

            if( result == null )
                result = (org.omg.CORBA.Object)names.get(n);           

            if( result == null || result._non_existent())
                throw new NotFound(NotFoundReason.missing_node, n.components());

            return result;      
        }                 
    }

    /**
     * unbind a name
     */

    public void unbind(NameComponent[] nc ) 
        throws NotFound, CannotProceed, InvalidName
    {
        if( destroyed )
            throw new CannotProceed();

        if( nc == null || nc.length == 0 )
            throw new InvalidName();

        Name n = new Name( nc );
        Name ctx = n.ctxName();
        NameComponent nb = n.baseNameComponent();

        if( ctx == null )
        {
            if( names.containsKey(n))
            {
                org.omg.CORBA.Object o = (org.omg.CORBA.Object)names.remove( n );
                o._release();
                org.jacorb.util.Debug.output(1,"Unbound: " + n.toString());
            } 
            else if( contexts.containsKey(n))
            {
                org.omg.CORBA.Object o = (org.omg.CORBA.Object)contexts.remove( n );
                o._release();
                org.jacorb.util.Debug.output(1,"Unbound: " + n.toString());
            } 
            else 
            {
                org.jacorb.util.Debug.output(1,"Unbind failed for " + n.toString() );
                throw new NotFound(
                                   NotFoundReason.not_context, 
                                   n.components());
            }
        }
        else
        {
            NameComponent[] ncx = new NameComponent[1];
            ncx[0] = nb;
            NamingContextExtHelper.narrow( resolve( ctx.components())).unbind(ncx );
        }
    }

    /** 
     * POA-related,
     * @overrides _default_POA() in org.omg.PortableServer.Servant
     */

    public org.omg.PortableServer.POA default_POA() 
    {     
        return poa;  
    }


    /**
     * @overrides writeObject in Serializable
     */

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        /*
         * For serialization, object references are transformed
         * into strings
         */

        for( Enumeration e = contexts.keys(); e.hasMoreElements();)
        {          
            Name key = (Name)e.nextElement();
            org.omg.CORBA.Object o = (org.omg.CORBA.Object)contexts.remove( key );
            contexts.put( key, orb.object_to_string( o ));
        }

        for( Enumeration e = names.keys(); e.hasMoreElements();)
        {          
            Name key = (Name)e.nextElement();
            org.omg.CORBA.Object o = (org.omg.CORBA.Object)names.remove(key);
            names.put( key, orb.object_to_string( o ));
        }
        
        out.defaultWriteObject();
    }

    /**
     *   This method needs to be called for each newly created
     *   or re-read naming context to set its orb an poa.
     *
     */

    void init(org.omg.CORBA.ORB orb, org.omg.PortableServer.POA poa )
    {
        this.poa = poa;
        this.orb = orb;

        try
        {
            this.rootPoa = 
                org.omg.PortableServer.POAHelper.narrow(
                          orb.resolve_initial_references("RootPOA"));
        }
        catch ( org.omg.CORBA.ORBPackage.InvalidName in ) 
        {
            in.printStackTrace();
        }
        
        /** 
         * Recreate tables. For serialization, object references 
         * have been transformed into strings
         */

        for( Enumeration e = contexts.keys(); e.hasMoreElements();)
        {          
            Name key = (Name)e.nextElement();
            String ref = (String)contexts.remove(key);
            contexts.put( key, orb.string_to_object( ref ));
        }

        for( Enumeration e = names.keys(); e.hasMoreElements();)
        {          
            Name key = (Name)e.nextElement();
            String ref = (String)names.remove(key);
            names.put( key, orb.string_to_object( ref ));
        }

    }

    /* NamingContextExt */

    /**
     * convert a name into its string representation
     */

    public String to_string(NameComponent[] n) 
        throws InvalidName
    {
        return Name.toString(n);
    }

    /**
     * convert a string into name
     * @throw InvalidName
     */

    public NameComponent[] to_name( String sn ) 
        throws InvalidName
    {
        return Name.toName( sn );
    }


    /**
     *
     */

    public String to_url(String addr, String sn) 
        throws InvalidAddress, InvalidName
    {

        org.jacorb.orb.util.CorbaLoc corbaLoc;
        try
        {
            corbaLoc = new org.jacorb.orb.util.CorbaLoc( addr );
            return corbaLoc.toCorbaName( sn );
        }
        catch( IllegalArgumentException ia )
        {
            throw new InvalidAddress();
        }

    }

    /**
     *
     */

    public org.omg.CORBA.Object resolve_str(String n) 
        throws NotFound, CannotProceed, InvalidName
    {
        return resolve( to_name(n));
    }


}









