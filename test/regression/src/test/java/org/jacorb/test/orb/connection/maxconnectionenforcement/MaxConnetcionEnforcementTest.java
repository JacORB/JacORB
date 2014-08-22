package org.jacorb.test.orb.connection.maxconnectionenforcement;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import org.jacorb.orb.giop.BiDirConnectionInitializer;
import org.jacorb.test.harness.ClientServerSetup;
import org.jacorb.test.harness.ClientServerTestCase;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
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
import org.omg.PortableServer.POAHelper;

public class MaxConnetcionEnforcementTest extends ClientServerTestCase
{
    static Random rnd = new Random();

    @BeforeClass
    public static void beforeClassSetUp() throws Exception
    {

        Properties props = new Properties();
        props.put( "org.omg.PortableInterceptor.ORBInitializerClass.bidir_init",
                   BiDirConnectionInitializer.class.getName() );

        setup = new ClientServerSetup(TestServer.class.getName(), TestIfImpl.class.getName(), props, null);

    }

    @Test
    public void testMaxConnetcionEnforcement()
    {
        try
        {
            //init ORB
            ORB orb = setup.getClientOrb();

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

            final CallbackIf myself = CallbackIfHelper.narrow( bidir_poa.servant_to_reference( new CallbackIfImpl() ));

            //narrow to test interface
            final TestIf remoteObj = TestIfHelper.narrow( setup.getServerObject() );

            final int callInterval = 1;
            int threads = 10;

            List<Thread> threadPool = new LinkedList<Thread>();
            Thread thread;
            for( int i = 0; i < threads; i++ )
            {
                thread = new Thread( new Runnable()
                    {
                        public void run()
                        {
                            try
                            {
                                int c = 10;
                                while( c-- > 0 )
                                {
                                    if( Math.abs( rnd.nextInt() ) % 3 > 0 )
                                    {
                                        //call remote op
                                        Assert.assertTrue(remoteObj.op());
                                    }
                                    else
                                    {
                                        Assert.assertTrue(remoteObj.doCallback( myself ));
                                    }

                                    Thread.sleep( Math.abs( rnd.nextLong() ) % callInterval );
                                }
                            }
                            catch( Exception e )
                            {
                            }
                        }
                    });
                thread.start();
                threadPool.add(thread);
            }

            for(Object thr_obj : threadPool )
            {
                ((Thread)thr_obj).join();
            }
        }
        catch (Exception e)
        {
        }
    }
}
