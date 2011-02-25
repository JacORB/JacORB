package org.jacorb.test.orb.connection.maxconnectionenforcement;

import java.util.Properties;
import org.jacorb.orb.giop.BiDirConnectionInitializer;
import org.jacorb.orb.giop.LRUSelectionStrategyImpl;
import org.jacorb.orb.giop.LRUStatisticsProviderImpl;
import org.jacorb.test.common.TestUtils;
import org.omg.BiDirPolicy.BIDIRECTIONAL_POLICY_TYPE;
import org.omg.BiDirPolicy.BOTH;
import org.omg.BiDirPolicy.BidirectionalPolicyValueHelper;
import org.omg.CORBA.Any;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

public class TestServer
{
    public static void main (String[] args)
    {
        try
        {
            Properties props = new Properties();
            props.put( "jacorb.connection.max_server_connections",
                       "10" );
            props.put( "jacorb.connection.selection_strategy_class",
                       LRUSelectionStrategyImpl.class.getName() );
            props.put( "jacorb.connection.statistics_provider_class",
                       LRUStatisticsProviderImpl.class.getName() );
            props.put( "org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                       BiDirConnectionInitializer.class.getName() );

            //init ORB
            org.omg.CORBA.ORB  orb = org.omg.CORBA.ORB.init(args, props);

            //init POA
            POA root_poa = POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));

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

            final String servantName = args[0];
            Class servantClass = TestUtils.classForName(servantName);
            Servant servant = ( Servant ) servantClass.newInstance();

            // create the object reference
            org.omg.CORBA.Object obj = bidir_poa.servant_to_reference(servant);

            System.out.println("SERVER IOR: "+orb.object_to_string(obj));
            System.out.flush();

            orb.run();
        }
        catch( Exception e )
        {
            System.err.println ("ERROR " + e);
        }
    }
}
