package demo.policies;

import java.io.*;
import org.omg.CORBA.*;
import org.omg.Messaging.*;

/**
 * A simple demo that shows how to use CORBA QoS policies at the level
 * of individual objects, or ORB-wide. This is a variaton on hello
 * world and relies on IOR files rather than the naming service.
 *
 * @author Gerald Brose
 */

public class Client
{
    public static final int MSEC_FACTOR = 10000;
    public static final int ONE_SECOND = 1000 * MSEC_FACTOR;

    public static void main( String args[] ) throws Exception
    {
        if( args.length != 1 )
        {
            System.out.println( "Usage: jaco demo.policies.Client <ior_file>" );
            return;
        }

        // read IOR from file
        File f = new File( args[ 0 ] );
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
        BufferedReader br =
            new BufferedReader( new FileReader( f ));

        // initialize the ORB
        ORB orb = ORB.init( args, null );

        // get object reference from command-line argument file
        org.omg.CORBA.Object obj =
            orb.string_to_object( br.readLine() );

        br.close();

        // and narrow it to HelloWorld.GoodDay
        // if this fails, a BAD_PARAM will be thrown
        GoodDay goodDay = GoodDayHelper.narrow( obj );

        // get PolicyManager and create policies ....

        PolicyManager policyManager =
            PolicyManagerHelper.narrow( orb.resolve_initial_references("ORBPolicyManager"));

        // create an timeout value of 1 sec. The unit is a time
        // step of 100 nano secs., so 10000 of these make up a
        // micro second.
        Any rrtPolicyAny = orb.create_any();
        rrtPolicyAny.insert_ulonglong( ONE_SECOND );

        // create a relative roundtrip timeout policy and set this
        // policy ORB-wide
        Policy rrtPolicy =
            orb.create_policy( RELATIVE_RT_TIMEOUT_POLICY_TYPE.value,
                    rrtPolicyAny );

        policyManager.set_policy_overrides( new Policy[] {rrtPolicy},
                SetOverrideType.ADD_OVERRIDE);

        // create a sync scope policy
        Any syncPolicyAny = orb.create_any();
        syncPolicyAny.insert_short( SYNC_WITH_SERVER.value );
        Policy syncPolicy =
            orb.create_policy( SYNC_SCOPE_POLICY_TYPE.value, syncPolicyAny  );

        // set the sync scope policy on an object reference
        goodDay._set_policy_override( new Policy[] {syncPolicy},
                SetOverrideType.ADD_OVERRIDE);

        // try to invoke the operation and print the result: this
        // should result in a timeout exception because the server
        // will sleep longer than the timeout (sleep is int msecs.)
        System.out.println( "Should see a TIMEOUT exception soon...");
        try
        {
            System.out.println( goodDay.hello( 2000 ) );
            System.out.println( "... should not be here, no exception!");
        }
        catch ( org.omg.CORBA.TIMEOUT t )
        {
            System.out.println( "here it is: "  + t.getMessage());
        }

        // create another timeout poliy
        Any objRrtPolicyAny = orb.create_any();
        objRrtPolicyAny.insert_ulonglong( 4 * ONE_SECOND );

        Policy objRrtPolicy =
            orb.create_policy( RELATIVE_RT_TIMEOUT_POLICY_TYPE.value,
                    objRrtPolicyAny );

        goodDay._set_policy_override( new Policy[] {objRrtPolicy},
                SetOverrideType.ADD_OVERRIDE);

        // see what the effective policy is now...
        Policy objPol =
            goodDay._get_policy( RELATIVE_RT_TIMEOUT_POLICY_TYPE.value );

        if ( objPol != null)
        {
            long timoutValue = ((RelativeRoundtripTimeoutPolicy)objPol).relative_expiry();
            System.out.println("Object-level timeout is " + timoutValue + " timesteps (100ns)");

            // try the invocation again, in this case the override
            // policy's new timeout should be sufficient to let
            // the call come back
            System.out.println( goodDay.hello( 100 ));
        }
        else
        {
            System.out.println("ERROR, no Object-level timeout found");
        }
    }
}

