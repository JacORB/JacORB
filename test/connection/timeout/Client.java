package test.connection.timeout;

import org.omg.CosNaming.*;

public class Client
{
    public static void main(String args[]) 
    { 
	try
	{
	    MyServer grid;

	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

	    if( args.length == 1 )
	    {
		// args[0] is an IOR-string 
		grid = MyServerHelper.narrow(orb.string_to_object(args[0]));
	    } 
	    else
	    {
		NamingContextExt nc = 
                    NamingContextExtHelper.narrow(
      		        orb.resolve_initial_references( "NameService" ));

                org.omg.CORBA.Object o = 
                    nc.resolve(nc.to_name("grid.example"));

		grid = MyServerHelper.narrow(o);

	    }

            short x = -1;
            short y = -1;
            try
            {
                x = grid.height();
            }
            catch (org.omg.CORBA.IMP_LIMIT e) 
            {
                e.printStackTrace();
            }

            System.out.println("Height = " + x);
            try
            {	    
                y = grid.width();
            }
            catch (org.omg.CORBA.IMP_LIMIT e) 
            {
                e.printStackTrace();
            }

	    System.out.println("Width = " + y);


            orb.shutdown(true);
            System.out.println("done. ");
       
	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	}
    }
}


