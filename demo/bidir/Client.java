package demo.bidir;

import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.BiDirPolicy.*;
import org.omg.CORBA.*;

import java.util.Properties;
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
        Properties props = new Properties();
        props.put( "org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                   "org.jacorb.orb.connection.BiDirConnectionInitializer" );

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init( args, props );

        NamingContextExt nc = 
            NamingContextExtHelper.narrow(
                orb.resolve_initial_references( "NameService" ));
        
        org.omg.CORBA.Object o = 
            nc.resolve( nc.to_name( "bidir.example" ));

        Server server = ServerHelper.narrow( o );

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
        
        server.register_callback( ccb );
        
        server.callback_hello( "A test string" );
    }        
}// Client
