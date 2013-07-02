package org.jacorb.test.orb.connection;

import java.util.Properties;
import junit.framework.Test;
import org.jacorb.test.common.ClientServerSetup;
import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicyValueHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

/**
 * @author Andre Spiegel
 */
public class BiDirSetup extends ClientServerSetup
{
    private POA biDirPOA;

    public BiDirSetup (Test test,
                       Properties clientProperties,
                       Properties serverProperties)
    {
        super(test,
              "org.jacorb.test.orb.connection.BiDirServerImpl",
              "org.jacorb.test.orb.connection.BiDirServerImpl",
              clientProperties, serverProperties);
    }

    protected void doSetUp() throws Exception
    {
        ORB clientOrb = getClientOrb();
        POA clientRootPOA = getClientRootPOA();

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

    protected void doTearDown() throws Exception
    {
        biDirPOA.destroy(false, true);
        biDirPOA = null;
    }

    public POA getBiDirPOA()
    {
        return biDirPOA;
    }
}
