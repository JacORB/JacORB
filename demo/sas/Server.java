package demo.sas;

import java.io.*;

import java.security.cert.X509Certificate;

import org.omg.PortableServer.POA;
import org.omg.SecurityLevel2.*;
import org.omg.Security.*;
import org.omg.CORBA.ORB;
import org.ietf.jgss.*;
import org.jacorb.util.*;

import org.jacorb.security.level2.*;

/**
 * This is the server part of the sas demo. It demonstrates
 * how to get access to the certificates that the client sent
 * for mutual authentication. The certificate chain can be
 * accessed via the Security Level 2 interfaces.
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Server
    extends SASDemoPOA
{
    //the Security Level 2 Current
    //private Current current = null;

    /*
     * This class from package org.jacorb.security.level2
     * contains the actual contents of the security attributes
     */
    private SecAttributeManager attrib_mgr = null;

    //the single attribute type array, that is used
    //for getting the SecAttributes from the Credentials
    private AttributeType[] access_id = null;


    public Server( /*Current current*/ )
    {
        //this.current = current;

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
        ReceivedCredentials creds = null;//current.received_credentials();

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
    public void printSAS()
    {
        System.out.println("printSAS");
        //X509Certificate client_cert = getClientCert();

        //if( client_cert == null )
        //{
        //    System.out.println( "No client certificate available" );
        //}
        //else
        //{
        //    System.out.println( "Received a client certificate:" );
        //    System.out.println( client_cert );
        //}
    }

    public static void main( String[] args )
    {
        if( args.length != 1 )
	{
            System.out.println( "Usage: java demo.sas.Server <ior_file>" );
            System.exit( -1 );
        }

        try
        {
            ORB orb = ORB.init( args, null );

            try {
                java.security.Provider p = new org.jacorb.security.sas.GSSUPProvider();
                Oid mechOid = new Oid(org.omg.GSSUP.GSSUPMechOID.value.replaceFirst("oid:",""));
                GSSManager gssManager = GSSManager.getInstance();
                // load any mechanism providors
                for (int i = 1; i <= 16; i++) {
                    String mechOID = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism."+i+".oid");
                    if (mechOID == null) continue;
                    String mechProvider = org.jacorb.util.Environment.getProperty("jacorb.security.sas.mechanism."+i+".provider");
                    if (mechProvider == null) continue;
                    try {
                        org.ietf.jgss.Oid oid = new org.ietf.jgss.Oid(mechOID);
                        Class cls = Class.forName (mechProvider);
                        java.lang.Object provider = cls.newInstance ();
                        gssManager.addProviderAtFront((java.security.Provider)provider, oid);
                        Debug.output(1, "Adding GSS SPI Provider: " + oid + " " + mechProvider);
                    } catch (Exception e) {
                        Debug.output( 1, "GSSProvider "+mechOID+" "+mechProvider + " error: " +e );
                    }
                }
                org.jacorb.security.sas.GSSUPProvider.setORB(orb);
                org.jacorb.orb.standardInterceptors.SASComponentInterceptor.setGSSManager(gssManager);
                org.jacorb.security.sas.TSSInvocationInterceptor.setGSSManager(gssManager);
            } catch (GSSException e) {
                System.out.println("GSSException "+e.getMessage()+": "+e.getMajorString()+": "+e.getMinorString());
                e.printStackTrace();
                System.exit(1);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

            POA poa = (POA)
                orb.resolve_initial_references( "RootPOA" );

            poa.the_POAManager().activate();

            //Current current = (org.omg.SecurityLevel2.Current)
            //    orb.resolve_initial_references( "SecurityCurrent" );

            org.omg.CORBA.Object demo =
                poa.servant_to_reference( new Server( /*current*/ ));

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
