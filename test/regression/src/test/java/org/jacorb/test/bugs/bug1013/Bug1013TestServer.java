package org.jacorb.test.bugs.bug1013;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import org.jacorb.test.harness.TestUtils;
import org.jacorb.test.listenendpoints.echo_corbaloc.CmdArgs;
import org.jacorb.test.listenendpoints.echo_corbaloc.EchoMessageImpl;
import org.jacorb.util.ObjectUtil;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class Bug1013TestServer
{
    public static void main(String[] args)
    {
        try
        {
            CmdArgs cmdArgs = new CmdArgs("Server", args);
            cmdArgs.processArgs();

            // translate any properties set on the commandline but after the
            // class name to a properties
            java.util.Properties props = ObjectUtil.argsToProps(args);
            String implName = props.getProperty("jacorb.implname", "other");
            String objectId = implName + "-ID";
            TestUtils.getLogger().debug("Server: jacorb.implname: <" + implName + ">");

            //init ORB
            ORB orb = ORB.init(args, props);

            //init POA
            POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poa.the_POAManager().activate();
            EchoMessageImpl echoServant = new EchoMessageImpl(implName + "." + objectId);

            if (implName.equals("EchoServer"))
            {
                String poaName = "EchoServer-POA";

                //init new POA
                Policy[] policies = new Policy[2];
                policies[0] = poa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
                policies[1] = poa.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID);

                poa = poa.create_POA(poaName, poa.the_POAManager(), policies);

                for (int i = 0; i < policies.length; i++)
                {
                    policies[i].destroy();
                }

                poa.activate_object_with_id(objectId.getBytes(), echoServant);
            }
            else
            {
                poa.activate_object(echoServant);
            }

            String ior = orb.object_to_string(poa.servant_to_reference(echoServant));
            System.out.println("SERVER IOR: " + ior);
            System.out.flush();

            if (cmdArgs.getIORFile() != null)
            {
                PrintWriter ps = new PrintWriter(new FileOutputStream(
                                                     new File( cmdArgs.getIORFile())));
                ps.println(ior);
                ps.close();
            }


            // wait for requests
            orb.run();

        }
        catch (Exception e)
        {
        }
    }
}
