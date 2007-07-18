package test.delegateSync;

import java.io.*;
import org.omg.CORBA.*;
import java.util.Properties;

public class Test
{

    public boolean keep_going = true;

    public class Checker implements Runnable
    {
        private org.omg.CORBA.Object obj1;
        private org.omg.CORBA.Object obj2;
        public Checker (org.omg.CORBA.Object o1, org.omg.CORBA.Object o2)
        {
            obj1 = o1;
            obj2 = o2;
        }

        public void run ()
        {
            for (int count = 0; keep_going && count < 5; count++) {
                System.out.print (".");
                boolean whatever = obj1._is_equivalent(obj2);
//                 if (whatever)
//                     System.out.println ("objects are equivalent");
                try {
                    Thread.sleep (1000);
                } catch (InterruptedException ex) {
                }
            }
            if (keep_going)
            {
                System.out.println ("\nSuccess, the test didn't block");
                System.exit (0);
            }
        }
    }

    public void do_work (ORB orb, String ior)
    {
        org.omg.CORBA.Object obj1 = orb.string_to_object(ior);
        org.omg.CORBA.Object obj2 = orb.string_to_object(ior);

        Checker c = new Checker (obj1, obj2);
        Thread t = new Thread (c);

        t.start();
        System.out.println ("starting invocation");

        // just do anything to force an invocation;
        if (obj1._is_a ("IDL:DoesNotMatter:1.0"))
            System.out.println ("Wierd, but _is_a returned true!");
        System.out.println ("invocation complete");
    }

    public static void main( String args[] )
    {
        if( args.length != 1 )
	{
            System.out.println( "Usage: jaco test.delegateSync.Test <ior|fake_host>" );
            System.exit( 1 );
        }

        Test c = new Test();
        try
	{
            Properties p = new Properties();
//            p.setProperty("jacorb.log.default.verbosity","4");
            p.setProperty("jacorb.connection.client.connect_timeout","8000");
            // initialize the ORB.
            ORB orb = ORB.init( args,p );

            if (args[0].startsWith ("file") ||
                args[0].startsWith ("corbaloc"))
                c.do_work (orb, args[0]);
            else
            {
                String ior = "corbaloc::" + args[0] + ":36660/bogus";
                c.do_work (orb, ior);
            }
        }
        catch( Exception ex )
	{
            ex.printStackTrace();
        }

        c.keep_going = false;
        System.out.println ("Failed, the equivalency test was blocked or a bad ior/test host specified");
    }
}
