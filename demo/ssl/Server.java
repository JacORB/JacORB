package demo.ssl;

import java.io.*;

import java.security.cert.X509Certificate;

import org.omg.PortableServer.POA;
import org.omg.SecurityLevel2.*;
import org.omg.Security.*;
import org.omg.CORBA.ORB;

import org.jacorb.security.level2.*;

/**
 * This is the server part of the ssl demo. It demonstrates 
 * how to get access to the certificates that the client sent 
 * for mutual authentication. The certificate chain can be 
 * accessed via the Security Level 2 interfaces.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Server 
    extends SSLDemoPOA 
{
    //the Security Level 2 Current
    private Current current = null;

    /*
     * This class from package org.jacorb.security.level2
     * contains the actual contents of the security attributes
     */
    private SecAttributeManager attrib_mgr = null;

    //the single attribute type array, that is used 
    //for getting the SecAttributes from the Credentials
    private AttributeType[] access_id = null;
    

    public Server( Current current ) 
    {
        this.current = current;

        attrib_mgr = SecAttributeManager.getInstance();
        
        AttributeType attribute_type = 
            new AttributeType(new ExtensibleFamily((short) 0,
                                                   (short) 1),
                              AccessId.value);
        
        access_id = new AttributeType[] {attribute_type};        
    }

    /**
     * This method retrievs the received client certificate 
     * from the Credentials.
     */
    private X509Certificate getClientCert()
    {
        //get the ReceivedCredentials
        ReceivedCredentials creds = current.received_credentials();
        
        if (creds == null)
        {            
            return null;
        }
        
        //get the SecAttributes we're interested in
        SecAttribute[] attribs = creds.get_attributes( access_id );

        if( attribs.length == 0 )
        {
            return null;
        }

        //get the actual contents of the SecAttributes via
        //the SecAttributeManager
        KeyAndCert kac = attrib_mgr.getAttributeCertValue( attribs[0] );

        if( kac == null )
        {
            return null;
        }
 
        //return the first (self-signed) certificate of the chain
        return (X509Certificate) kac.chain[0];
    }


    /**
     * This method is from the IDL--interface. It prints out the 
     * received client cert (if available).
     */
    public void printCert()
    {
        X509Certificate client_cert = getClientCert();
        
        if( client_cert == null )
        {
            System.out.println( "No client certificate available" );
        }
        else
        {
            System.out.println( "Received a client certificate:" );
            System.out.println( client_cert );            
        }            
    }

    public static void main( String[] args )
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: java demo.ssl.Server <ior_file>" );
            System.exit( -1 );
        }

        try
        {
            ORB orb = ORB.init( args, null );
            
            POA poa = (POA) 
                orb.resolve_initial_references( "RootPOA" );

            poa.the_POAManager().activate();

            Current current = (org.omg.SecurityLevel2.Current)
                orb.resolve_initial_references( "SecurityCurrent" );

            org.omg.CORBA.Object demo = 
                poa.servant_to_reference( new Server( current ));

            PrintWriter pw = 
                new PrintWriter( new FileWriter( args[ 0 ] ));

            // print stringified object reference to file
            pw.println( orb.object_to_string( demo ));
            
            pw.flush();
            pw.close();
    
            // wait for requests
	    orb.run();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }            
    }
} // Server
