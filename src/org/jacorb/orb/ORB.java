package org.jacorb.orb;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import java.util.*;
import java.io.*;  
import java.applet.Applet;
import java.lang.reflect.*;

import org.jacorb.imr.*;
import org.jacorb.util.*;
import org.jacorb.orb.policies.*;
import org.jacorb.orb.connection.*;
import org.jacorb.orb.domain.DomainFactory;
import org.jacorb.orb.portableInterceptor.*;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.BooleanHolder;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA.portable.StreamableValue;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;

/**
 * @author Gerald Brose, FU Berlin
 * @version $Id$
 */

public final class ORB
    extends ORBSingleton
    implements org.jacorb.poa.POAListener
{
    private static final String versionString = "1.4 beta 1";
    private static final String dateString = "2 Nov 2001";

    /** "initial" references */
    private Hashtable initial_references = new Hashtable();

    private org.jacorb.poa.POA rootpoa;
    private org.jacorb.poa.Current poaCurrent;
    private BasicAdapter basicAdapter;
    private Current current;
    private org.omg.SecurityLevel2.Current securityCurrent = null;

    /** interceptor handling */
    private InterceptorManager interceptor_manager = null;
  
    /** reference caching */
    private Hashtable knownReferences = null;

    /** connection mgmt. */
    private ConnectionManager connectionManager;

    /** buffer mgmt. */
    private BufferManager bufferManager = BufferManager.getInstance();

    /**
     * Maps repository ids (strings) to objects that implement 
     * org.omg.CORBA.portable.ValueFactory.  This map is used by
     * register/unregister_value_factory() and lookup_value_factory().
     */
    protected Map valueFactories = new HashMap();

    /** properties */ 
    private java.util.Properties _props;

    /** command like args */
    public String[] _args;
        
    public  java.applet.Applet applet;

    /* for run() and shutdown()  */
    private Object orb_synch = new java.lang.Object();
    private boolean run = true;
    private boolean wait = true;
    private boolean shutdown_in_progress = false;
    private boolean destroyed = false;
    private Object shutdown_synch = new Object();

    /** synchroniziation for orb domain creation */ 
    private static boolean ORBDomainCreationInProgress = false;
  
    /* for registering POAs with the ImR */
    private Registration imr = null;
    private ImRInfo imr_info = null;
    private int persistentPOACount;

    public static String orb_id = "jacorb:1.0";

    /* policy factories, from portable interceptor spec */
    private Hashtable policy_factories = null;

    private static org.omg.CORBA.TCKind kind;

    private static final String [] services  = 
    {"RootPOA","POACurrent",  "DynAnyFactory", "DomainService", 
     "LocalDomainService", "PICurrent", "CodecFactory"};

    private boolean bidir_giop = false;

    public ORB()
    {
    }

    public boolean useBiDirGIOP()
    {
        return bidir_giop;
    }
    
    public void turnOnBiDirGIOP()
    {
        if( ! bidir_giop )
        {
            bidir_giop = true;
            
            connectionManager.setRequestListener( basicAdapter.getRequestListener() );
        }
    }

    /** 
     *  This  version of _getObject  is used for references  that have
     *  arrived over the network and is called from CDRInputStream. It
     *  removes stale cache entries 
     */

    synchronized org.omg.CORBA.Object _getObject( ParsedIOR pior )
    {
        String key = pior.getIORString();
        org.omg.CORBA.portable.ObjectImpl o = 
            (org.omg.CORBA.portable.ObjectImpl)knownReferences.get( key );

        if( o != null )
        {
            Debug.output( 5, "Found a reference for key " + key + " in cache ");
            org.jacorb.orb.Delegate del = (org.jacorb.orb.Delegate) o._get_delegate();
            if (del != null)
            {
                ParsedIOR delpior= del.getParsedIOR();
                if (delpior == null)
                {
                    knownReferences.remove(key);
                    Debug.output(4,"Removing an invalid reference from cache.");
                }
                else if( pior.getAddress().equals(delpior.getAddress()))
                {
                    return o._duplicate();
                }
            }
            else
            {           
                Debug.output(3,"remove stale reference for key " + 
                             key + " from cache ");
                knownReferences.remove( key );
            }
        }
        
        org.jacorb.orb.Delegate d = new Delegate(this, pior );
        o = d.getReference( null );
        if( Environment.cacheReferences() )
        {
            Debug.output(5,"Caching reference for key " + key);
            knownReferences.put( key, o );
        }
        return o;
    }

    /**
     * Find a local POA for a delegate (called from is_local())
     * returns non-null only if a root POA is already activated
     * and all POAs along the path on the poa name are active, i.e.
     * returns null for POAs in the holding state
     */

    org.jacorb.poa.POA findPOA( org.jacorb.orb.Delegate d, org.omg.CORBA.Object ref )
    {
        // if no POAs activated, we don't look further
        if( rootpoa == null || basicAdapter == null )
        {
            Debug.output(3, "ORB.findPOA: no local root/base adapters");
            return null;
        }

//          if( ! (basicAdapter.getAddress() +":"+ basicAdapter.getPort()).equals( d.get_adport() ))
//          {
//              Debug.output(3, "ORB.findPOA: wrong base adapter address "  + d.get_adport() +
//                           " vs. " + basicAdapter.getAddress() +":"+ basicAdapter.getPort()+ " !");
//              return null;
//          }

        String implName = new String( Environment.implName());
        if( implName.length() > 0 && 
            !(implName.equals( org.jacorb.poa.util.POAUtil.extractImplName(d.getObjectKey())))
              )
        {
            Debug.output(3, "ORB.findPOA: impl_name mismatch");
            return null;
        }

        try
        {
            org.jacorb.poa.POA tmp_poa = (org.jacorb.poa.POA)rootpoa;       
            String poa_name = org.jacorb.poa.util.POAUtil.extractPOAName(d.getObjectKey());

            /** strip scoped poa name (first part of the object key before "::",
             *  will be empty for the root poa
             */

            java.util.StringTokenizer strtok = 
                new java.util.StringTokenizer( poa_name, org.jacorb.poa.POAConstants.OBJECT_KEY_SEPARATOR );

            String scopes[] = new String[strtok.countTokens()];

            for( int i = 0; strtok.hasMoreTokens(); scopes[i++] = strtok.nextToken() );

            for( int i = 0; i < scopes.length; i++)
            {
                if( scopes[i].equals(""))
                    break;
                
                /* the following is a  call to a method in the private
                   interface between the ORB  and the POA. It does the
                   necessary    synchronization    between   incoming,
                   potentially concurrent  requests to activate  a POA
                   using its  adapter activator. This  call will block
                   until  the correct  POA is  activated and  ready to
                   service    requests.    Thus,    concurrent   calls
                   originating  from a  single,  multi-threaded client
                   will be serialized  because the thread that accepts
                   incoming  requests  from   the  client  process  is
                   blocked.  Concurrent  calls from other destinations
                   are not  serialized unless they  involve activating
                   the same adapter.  */
                
                try
                {
                    tmp_poa = tmp_poa._getChildPOA( scopes[i] );
                }
                catch ( org.jacorb.poa.except.ParentIsHolding p )
                {
                    Debug.output(3, "ORB.findPOA: holding adapter");
                    return null;
                }           
            }

            if( ! tmp_poa.previouslyGeneratedObjectKey(d.getObjectKey()))
            {
                Debug.output(3, "ORB.findPOA: not a previously generated object key.");
                return null;
            }

            return tmp_poa;
        }
        catch( Exception e )
        {
            Debug.output( 2, e); // TODO
        }        

        Debug.output(3, "ORB.findPOA: nothing found");
        return null;
    }


    public ConnectionManager getConnectionManager()
    {
        return connectionManager;
    }
    

    //      void _release( org.omg.CORBA.Object o )
    //      {
    //          String key = ((org.jacorb.orb.Delegate)((org.omg.CORBA.portable.ObjectImpl)o)._get_delegate()).pior.getIORString() ;
    //          knownReferences.remove( key );
    //      }

    synchronized void _release( org.jacorb.orb.Delegate delegate )
    {
        knownReferences.remove( delegate.getParsedIOR().getIORString() );
    }


    /**
     * This method creates a policy  with the given type and the given
     * value.  The ORB relies on PolicyFactories.   to actually create
     * policy objects. New PolicyFactories can be registered using the
     * ORBInitializer mechanism.
     *
     * @param type The policies type.
     * @param value The policies value.
     * @exception org.omg.CORBA.PolicyError There is no PolicyFactory for the 
     * given type or the policy creation failed.  
     * @see org.omg.PortableInterceptor.PolicyFactory
     */

    public org.omg.CORBA.Policy create_policy( int type, org.omg.CORBA.Any value )
        throws org.omg.CORBA.PolicyError
    {
        Integer key = new Integer(type);

        if ( policy_factories == null || (! policy_factories.containsKey(key)) )
            throw new org.omg.CORBA.PolicyError();

        PolicyFactory factory = (PolicyFactory)policy_factories.get(key);    
        return factory.create_policy(type, value);
    }
    

    /**
     * Tests, if a policy factory is present for the given type.
     * This method is (right now) only called by the Info-Objects
     * of the portableInterceptor implementation to avoid running
     * into exceptions.
     */

    public boolean hasPolicyFactoryForType(int type)
    {
        return (policy_factories != null && 
                policy_factories.containsKey( new Integer(type)) );
    }

    public  org.omg.CORBA.ContextList create_context_list()
    {
        return null;
    }

    public  org.omg.CORBA.Environment create_environment()
    {
        return null;
    }

    public org.omg.CORBA.portable.OutputStream create_output_stream()
    {
        return new CDROutputStream(this);
    }

    /**
     * Called from within getReference above. 
     *
     * Generates IOR 1.0 and 1.1, 1.1 contains also 1.0 profile. Only 1.1 and 
     * greater profiles contain codeset information about this server. <br>
     * If present, IORInterceptors will be invoked.
     */

    protected org.omg.IOP.IOR createIOR( String repId, 
                                         byte[] key, 
                                         boolean _transient, 
                                         org.jacorb.poa.POA poa )
    {
        String address = Environment.getProperty( "jacorb.ior_proxy_host" );
        if( address == null )
        {
            //property not set
            address = basicAdapter.getAddress();
        }
        else
        {
            Debug.output( Debug.INFORMATION | Debug.ORB_MISC, 
                          "Using proxy host " + address +
                          " in IOR" );
        }

        String port_str = Environment.getProperty( "jacorb.ior_proxy_port" );
        int port = -1;
        
        if( port_str != null )
        {
            try
            {
                port = Integer.parseInt( port_str );
            }
            catch( NumberFormatException nfe )
            {
                throw new Error( "Unable to create int from string >>" +
                                 port_str + "<<. " +
                                 "(check property \"jacorb.ior_proxy_port\")" );
            }

            if( port < 0 )
            {
                throw new Error( "Negative port numbers are not allowed! " +
                                 "(check property \"jacorb.ior_proxy_port\")" );
            }
        }
        else
        {
            //property not set
            port = basicAdapter.getPort();
        }

        if( !_transient && Environment.useImR() )
        {
            try
            {
                if (imr == null)
                {
                    // we're first to consult ImR
                    imr = RegistrationHelper.narrow( 
                             resolve_initial_references("ImplementationRepository"));
                }

                if (imr_info == null)
                {
                    // we're first to get ImRInfo
                    imr_info = imr.get_imr_info();
                }
                
                // set host and port to ImR's values
                address = imr_info.host;
                port =  imr_info.port;
                Debug.output(2,"New persistent IOR created with ImR at " + 
                             address + ":" + port );
                
            }
            catch (Exception _e)
            {
                Debug.output(6, _e);
                // sth went wrong,  host and port values of this ORB as set above are use
            }
        }


        Vector components_iiop_profile = new Vector();
        Vector components_multi_profile = new Vector();

        //if IORInterceptors are present, invoke them
        if ((interceptor_manager != null) &&
            interceptor_manager.hasIORInterceptors())
        {
            IORInfoImpl info = 
                new IORInfoImpl(this, 
                                poa, 
                                components_iiop_profile,
                                components_multi_profile);
            try
            {
                interceptor_manager.getIORIterator().iterate(info);
            }
            catch (Exception e)
            {
                Debug.output(2, e);
            }
        }
        
        TaggedProfile[] tps = null;

        boolean useMulti = components_multi_profile.size() > 0;

        //find out GIOP minor version to use. Defaults to 2.
        int giop_minor = 2;
        String gm_str = 
            Environment.getProperty( "jacorb.giop_minor_version", "2" );
        try
        {
            giop_minor = Integer.parseInt( gm_str );
        }
        catch( NumberFormatException nfe )
        {
            throw new Error( "Unable to create int from string >>" +
                             gm_str + "<<. " +
                             "(check property \"jacorb.giop_minor_version\")" );
        }

        //all components for the profiles have to be present by now.
        switch( giop_minor )
        {
            case 2 : 
            { 
                //same as IIOP 1.1 
            }
            case 1: 
            {
                // create IIOP 1.1 profile  
                TaggedComponent[] components = 
                    new TaggedComponent[ components_iiop_profile.size() ];
                
                components_iiop_profile.copyInto( components );

                org.omg.IIOP.Version version =
                    new org.omg.IIOP.Version( (byte) 1, (byte) giop_minor );

                org.omg.IIOP.ProfileBody_1_1 pb1 = 
                    new org.omg.IIOP.ProfileBody_1_1( version, 
                                                      address, 
                                                      (short) port, 
                                                      key, 
                                                      components);
                
                // serialize the profile id 1, leave idx 0 for v.1.0 profile
                CDROutputStream profileDataStream = new CDROutputStream( this );
                profileDataStream.beginEncapsulatedArray();

                org.omg.IIOP.ProfileBody_1_1Helper.write( profileDataStream, 
                                                          pb1 );

                tps = new TaggedProfile[useMulti ? 3:2];
                

                byte[] data = profileDataStream.getBufferCopy();

                tps[1] = new TaggedProfile( TAG_INTERNET_IOP.value, data );
                // fall through
            }
            case 0: 
            {
                // create IIOP 1.0 profile                
                org.omg.IIOP.ProfileBody_1_0 pb0 =
                    new org.omg.IIOP.ProfileBody_1_0(
                        new org.omg.IIOP.Version( (byte) 1, (byte) 0 ),
                        address,
                        (short) port,
                        key );
            
                CDROutputStream profileDataStream = new CDROutputStream( this );
                profileDataStream.beginEncapsulatedArray();

                org.omg.IIOP.ProfileBody_1_0Helper.write( profileDataStream, 
                                                          pb0 );

                if(tps == null) 
                    tps = new TaggedProfile[useMulti ? 2:1];

                tps[0] = new TaggedProfile( TAG_INTERNET_IOP.value, 
                                            profileDataStream.getBufferCopy() );
            }      
        }

        // now optionally fill the last IOR profile with multicomponent
        if(useMulti)
        {
            TaggedComponent[] components = 
                new TaggedComponent[ components_multi_profile.size() ];
            
            components_multi_profile.copyInto( components );
            
            CDROutputStream profileDataStream = new CDROutputStream(this);
            profileDataStream.beginEncapsulatedArray();
            
            MultipleComponentProfileHelper.write( profileDataStream, 
                                                  components );
            
            tps[ tps.length - 1 ] = 
                new TaggedProfile( TAG_MULTIPLE_COMPONENTS.value,
                                   profileDataStream.getBufferCopy() );
        }


        IOR _ior = new IOR( repId, tps );

        return _ior;
    }

    /** 
     *  called by Delegate to retrieve an unproxyified, local IOR 
     */
    /*
    org.omg.IOP.IOR unproxyfy(org.omg.IOP.IOR proxy_ior)
    {
        return getConnectionManager().unproxyfy(proxy_ior);
    }
    */
    public org.omg.CORBA.Current get_current()
    {
        return current;
    }

    public  org.omg.CORBA.Context get_default_context()
    {
        return null;
    }

    public org.omg.CORBA.Request get_next_response()
    {
        return null;
    }

    /**
     * used from the POA
     * @returns the basic adapter used by this ORB instance
     */

    public org.jacorb.orb.BasicAdapter getBasicAdapter()
    {
        if( basicAdapter == null )
            throw new RuntimeException("Adapters not initialized, call POA.init() first");
        return basicAdapter;
    }


    /** 
     * getPOACurrent
     */

    public org.jacorb.poa.Current getPOACurrent()
    {
        if (poaCurrent == null)
        {
            poaCurrent = org.jacorb.poa.Current._Current_init();
        }
        return poaCurrent;
    }

    /** 
     * called by POA to create an IOR
     *
     * @param poa the calling POA
     * @param object_key
     * @param rep_id
     * @param _transient is the new reference transient or persistent
     * @return a new CORBA Object reference
     */

    public org.omg.CORBA.Object getReference( org.jacorb.poa.POA poa, 
                                              byte[] object_key, 
                                              String rep_id, 
                                              boolean _transient )
    {
        try 
        {
            if( rep_id == null )
                rep_id = "IDL:org.omg/CORBA/Object:1.0";

            org.omg.IOP.IOR ior = 
                createIOR( rep_id, object_key, _transient, poa );

            Delegate d = new Delegate( this, ior );
            return d.getReference( poa );
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public org.jacorb.poa.POA getRootPOA()
    {
        if (rootpoa == null) 
        {
            rootpoa = org.jacorb.poa.POA._POA_init(this);
            rootpoa._addPOAEventListener( this );
            basicAdapter = new BasicAdapter( this, rootpoa );
        }
        return rootpoa;
    }


    /** 
     * @returns - true if ORB is initialized by an applet and 
     * appligator use is switched on
     */

    public boolean isApplet()
    {
        return applet != null;
        //        return applet != null && Environment.useAppligator();
    }


    public Applet getApplet()
    {
        return applet;
    }

    public String[] list_initial_services()
    {
        Vector v = new Vector();

        for( Enumeration e = initial_references.keys(); e.hasMoreElements(); v.add( e.nextElement()))
            ;

        String [] initial_services = 
            new String[ services.length + v.size()];

        v.copyInto( initial_services );

        System.arraycopy( services, 0, initial_services, v.size(), services.length );
        

        return initial_services;
    }

    /** 
     * An operation from the POAListener interface. Whenever a new POA is
     * created, the ORB is notified. 
     */

    public void poaCreated(org.jacorb.poa.POA poa)
    {
        /* 
         * Add this orb as the child poa's event listener. This means that the
         * ORB is always a listener to all poa events!
         */
        poa._addPOAEventListener(this);


        /* If the new POA has a persistent lifetime policy, it is registered
         * with the implementation repository if there is one and the 
         * use_imr policy is set via the "jacorb.orb.use_imr" property
         */

        if( poa.isPersistent())
        {
            persistentPOACount++;

            /* Lookup the implementation repository */

            if (imr == null && Environment.useImR() )
            {
                try
                {
                    imr = RegistrationHelper.narrow( 
                        resolve_initial_references("ImplementationRepository")  );
                }
                catch( org.omg.CORBA.ORBPackage.InvalidName in )
                {}
                if (imr == null || imr._non_existent())
                {
                    Debug.output(3, "No connection to ImplementationRepository");
                    return;
                }
            }

            if( imr != null )
            {
                try
                {
                    /* Register the POA  */
                    String server_name = new String(Environment.implName());
                    imr.register_poa(server_name + "/" + poa._getQualifiedName(), 
                                     server_name, // logical server name
                                     basicAdapter.getAddress(),
                                     basicAdapter.getPort());
                }
                catch( org.jacorb.imr.RegistrationPackage.DuplicatePOAName e )
                {
                    throw new org.omg.CORBA.INTERNAL( "A server with the same combination of ImplName/POA-Name (" +
                                                      new String(Environment.implName()) + '/' +
                                                      poa._getQualifiedName() +
                                                      ") is already registered and listed as active at the imr!" );
                }
                catch( org.jacorb.imr.RegistrationPackage.IllegalPOAName e )
                {
                    throw new org.omg.CORBA.INTERNAL( "The ImR replied that the POA name >>" + e.name + "<< is illegal!" );
                }
                catch( org.jacorb.imr.UnknownServerName e )
                {
                    throw new org.omg.CORBA.INTERNAL( "The ImR replied that the server name >>" + e.name + "<< is unknown!" );
                }
                catch (Exception _e)
                {
                    Debug.output(4, _e);
                }
            }
        }
    }



    public void poaStateChanged(org.jacorb.poa.POA poa, int new_state)
    {
        if( ( new_state == org.jacorb.poa.POAConstants.DESTROYED ||
              new_state == org.jacorb.poa.POAConstants.INACTIVE )  && 
            poa.isPersistent() && imr != null
            )
        {
            /* if all persistent POAs in this server have gone down, unregister
               the server */
            if( --persistentPOACount == 0 )
            {
                try
                {
                    imr.set_server_down(new String(Environment.implName()));
                }
                catch (Exception _e)
                {
                    Debug.output(2, _e);
                }
            }
        }
    }

    public  boolean poll_next_response()
    {
        return false;
    }


    public void referenceCreated(org.omg.CORBA.Object o)
    {}

    public boolean get_service_information( short service_type,
                                            org.omg.CORBA.ServiceInformationHolder service_information) 
    {
        //          if (( service_type == org.omg.CORBA.Security.value ) && Environment.supportSSL ())
        //          {
        //              byte options[] = new byte [5]; // ServiceOption[]
        //              options[0] = (byte)org.omg.Security.SecurityLevel1.value;
        //              options[1] = (byte)org.omg.Security.SecurityLevel2.value;
        //              options[2] = (byte)org.omg.Security.ReplaceORBServices.value;
        //              options[3] = (byte)org.omg.Security.ReplaceSecurityServices.value;
        //              options[4] = (byte)org.omg.Security.CommonInteroperabilityLevel0.value;
        //              org.omg.CORBA.ServiceDetail details[] = new org.omg.CORBA.ServiceDetail [2];
        //              details[0].service_detail_type = org.omg.Security.SecureTransportType.value;
        //              details[0].service_detail = org.jacorb.security.ssl.SSLSetup.getMechanismType().getBytes();
        //              details[1].service_detail_type = org.omg.Security.SecureTransportType.value;
        //              details[1].service_detail = "001010011".getBytes(); // AuditId, _PublicId, AccessId
        //              return true;
        //          }
        //          else return false;
        return false;
    }
    
    /**
     * resolve_initial_references
     */
    public org.omg.CORBA.Object resolve_initial_references(String identifier) 
        throws org.omg.CORBA.ORBPackage.InvalidName 
    {
        if ( initial_references.containsKey(identifier) )
        {
            return (org.omg.CORBA.Object)initial_references.get(identifier);
        }
        else
        {
            org.omg.CORBA.Object obj = null;
            String url = Environment.getProperty("ORBInitRef." + identifier);

            if( url != null )
            {
		try
		{
		    obj = this.string_to_object( url );
		}
		catch( Exception e )
		{
		    Debug.output( 1, "ERROR: Could not create initial reference for \"" +
				  identifier + '\"' );
		    Debug.output( 1, "Please check property \"ORBInitRef." + 
				  identifier + '\"' );

		    Debug.output( 3, e );

		    throw new org.omg.CORBA.ORBPackage.InvalidName();
		}
            }
            /* "special" behavior follows */
            else if( identifier.equals("NameService") && isApplet() )
            {
                // try to get location of URL with ns's IOR from a file
                //     called NameService.ior at the applet's host
                String ior_str = 
                    org.jacorb.util.ObjectUtil.readURL("http://"
                                                       + applet.getCodeBase().getHost()
                                                       + "/"
                                                       + "NameService.ior");
                
                obj = this.string_to_object(ior_str);

                if( ! obj._is_a( org.omg.CosNaming.NamingContextHelper.id()))
                    obj = null;
            }
            else if( identifier.equals("RootPOA") )
            {
                return (org.omg.CORBA.Object)getRootPOA();
            }
            else if( identifier.equals("POACurrent") )
            {
                return (org.omg.CORBA.Object)getPOACurrent();
            }
            else if( identifier.equals("LocalDomainService") )
            { 
                if ( ! Environment.useDomain() ) 
                    throw new org.omg.CORBA.ORBPackage.InvalidName(
                         "domain service is not configured for usage."
                         +" Set property \"jacorb.use_domain\" to \"on\".");
          
                // make  a  new local,  orb-specific
                // instance of a domain called "orb domain"

                org.jacorb.orb.domain.ORBDomain orb_domain= null;

                // recursion  end: at  orb  domain  creation the  orb
                // domain itself wants to be mapped by the poa to some
                // domain(s).   The   poa   calls   this   part   of
                // resolve_initial_references again  to obtain the orb
                // domain. The  dilemma is,  that the  orb  domain is
                // going  to be created  and the reference to  it will
                // not   be   available   until   poa.servant_to_ref
                // returns. The chosen solution to this problem is to
                // return a  null  refence on  the  second call.  The
                // method POA.doInitialMapping() knows how to handle this.

                if( ORBDomainCreationInProgress ) 
                    throw new org.omg.CORBA.ORBPackage.InvalidName("second call");

                ORBDomainCreationInProgress = true;
                try 
                {               
                    // policy factory
                    org.jacorb.orb.domain.PolicyFactoryImpl polFactoryImpl =
                        new org.jacorb.orb.domain.PolicyFactoryImpl();
                    try 
                    { 
                        getRootPOA().the_POAManager().activate(); 
                    } 
                    catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive e) 
                    { 
                        Debug.output(1, e); 
                    }

                    org.jacorb.orb.domain.PolicyFactory polFactory =
                        org.jacorb.orb.domain.PolicyFactoryHelper.narrow(
                             getRootPOA().servant_to_reference( polFactoryImpl ));

                    // create policies by the help of the policy factory
                    org.omg.CORBA.Policy policies[] = new org.omg.CORBA.Policy[1];
                    policies[0]= 
                        polFactory.createConflictResolutionPolicy(
                            org.jacorb.orb.domain.ConflictResolutionPolicy.PARENT_RULES);
              
                    // create domain with the above policies
                    org.jacorb.orb.domain.ORBDomainImpl impl = 
                        new org.jacorb.orb.domain.ORBDomainImpl( null, policies, "orb domain");
                    
                    org.jacorb.orb.domain.ORBDomainPOATie tie = 
                        new org.jacorb.orb.domain.ORBDomainPOATie( impl );
                    impl.setTie(tie);

                    orb_domain = 
                        org.jacorb.orb.domain.ORBDomainHelper.narrow(
                             getRootPOA().servant_to_reference( tie ));

                    obj = orb_domain;
                    ORBDomainCreationInProgress = false;
                    //                    orb_domain.insertLocalDomain( orb_domain );
                } 
                catch( org.omg.PortableServer.POAPackage.WrongPolicy wp ) 
                {
                    Debug.output(1, "the root poa of this orb has the wrong"
                                 +" policies for \"servant_to_reference\".");
                }
                catch( org.omg.PortableServer.POAPackage.ServantNotActive na ) 
                {
                    Debug.output(1, na);
                }

                if( Environment.mountORBDomain() )
                { 
                    initial_references.put( identifier, obj ); // to avoid recursive calls
                    this.mountORBDomain();
                }

                String filename = Environment.ORBDomainFilename();

                if ( filename!= null && !filename.equals("") )
                {
                    Debug.output(Debug.DOMAIN | Debug.INFORMATION, 
                                 "writing IOR of local domain service to file "+ filename);
                    try 
                    {
                        FileOutputStream out = new FileOutputStream( filename );
                        PrintWriter pw = new PrintWriter( out );
                        pw.println(this.object_to_string( orb_domain ));
                        pw.flush();
                        out.close();
                    }
                    catch (IOException e) 
                    {
                        Debug.output(1, "Unable to write IOR to file " + filename);
                    }
                }
            }
            else if( identifier.equals("SecurityCurrent") ) 
            {
                if( securityCurrent == null )
                {
                    try
                    {
                        Class ssl = Class.forName( "org.jacorb.security.level2.CurrentImpl" );

                        Constructor constr = ssl.getConstructor( new Class[]{
                            org.omg.CORBA.ORB.class });

                        securityCurrent = (org.omg.SecurityLevel2.Current)
                            constr.newInstance( new Object[]{ this });

                        Method init = ssl.getDeclaredMethod( "init",
                                                             new Class[0] );
                        init.invoke( securityCurrent, new Object[0] );
                    }
                    catch (Exception e)
                    {
                        Debug.output( Debug.IMPORTANT | Debug.ORB_MISC,
                                      e );
                    }
                }

                obj = securityCurrent;
            }
            else if( identifier.equals("DynAnyFactory") )
            {
                obj = new org.jacorb.orb.dynany.DynAnyFactoryImpl( this );
            }
            else if( identifier.equals("PICurrent") )
            {
                if (interceptor_manager == null)
                    return InterceptorManager.EMPTY_CURRENT;
                else
                    return interceptor_manager.getCurrent();
            }
            else if( identifier.equals("CodecFactory") )
            {
                obj = new CodecFactoryImpl(this);
            }
//              else if( identifier.equals("TransactionCurrent") )
//                  obj = new org.jacorb.transaction.TransactionCurrentImpl();
            else
                throw new org.omg.CORBA.ORBPackage.InvalidName();
    
            if (obj != null)
            {
                initial_references.put(identifier, obj);
                return obj;
            }
            else
                return null;
        }
    }

//      private void initialReferencesInit()
//      {
//          Hashtable props = Environment.getProperties( "ORBInitRef." );
            
//          for( Enumeration names = props.keys(); names.hasMoreElements(); )
//          {
//              String name = (String) names.nextElement();
//              String key = name.substring( name.indexOf('.')+1);
//              try
//              {
//                  register_initial_reference( key, 
//                             string_to_object( (String) props.get( name )));

//                  Debug.output(3, "ORBInitRef " + key + " string: " + 
//                               (String) props.get( name ));
//              }
//              catch( Exception e )
//              {
//                  Debug.output( 1, "Unable to create initial reference from " +
//                                name + "=" + props.get( name ) );
//                  Debug.output( 3, e );
//              }
//          }                
//      }


    /**
     * Register a reference, that will be returned on subsequent calls
     * to resove_initial_references(id). <br>
     * The references "RootPOA", "POACurrent" and "PICurrent" can be set,
     * but will not be resolved with the passed in references.
     *
     * @param id The references human-readable id, e.g. "MyService".
     * @param obj The objects reference.
     * @exception InvalidName A reference with id has already been registered.
     */

    public void register_initial_reference( String id, org.omg.CORBA.Object obj ) 
        throws InvalidName 
    {
        if (id == null || id.length() == 0 || 
            initial_references.containsKey(id) )
        {
            throw new InvalidName();
        }
        else
        {
            Debug.output( 4, "Registering initial ref " + id );
            initial_references.put(id, obj);
        }
    }

    public void run() 
    {   
        Debug.output(4,"ORB run");
        try 
        {
            synchronized( orb_synch )
            {
                while( run  )
                {
                    orb_synch.wait();
                }
            }
        } 
        catch( java.lang.InterruptedException iex )
        {
            iex.printStackTrace();
        }                 
        Debug.output(4,"ORB run, exit");
    }

    public  void send_multiple_requests_deferred(org.omg.CORBA.Request[] req)
    {
    }

    public  void send_multiple_requests_oneway(org.omg.CORBA.Request[] req)
    {
    }

    /**
     * called from ORB.init()
     */

    protected void set_parameters( String[] args, java.util.Properties props )
    {      
        if( props != null )
        {
            _props = props;
            Environment.addProperties( props );
        }

        /*
         * find -ORBInitRef args and add them to Environment
         * (overwriting existing props).  
         */

        if( args != null )
        {
            _args = args;
            for( int i = 0; i < args.length; i++ )
            {
                if( args[ i ].startsWith( "-ORBInitRef" ))
                {
                    //get rid of the leading `-'
                    String prop = args[ i ].substring( 1 );
                    
                    //find the equals char that separates prop name from
                    //prop value
                    int equals_pos = prop.indexOf( '=' );
                    
                    //add the property to environment
                    Environment.setProperty( prop.substring( 0, equals_pos ),
                                             prop.substring( equals_pos + 1) );
                }
            }
        }

	connectionManager = new ConnectionManager(this);
	
        String s = Environment.getProperty( "jacorb.hashtable_class" );
        if( s == null || s.length() == 0 )
        {
            Debug.output( Debug.INFORMATION | Debug.ORB_MISC, 
                          "Property \"jacorb.hashtable_class\" not present. Will use default hashtable implementation" );
            knownReferences = new Hashtable();  

        }
        else
        {
            try
            {
                knownReferences = (Hashtable) Class.forName( s ).newInstance();
            }
            catch( Exception e )
            {
                Debug.output( Debug.INFORMATION | Debug.ORB_MISC, e );
                knownReferences = new Hashtable();
            }
        }

        String versionProperty = 
            Environment.getProperty("jacorb.orb.print_version");

        if( versionProperty != null &&
            versionProperty.equals("on") )
        {
            System.out.println("\tJacORB V " + versionString + 
                               ", www.jacorb.org");
            System.out.println("\t(C) Gerald Brose, FU Berlin, " + 
                               dateString );
        }

        Hashtable initrefs = Environment.getProperties("ORBInitRef");

        for( Enumeration e = initrefs.keys(); e.hasMoreElements(); )
        {
            String key = (String)e.nextElement();
            Object obj = string_to_object( (String)initrefs.get( key ));
            if( obj != null )
                initial_references.put( key.substring( key.indexOf('.')+1), obj);
        }

        interceptorInit();
    }


    protected void set_parameters( java.applet.Applet app, 
				   java.util.Properties  props )
    {
        applet = app;
        _props = props;

        Environment.addProperties( props );

	connectionManager = new ConnectionManager(this);
	
        String s = Environment.getProperty( "jacorb.hashtable_class" );
        if( s == null || s.length() == 0 )
        {
            Debug.output( Debug.INFORMATION | Debug.ORB_MISC, 
                          "Property \"jacorb.hashtable_class\" not present. Will use default hashtable implementation" );
            knownReferences = new Hashtable();  

        }
        else
        {
            try
            {
                knownReferences = (Hashtable) Class.forName( s ).newInstance();
            }
            catch( Exception e )
            {
                Debug.output( Debug.INFORMATION | Debug.ORB_MISC, e );
                knownReferences = new Hashtable();
            }
        }


        // unproxyTable = new Hashtable();

        interceptorInit();
    }

    /**
     * This method retrieves the ORBInitializer-Names from the Environment,
     * and runs them.
     */ 

    private void interceptorInit()
    {
        // get instances from Environment
        Vector orb_initializers = Environment.getORBInitializers();

        if (orb_initializers.size() > 0)
        {
            ORBInitInfoImpl info = new ORBInitInfoImpl(this);

            // call pre_init in ORBInitializers
            for (int i = 0; i < orb_initializers.size(); i++)
            {
                try
                {
                    ORBInitializer init = (ORBInitializer) orb_initializers.elementAt(i);
                    init.pre_init(info);
                }
                catch (Exception e)
                {
                    Debug.output(0, e);
                }
            }
     
            //call post_init on ORBInitializers
            for (int i = 0; i < orb_initializers.size(); i++)
            {
                try
                {
                    ORBInitializer init = (ORBInitializer) orb_initializers.elementAt(i);
                    init.post_init(info);
                }
                catch (Exception e){
                    Debug.output(0, e);
                } 
            }

            //allow no more access to ORBInitInfo from ORBInitializers
            info.setInvalid();

            Vector client_interceptors = info.getClientInterceptors();
            Vector server_interceptors = info.getServerInterceptors();
            Vector ior_intercept = info.getIORInterceptors();

            if ((server_interceptors.size() > 0) || 
                (client_interceptors.size() > 0) ||
                (ior_intercept.size() > 0))
            {
                interceptor_manager = new InterceptorManager(client_interceptors,
                                                             server_interceptors,
                                                             ior_intercept,
                                                             info.getSlotCount(),
                                                             this);

                /**
                 * reinitialize cached references. This is needed
                 * since references that have been created in an
                 * ORBInitializer will have no interceptors called 
                 * on invocation.
                 */
                Enumeration e = knownReferences.elements();

                while(e.hasMoreElements())
                {
                    Reference r = (Reference) e.nextElement();
                    if( r != null )
                        ((Delegate) r._get_delegate()).initInterceptors();
                }
            }
      
            // add PolicyFactories to ORB
            policy_factories = info.getPolicyFactories(); 

            /*
            // add policy factories that are always present
            try
            {
                PolicyFactory factory = (PolicyFactory)new BiDirPolicyFactoryImpl( this );

                policy_factories.put( 
                   new Integer(org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE.value),
                   factory );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
            */
        }
    }

    public void shutdown( boolean wait_for_completion ) 
    {    
        Debug.output(2,"prepare ORB for going down...");

        if( ! run )
        {
            return; // ORB already shut down...
        }

        synchronized( shutdown_synch )
        {
            Debug.output(2,"ORB going down...");
            if( shutdown_in_progress && wait_for_completion )
            {
                synchronized( shutdown_synch )
                {
                    try
                    {
                        shutdown_synch.wait();
                    }
                    catch( InterruptedException ie )
                    {}
                    Debug.output(2,"ORB shutdown complete (1)");
                    return;
                }
            }   
            else if( shutdown_in_progress && !wait_for_completion )
            {
                Debug.output(2,"ORB shutdown complete (2)");
                return;
            }
            
            shutdown_in_progress = true;
        }

        if( rootpoa != null )
            rootpoa.destroy(true, wait_for_completion); 

        if( basicAdapter != null )
        {
            basicAdapter.stopListeners();
        }

        Debug.output(3,"ORB shutdown (cleaning up ORB...)");

        connectionManager.shutdown();
        knownReferences.clear();
        BufferManager.getInstance().release();

        Debug.output(3,"ORB shutdown (all tables cleared)");

        /* notify all threads waiting for shutdown to complete */

        synchronized( shutdown_synch )
        {
            shutdown_synch.notifyAll();
        }
        Debug.output(3,"ORB shutdown (threads notified)");

        /* notify all threads waiting in orb.run() */
        synchronized( orb_synch )
        {              
            run = false;
            orb_synch.notifyAll();
        }
        Debug.output(2,"ORB shutdown complete");
    }

    public void destroy()
    {
        if( destroyed )
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();

        if( run )
        {
            shutdown( true );
        }
        
        // other clean up possible here ?
        destroyed = true;
    }

    public org.omg.CORBA.Object string_to_object(String str) 
    {
        if( str == null )
            return null;

        try
        {
            ParsedIOR pior = new ParsedIOR( str, this );
            if( pior.isNull() )
            {
                return null;
            }
            else
            {
                return _getObject(pior);
            }
        }
        catch( IllegalArgumentException iae )
        {
            Debug.output( 5, iae );
            return null;
        }


    }

    /**
     * called by org.jacorb.poa.RequestProcessor
     */

    public void set_delegate( java.lang.Object wrapper ) 
    {
        if( ! (wrapper instanceof org.omg.PortableServer.Servant) )
            throw new org.omg.CORBA.BAD_PARAM("Argument must be of type org.omg.PortableServer.Servant");
        else
        {
            try
            {
                ((org.omg.PortableServer.Servant)wrapper)._get_delegate();
            }
            catch( org.omg.CORBA.BAD_INV_ORDER bio )
            {
                // only set the delegate if it has not been set already                
                org.jacorb.orb.ServantDelegate delegate = 
                    new org.jacorb.orb.ServantDelegate( this );
                ((org.omg.PortableServer.Servant)wrapper)._set_delegate(delegate);
            }
        }
    }

    /** 
     * forces the use of a proxy (by using a dummy applet)
     */

    public void useProxy()
    {
        applet = new java.applet.Applet();
    }

    public String object_to_string( org.omg.CORBA.Object obj)
    {
        Object delegate = 
            ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate();
        if (delegate instanceof org.jacorb.orb.Delegate)
            return delegate.toString();
        else
            throw new Error("Argument has a delegate whose class is "
                            + delegate.getClass().getName()
                            + ", a org.jacorb.orb.Delegate was expected");
    }

    public void perform_work() 
    {     
    }

    public boolean work_pending() 
    {     
        return false;
    }

    public ValueFactory register_value_factory (String id, 
                                                ValueFactory factory)
    {
        return (ValueFactory)valueFactories.put (id, factory);
    }

    public void unregister_value_factory (String id)
    {
        valueFactories.remove (id);
    }

    public ValueFactory lookup_value_factory (String id) 
    {
        ValueFactory result = (ValueFactory)valueFactories.get (id);
        if (result == null)
        {
            if (id.startsWith ("IDL")) 
            {
                String valueName = org.jacorb.ir.RepositoryID.className (id);
                result = findValueFactory (valueName);
                valueFactories.put (id, result);
            }
        }
        return result;
    }

    /**
     * Finds a ValueFactory for class valueName by trying standard class names.
     */
    private ValueFactory findValueFactory (String valueName)
    {
        Class result = null;
        result = findClass (valueName + "DefaultFactory", true);
        if (result != null)
            return (ValueFactory)instantiate (result);
        else
        {
            // Extension of the standard: Handle the common case
            // when the Impl class is its own factory...
            Class c = findClass (valueName, false);
            result  = findClass (valueName + "Impl", false);
            if (result != null && c.isAssignableFrom (result))
                if (ValueFactory.class.isAssignableFrom (result))
                    return (ValueFactory)instantiate (result);
                else
                {
                    // ...or create a factory on the fly
                    final Class impl = result;
                    return new ValueFactory() {
                        public java.io.Serializable read_value
                        (org.omg.CORBA_2_3.portable.InputStream is)
                        {
                            StreamableValue result = 
                                (StreamableValue)instantiate (impl);
                            result._read (is);
                            return result;
                        }
                    };
                }
            else
                return null;
        }
    }

    /**
     * Returns the class object for `name', if it exists, otherwise
     * returns null.  If `orgomg' is true, and `name' starts with "org.omg",
     * do a double-take using "omg.org" as the prefix.
     */
    private Class findClass (String name, boolean orgomg)
    {
        Class result = null;
        try
        {
            result = Thread.currentThread().getContextClassLoader()
                                           .loadClass (name);
        } 
        catch (ClassNotFoundException e) 
        {
            if (orgomg && name.startsWith ("org.omg"))
                try
                {
                    result = Thread.currentThread().getContextClassLoader()
                                   .loadClass ("omg.org" + name.substring(7));
                } 
                catch (ClassNotFoundException x)
                {
                    // nothing, result is null
                }
        }
        return result;
    }

    /**
     * Instantiates class `c' using its no-arg constructor.  Throws a
     * run-time exception if that fails.
     */
    private Object instantiate (Class c)
    {
        try
        {
            return c.newInstance();
        }
        catch (IllegalAccessException e1)
        {
            throw new RuntimeException ("cannot instantiate class " 
                                        + c.getName()
                                        + " (IllegalAccessException)");
        }
        catch (InstantiationException e2)
        {
            throw new RuntimeException ("cannot instantiate class "
                                        + c.getName()
                                        + " (InstantiationException)");
        }
    }

    /**
     * Test, if the ORB has ClientRequestInterceptors <br>
     * Called by Delegate.
     */

    public boolean hasClientRequestInterceptors()
    {
        return (interceptor_manager != null) &&
            interceptor_manager.hasClientRequestInterceptors();
    }

    /**
     * Test, if the ORB has ServerRequestInterceptors <br>
     * Called by poa.RequestProcessor.
     */

    public boolean hasServerRequestInterceptors()
    {
        return (interceptor_manager != null) &&
            interceptor_manager.hasServerRequestInterceptors();
    }
  
    /**
     * Get the InterceptorManager, if present.
     *
     * @return the InterceptorManager, or null, if none is present.
     */

    public org.jacorb.orb.portableInterceptor.InterceptorManager getInterceptorManager()
    {
        return interceptor_manager;
    }

   
    public void mountORBDomain()
    {
        mountORBDomain("orb domain");
    }

    /** inserts the orb domain into the domain server as child domain. 
     *  The orb domain is the domain  returned by 
     *  this.resolve_initial_references("LocalDomainService"). The domain server is the domain 
     *  returned by this.resolve_initial_references("DomainService").
     *  @param orbDomainNameif not null and not empty the orb domain name is set
     *                              to this value
     */

    public void mountORBDomain(String orbDomainName)
    {
        org.jacorb.orb.domain.Domain domainServer= null;
        org.jacorb.orb.domain.Domain orb_domain  = null;

        try 
        {   
            domainServer= org.jacorb.orb.domain.DomainHelper.narrow
                (resolve_initial_references("DomainService"));
        
            orb_domain= org.jacorb.orb.domain.DomainHelper.narrow
                (resolve_initial_references("LocalDomainService"));

            // set name of orb domain
            if (orbDomainName == null || orbDomainName.equals(""))
                orbDomainName= "orb domain";

            if ( domainServer.hasChild(orb_domain) )
            { 
                // do not insert if already a child of domain server
                // just change name
                Debug.output(2, "ORB.mountORBDomain: rename from \"" + orb_domain.name() 
                             +"\" to \"" + orbDomainName +"\".");
                domainServer.renameChildDomain(orb_domain.name(), orbDomainName);
                orb_domain.name(orbDomainName);
                return;
            }
            // else first insert: set name of orb domain
            orb_domain.name(orbDomainName);

            
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName inv) 
        {
            Debug.output(1, inv);
        }
        catch (org.jacorb.orb.domain.NameAlreadyDefined def)
        { 
            return; 
        }
        catch (org.jacorb.orb.domain.InvalidName invalid)
        { 
            return; 
        }
        
        int tries= 0;
        while (true) // try until suceeded
        {
            try 
            { 
                domainServer.insertChild(orb_domain);
                return;
            } 
            catch (org.jacorb.orb.domain.GraphNodePackage.ClosesCycle cc) 
            {
                Debug.output(1, cc);
                return;
            }
            catch (org.jacorb.orb.domain.NameAlreadyDefined already)
            {
                Debug.output(1, "ORB.mountORBDomain: name "
                             + already.name +  " already used in domain server scope");
                tries++;
                orb_domain.name(orbDomainName + "#"+ Integer.toString(tries) );
            }
        }
    }

}



