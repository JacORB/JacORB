import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.BiDirPolicy.*;
import org.omg.CORBA.*;

import java.util.Properties;
import java.io.*;
/**
 * Client.java
 *
 *
 * Created: Mon Sep  3 19:28:34 2001
 *
 * @author Nicolas Noffke
 * @version $Id$
 */

public class Client 
    extends ClientCallbackPOA 
{
    public Client ()
    {        
    }
    
    public void hello( String message )
    {
        System.out.println( "Client callback object received hello message >" + 
                            message + 
                            '<');
    }

    public static void main( String[] args )
        throws Exception
    {
        if( args.length != 1 ) 
	{
            System.out.println( "Usage: jaco Client <ior_file>" );
            System.exit( 1 );
        }


        File f = new File( args[ 0 ] );

        //check if file exists
        if( ! f.exists() )
        {
            System.out.println("File " + args[0] + 
                               " does not exist.");
                
            System.exit( -1 );
        }
            
        //check if args[0] points to a directory
        if( f.isDirectory() )
        {
            System.out.println("File " + args[0] + 
                               " is a directory.");
                
            System.exit( -1 );
        }

        // initialize the ORB.
        Properties props = new Properties();
        props.put( "org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                   "org.jacorb.orb.connection.BiDirConnectionInitializer" );

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, props );

        BufferedReader br =
            new BufferedReader( new FileReader( f ));

        // get object reference from command-line argument file
        org.omg.CORBA.Object obj = 
            orb.string_to_object( br.readLine() );

        br.close();

        CallbackServer server = CallbackServerHelper.narrow( obj );

        Any any = orb.create_any();
        BidirectionalPolicyValueHelper.insert( any, BOTH.value );

        POA root_poa = (POA) orb.resolve_initial_references( "RootPOA" );

        Policy[] policies = new Policy[4];
        policies[0] = 
            root_poa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);

        policies[1] = 
            root_poa.create_id_assignment_policy(IdAssignmentPolicyValue.SYSTEM_ID);

        policies[2] = 
            root_poa.create_implicit_activation_policy( ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION );

        policies[3] = orb.create_policy( BIDIRECTIONAL_POLICY_TYPE.value,
                                         any );

        POA bidir_poa = root_poa.create_POA( "BiDirPOA",
                                             root_poa.the_POAManager(),
                                             policies );
        bidir_poa.the_POAManager().activate();

        ClientCallback ccb = ClientCallbackHelper.narrow( bidir_poa.servant_to_reference( new Client() ));

//          PrintWriter out = new PrintWriter( new FileWriter( "callback_IOR" ));
//          out.println( orb.object_to_string( ccb ));
//          out.flush();
//          out.close();
        
        server.callback_hello( ccb, "A test string" );
    }        
}// Client
