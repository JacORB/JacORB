package test.maxConnectionEnforcement;

import java.io.*;
import java.util.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.BiDirPolicy.*;

import org.jacorb.util.*;

public class Server 
    extends TestIfPOA
{
    Random rnd = null;

    public Server()
    {
        rnd = new Random();
    }

    public void op()
    {
        System.out.println("op called");
    }

    public void doCallback( CallbackIf callback )
    {
        System.out.println("doCallback called");
        
        try
        {
            Thread.sleep( Math.abs( rnd.nextInt() ) % 100 );
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }

        System.out.println("\tslept");

        callback.opOnCallback();

        System.out.println("\treturned");
    }
        
    public static void main(String[] args) 
    {
        if( args.length != 2 ) 
        {
            System.out.println(
                "Usage: jaco test.maxConnectionEnforcement.Server <ior_file> <max transports>");
            System.exit( 1 );
        }

        try 
        {         
            Properties props = new Properties();
            props.put( "jacorb.connection.max_server_connections",
                       args[1] );
            props.put( "jacorb.connection.selection_strategy_class",
                       "org.jacorb.orb.connection.LRUSelectionStrategyImpl" );
            props.put( "jacorb.connection.statistics_provider_class",
                       "org.jacorb.orb.connection.LRUStatisticsProviderImpl" );
            props.put( "org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                       "org.jacorb.orb.connection.BiDirConnectionInitializer" );


            //init ORB
            ORB orb = ORB.init( args, props );

            //init POA
            POA root_poa = 
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

            Any any = orb.create_any();
            BidirectionalPolicyValueHelper.insert( any, BOTH.value );

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


            Server s = new Server();
            
            // create the object reference
            org.omg.CORBA.Object obj = 
                bidir_poa.servant_to_reference( s );

            PrintWriter pw = 
                new PrintWriter( new FileWriter( args[ 0 ] ));

            // print stringified object reference to file
            pw.println( orb.object_to_string( obj ));
            
            pw.flush();
            pw.close();
    
            // wait for requests
            orb.run();
        }
        catch( Exception e ) 
        {
            System.out.println( e );
        }
    }
}


