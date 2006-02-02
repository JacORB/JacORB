package test.servantscaling;

import org.omg.CosNaming.*;

public class Client
{
    private SessionFactory sf;
    private org.omg.CORBA.ORB orb;
    private NamingContextExt nc;

    public Client (String args[])
    {
    	try {
            orb = org.omg.CORBA.ORB.init(args,null);
            nc = NamingContextExtHelper.narrow
                (orb.resolve_initial_references("NameService"));
            sf = SessionFactoryHelper.narrow
                (nc.resolve(nc.to_name("ServantScaling/SessionFactory")));

	}
	catch(Exception e)
	{
            sf = null;
            e.printStackTrace();
     	}
    }

    public void setPoa (POA_Kind pk)
    {
        System.out.println ("Setting poa to " + pk.value());
        long startTime = System.currentTimeMillis();
         sf.set_poa (pk);
        long endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        System.out.println ("set poa took " + delta + " msec");
    }

    public void runTest() {
        try {
            int counts[] = {100, 2000, 5000, 20000, 100000, 200000};
            for (int i = 0; i < counts.length; i++) {
                System.out.println ("creating " + counts[i] + " objects");
                long startTime = System.currentTimeMillis();
                sf.create_sessions (counts[i]);
                long endTime = System.currentTimeMillis();
                long delta = endTime - startTime;
                System.out.println ("creation took " + delta + " msec");
                startTime = System.currentTimeMillis();
                System.out.println ("sampling sessions");
                int sample = 100;
                for (int j = 0; j < counts[i]; j += counts[i]/sample) {
                    Session s = sf.get_session (j);
                }
                endTime = System.currentTimeMillis();
                delta = endTime - startTime;
                System.out.println ("sampling " + sample + " objects took " + delta + " msec, or " + (delta/sample) + " per call");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[])
    {
        //Client
        System.out.println(">testing client...");
        Client c = new Client(args);
        if (c.sf == null) {
            System.out.println ("Unable to initialize client");
            return;
        }
//         c.setPoa (POA_Kind.PK_SYSTEMID);
//         c.runTest();
        c.setPoa (POA_Kind.PK_USERID);
        c.runTest();
        c.setPoa (POA_Kind.PK_DEFSERVANT);
        c.runTest();
        c.setPoa (POA_Kind.PK_SERVANTLOC);
        c.runTest();
    }
}
