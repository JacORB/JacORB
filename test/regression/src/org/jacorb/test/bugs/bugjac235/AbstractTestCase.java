package org.jacorb.test.bugs.bugjac235;

//import java.io.*;
import org.jacorb.test.common.ClientServerSetup;
import org.jacorb.test.common.ClientServerTestCase;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.CORBA.PolicyManager;
import org.omg.CORBA.PolicyManagerHelper;
import org.omg.CORBA.SetOverrideType;
import org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE;
import org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE;
import org.omg.Messaging.SYNC_WITH_SERVER;

/**
 * This tests the setting of reply timeouts using the RELATIVE_RT_TIMEOUT_POLICY
 * and the jacorb.connection.client.pending_reply_timeout property.
 * A problem existed where the policy value was longer than the property value,
 * the timeout was occurring according to the property value and the policy
 * value was ignored.  A fix was implemented to wait for the timer created
 * using the policy value to complete if the property value timed out first.
 * The tests cover the different circumstances where neither/both/either of
 * these QoS items are set.
 */
public class AbstractTestCase extends ClientServerTestCase
{
    protected static final String PROP_PENDING_REPLY_TIMEOUT = "jacorb.connection.client.pending_reply_timeout";
    private static final int MSEC_FACTOR = 10000;

    protected JAC235 server;
    protected ORB orb;

    public AbstractTestCase (String name, ClientServerSetup setup)
    {
        super (name, setup);
    }

    protected final void setUp() throws Exception
    {
        server = JAC235Helper.narrow(setup.getServerObject());

        orb = setup.getClientOrb();
    }

    protected final void tearDown() throws Exception
    {
        server = null;
        orb = null;
    }

    protected void setServerPolicy() throws PolicyError
    {
        // create a sync scope policy
        Any syncPolicyAny = orb.create_any();
        syncPolicyAny.insert_short( SYNC_WITH_SERVER.value );

        Policy syncPolicy =
            orb.create_policy
                ( SYNC_SCOPE_POLICY_TYPE.value, syncPolicyAny  );


        // set the sync scope policy on an object reference
        server._set_policy_override( new Policy[] {syncPolicy},
                                  SetOverrideType.ADD_OVERRIDE);
    }

    protected void setTimeout(final int timeoutInMillis) throws Exception
    {
        // get PolicyManager and create policies ....
        PolicyManager policyManager =
            PolicyManagerHelper.narrow
                ( orb.resolve_initial_references("ORBPolicyManager"));

        // create an timeout value of 1 sec. The unit is a time
        // step of 100 nano secs., so 10000 of these make up a
        // micro second.
        Any rrtPolicyAny = orb.create_any();
        rrtPolicyAny.insert_ulonglong (timeoutInMillis * MSEC_FACTOR);

        // create a relative roundtrip timeout policy and set this
        // policy ORB-wide
        Policy rrtPolicy =
            orb.create_policy( RELATIVE_RT_TIMEOUT_POLICY_TYPE.value,
                               rrtPolicyAny );

        policyManager.set_policy_overrides( new Policy[] {rrtPolicy},
                                            SetOverrideType.ADD_OVERRIDE);
    }
}
