package org.jacorb.imr;

/**
 * ImRAccessImpl.java
 *
 *
 * Created: Thu Jan 31 21:05:55 2002
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

import org.jacorb.imr.RegistrationPackage.*;
import org.jacorb.util.*;

import org.omg.CORBA.INTERNAL;

public class ImRAccessImpl 
    implements org.jacorb.orb.ImRAccess
{
    private Registration reg = null;
    private ImRInfo info = null;

    public ImRAccessImpl ()
    {
        
    }

    public void connect( org.omg.CORBA.ORB orb )
        throws INTERNAL
    {
        try
        {
            reg = RegistrationHelper.narrow( orb.resolve_initial_references("ImplementationRepository")  );
        }
        catch( org.omg.CORBA.ORBPackage.InvalidName in )
        {            
        }

        if (reg == null || reg._non_existent())
        {
            Debug.output(1, "ERROR: No connection to ImplementationRepository");

            throw new INTERNAL( "Unable to resolve reference to ImR" );
        }
    }

    public String getImRHost()
    {
        if( info == null )
        {
            info = reg.get_imr_info();
        }

        return info.host;
    }

    public int getImRPort()
    {
        if( info == null )
        {
            info = reg.get_imr_info();
        }

        return info.port;
    }

    public void registerPOA( String name, 
                             String server,
                             String host, 
                             int port)
        throws INTERNAL
    {
        try
        {
            reg.register_poa(name, server, host, port );
        }
        catch( DuplicatePOAName e )
        {
            throw new INTERNAL( "A server with the same combination of ImplName/POA-Name (" +
                                name  +
                                ") is already registered and listed as active at the imr!" );
        }
        catch( IllegalPOAName e )
        {
            throw new INTERNAL( "The ImR replied that the POA name >>" + 
                                e.name + "<< is illegal!" );
        }
        catch( UnknownServerName e )
        {
            throw new INTERNAL( "The ImR replied that the server name >>" + 
                                e.name + "<< is unknown!" );
        }
    }

    public void setServerDown( String name )
        throws INTERNAL
    {
        try
        {
            reg.set_server_down( name );
        }
        catch(UnknownServerName e)
        {            
            throw new INTERNAL( "The ImR replied that a server with name " +
                                name + " is unknown" );
        }
    }
}// ImRAccessImpl
