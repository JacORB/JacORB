package test.memoryusage;

import org.omg.CosNaming.*;

public class Client
{
    public static void main(String args[])
    {
        int objects = 0;

    	try
        {
            //Client
            System.out.println(">testing client...");

            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

            NamingContextExt nc = 
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            SessionFactory df = 
                SessionFactoryHelper.narrow(nc.resolve(nc.to_name("SessionFactory")));
            for (objects = 0; objects < 1000000; objects++)
            {
                Session session = df.get_Session("AS", "", "", "");
                String sessionID = session.getID();
                df.releaseSession(session);
                //session._release();
                // Thread.sleep(1);
            };
            System.out.println(">done.");
	} 
	catch(Exception e) 
	{
            System.err.println("loops so far: " + objects);
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
     	}  
    }
}



