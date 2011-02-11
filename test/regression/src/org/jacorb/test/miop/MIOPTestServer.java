package org.jacorb.test.miop;

import java.util.Properties;
import org.jacorb.orb.util.CorbaLoc;
import org.jacorb.test.common.TestUtils;
import org.omg.PortableGroup.GOA;
import org.omg.PortableGroup.GOAHelper;
import org.omg.PortableServer.Servant;

public class MIOPTestServer
{
    private static String miopURL  = "corbaloc:miop:1.0@1.0-TestDomain-1/224.1.239.2:1234";

    public static void main (String[] args)
    {
        try
        {
            Properties props = new Properties();
            props.setProperty
                ("jacorb.transport.factories", "org.jacorb.orb.iiop.IIOPFactories,org.jacorb.orb.miop.MIOPFactories");
            props.setProperty
                ("jacorb.transport.client.selector", "org.jacorb.orb.miop.MIOPProfileSelector");
            
            org.omg.CORBA.ORB  orb = org.omg.CORBA.ORB.init(args, props);

            GreetingService helloGroup = GreetingServiceHelper.unchecked_narrow(orb.string_to_object(miopURL));

            org.omg.PortableServer.POA poa;

            poa = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            poa.the_POAManager().activate();
            GOA goa = GOAHelper.narrow(poa);

            
            final String servantName = args[0];
            Class servantClass = TestUtils.classForName(servantName);
            Servant helloServant = ( Servant ) servantClass.newInstance();

            byte[] oid = poa.activate_object(helloServant);
            goa.associate_reference_with_id(helloGroup,oid);

            String groupURL = miopURL + ";" + CorbaLoc.generateCorbaloc (orb, helloServant._this_object());

            System.out.println("SERVER IOR: "+groupURL);

            TestUtils.log("using IOR: " + groupURL);
            System.out.flush();

            orb.run();
        }
        catch( Exception e )
        {
            System.err.println ("ERROR " + e);
        }
    }
}
