package test.maxConnectionEnforcement;

import java.io.*;
import java.util.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.BiDirPolicy.*;

import org.jacorb.util.*;

public class Client 
    extends CallbackIfPOA
{
    static TestIf remoteObj = null;
    static long callInterval = 0;
    static Random rnd = new Random();

    static CallbackIf myself = null;

    public void opOnCallback()
    {
        System.out.println( "opOnCallback called" );
    }

    public static void main( String args[] ) 
    {
        if( args.length != 3 ) 
        {
            System.out.println( "Usage: jaco test.maxConnectionEnforcement.Client <ior_file> <call interval> <# of threads>" );
            System.exit( 1 );
        }

        try 
        {
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

            Properties props = new Properties();

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

            myself = CallbackIfHelper.narrow( bidir_poa.servant_to_reference( new Client() ));

            BufferedReader br =
                new BufferedReader( new FileReader( f ));

            // get object reference from command-line argument file
            org.omg.CORBA.Object obj = 
                orb.string_to_object( br.readLine() );

            br.close();

            //narrow to test interface
            remoteObj = TestIfHelper.narrow( obj );

            callInterval = Integer.parseInt( args[1] );

            int threads = Integer.parseInt( args[2] );
            for( int i = 0; i < threads; i++ )
            {
                (new Thread( new Runnable()
                    {
                        public void run()
                        {
                            try
                            {                                
                                while( true )
                                {                     
                                    if( Math.abs( rnd.nextInt() ) % 3 > 0 )
                                    {
                                        //call remote op
                                        remoteObj.op();
                                        System.out.println(
                                            "Thread " + 
                                            Thread.currentThread().getName() + 
                                            " made normal call" );
                                    }
                                    else
                                    {
                                        remoteObj.doCallback( myself );
                                        System.out.println(
                                            "Thread " + 
                                            Thread.currentThread().getName() + 
                                            " made bidir call" );
                                    }

                                    Thread.sleep( Math.abs( rnd.nextLong() ) % callInterval );
                                }
                            }
                            catch( Exception e )
                            {
                                e.printStackTrace();
                            }

                            System.exit( -1 );
                        }
                    })).start();
            }
            
            Thread.sleep( Long.MAX_VALUE );
        }
        catch( Exception ex ) 
        {
            ex.printStackTrace();
        }
    }
}


