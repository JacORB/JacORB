package miop_tao_interop;

import java.util.Properties;
import java.util.Arrays;
import org.omg.CORBA.ORB;

public class Client implements Runnable
{
    private UIPMC_Object obj;
    private int payload;
    private int calls;
    private int id;
    private int sleep;

    public Client (UIPMC_Object obj, int payload, int offset, int calls,
            int sleep)
    {
        this.obj = obj;
        this.payload = payload;
        this.calls = calls;
        this.id = offset;
        this.sleep = sleep;
    }

    public void run()
    {
        int i;
        synchronized (this)
        {
            i = this.id++;
        }

        byte[] seq = new byte[this.payload];
        Arrays.fill(seq, (byte) ClientIDs.value.charAt(i));

        for (int j = 0; j < this.calls; ++j)
        {
            this.obj.process(seq);

            try
            {
                Thread.sleep(this.sleep);
            }
            catch (InterruptedException _)
            {
            }
        }
    }

    private static String ior = "file://test.ior";
    private static int payload_length = 1000;
    private static int client_threads = 5;
    private static int id_offset = 0;
    private static int payload_calls = 100;
    private static int sleep_millis = 100;
    private static boolean do_shutdown = false;

    private static boolean parse_args(String[] args)
    {
        for (int i = 0; i < args.length; ++i)
        {
            if (args[i].equals("-k"))
                ior = args[++i];
            else if (args[i].equals("-p"))
                payload_length = Integer.parseInt(args[++i]);
            else if (args[i].equals("-t"))
                client_threads = Integer.parseInt(args[++i]);
            else if (args[i].equals("-f"))
                id_offset = Integer.parseInt(args[++i]);
            else if (args[i].equals("-c"))
                payload_calls = Integer.parseInt(args[++i]);
            else if (args[i].equals("-s"))
                sleep_millis = Integer.parseInt(args[++i]);
            else if (args[i].equals("-x"))
            {
                do_shutdown = true;
                ++i;
            }
            else
            {
                System.err.println("usage: Test.Client -k <ior> -p <payload_length> -t <client_threads> -f <id_offset> -c <payload_calls> -s <sleep_millis> -x");
                return false;
            }
        }
        System.out.println("args to be used: -k '" + ior + "' -p "
                + Integer.toString(payload_length) + " -t "
                + Integer.toString(client_threads) + " -f "
                + Integer.toString(id_offset) + " -c "
                + Integer.toString(payload_calls) + " -s "
                + Integer.toString(sleep_millis) + (do_shutdown ? " -x" : ""));
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

        Hello hello_obj = HelloHelper.narrow(orb.string_to_object(ior));

        if (do_shutdown)
            hello_obj.shutdown();
        else
        {
            UIPMC_Object uipmc_obj = hello_obj.get_object();

            Client client = new Client(uipmc_obj, payload_length, id_offset,
                                       payload_calls, sleep_millis);

            Thread[] client_runners = new Thread[client_threads];
            for (int i = 0; i < client_runners.length; ++i)
            {
                client_runners[i] = new Thread(client);
                client_runners[i].start();
            }

            for (int i = 0; i < client_runners.length; ++i)
                client_runners[i].join();

            while (orb.work_pending())
                orb.perform_work();
        }

        System.out.println("\nClient finished successfully.");
    }
}
