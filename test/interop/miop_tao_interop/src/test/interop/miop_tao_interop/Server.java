package test.interop.miop_tao_interop;

import java.io.*;
import java.util.Properties;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableGroup.GOA;
import org.omg.PortableGroup.GOAHelper;

public class Server implements Runnable
{
    private ORB orb;

    public Server (ORB orb)
    {
        this.orb = orb;
    }

    public void run()
    {
        orb.run();
    }

    private static String uipmc_url = "corbaloc:miop:1.0@1.0-test-1/225.1.1.8:321581";
    private static String ior_output_file = "test.ior";
    private static int orb_threads = 10;
    private static int payload_length = 1000;
    private static int client_threads = 5;
    private static int payload_calls = 100;

    private static boolean parse_args(String[] args)
    {
        for (int i = 0; i < args.length; ++i)
        {
            if (args[i].equals("-o"))
                ior_output_file = args[++i];
            else if (args[i].equals("-u"))
                uipmc_url = args[++i];
            else if (args[i].equals("-s"))
                orb_threads = Integer.parseInt(args[++i]);
            else if (args[i].equals("-p"))
                payload_length = Integer.parseInt(args[++i]);
            else if (args[i].equals("-t"))
                client_threads = Integer.parseInt(args[++i]);
            else if (args[i].equals("-c"))
                payload_calls = Integer.parseInt(args[++i]);
            else
            {
                System.err.println("usage: Test.Server -o <iorfile> -u <uipmc_url> -s <orb_threads> -p <payload_length> -t <client_threads> -c <payload_calls>");
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) throws Exception
    {
        Properties props = new Properties();
        props.setProperty("jacorb.transport.factories",
                          "org.jacorb.orb.iiop.IIOPFactories,org.jacorb.orb.miop.MIOPFactories");
        props.setProperty("jacorb.transport.client.selector",
                          "org.jacorb.orb.miop.MIOPProfileSelector");

        // init ORB
        ORB orb = ORB.init(args, props);

        if (!parse_args(args)) return;

        // init POA
        POA poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

        System.out.println("I am here!!! " + poa);
        GOA goa = GOAHelper.narrow(poa);

        // create a UIPMC object
        UIPMCObjectImpl uipmcImpl = new UIPMCObjectImpl(payload_length,
                                                        client_threads,
                                                        payload_calls);

        org.omg.CORBA.Object obj = orb.string_to_object(uipmc_url);

        byte[] oid = goa.create_id_for_reference(obj);
        goa.activate_object_with_id(oid, uipmcImpl);

        UIPMC_Object uipmc_obj = UIPMC_ObjectHelper.unchecked_narrow(obj);

        System.out.println("MIOP object is <" + orb.object_to_string(obj) + ">");

        // create a Hello object
        HelloImpl helloImpl = new HelloImpl(orb, uipmc_obj);

        // create the object reference
        obj = helloImpl._this_object(orb);

        System.out.println("Activated as <" + orb.object_to_string(obj) + ">");

        PrintWriter ps = new PrintWriter(new FileWriter(ior_output_file));
        ps.println(orb.object_to_string(obj));
        ps.close();

        poa.the_POAManager().activate();

        Server server = new Server(orb);

        Thread[] orb_runners = new Thread[orb_threads];
        for (int i = 0; i < orb_runners.length; ++i)
        {
            orb_runners[i] = new Thread(server);
            orb_runners[i].start();
        }

        for (int i = 0; i < orb_runners.length; ++i)
            orb_runners[i].join();

        uipmcImpl.final_check();

        System.out.println("\nServer finished successfully.");
    }
}
