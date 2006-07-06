package org.jacorb.test.orb.connection;

import java.util.Properties;

import junit.framework.Test;

import org.jacorb.test.common.ClientServerSetup;
import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicyValueHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

/**
 * @author Andre Spiegel
 * @version $Id$
 */
public class BiDirSetup extends ClientServerSetup
{
    private POA biDirPOA = null;

    public BiDirSetup (Test test,
                       Properties clientProperties,
                       Properties serverProperties)
    {
        super(test, "org.jacorb.test.orb.connection.BiDirServerImpl",
              clientProperties, serverProperties);
    }

    public String getTestServerMain()
    {
        return "org.jacorb.test.orb.connection.BiDirServerImpl";
    }

    public void setUp() throws Exception
    {
        super.setUp();

        Policy[] policies = new Policy[4];
        policies[0] =
            clientRootPOA.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);

        policies[1] =
            clientRootPOA.create_id_assignment_policy(IdAssignmentPolicyValue.SYSTEM_ID);

        policies[2] =
            clientRootPOA.create_implicit_activation_policy( ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION );

        Any any = clientOrb.create_any();
        BidirectionalPolicyValueHelper.insert( any, BOTH.value );
        policies[3] = clientOrb.create_policy( BIDIRECTIONAL_POLICY_TYPE.value,
                                         any );

        biDirPOA = clientRootPOA.create_POA( "BiDirPOA",
                                             clientRootPOA.the_POAManager(),
                                             policies );
        biDirPOA.the_POAManager().activate();
    }

    public POA getBiDirPOA()
    {
        return biDirPOA;
    }

    /**
     * <code>initSecurity</code> is a dummy implementation to prevent security
     * init for this test - this tests counts transports which are disrupted by
     * security initialisation.
     */
    protected void initSecurity()
    {
    }
}
