package test.naming;

import org.omg.CosNaming.*;

public class RIRTest
{

    public static void main(String args[]) 
    { 
	try
	{
	    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,null);

            org.omg.CORBA.Object obj;

            try
            {
                obj = orb.string_to_object("corbaname:rir:");
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }

            try
            {            
                obj = orb.string_to_object("corbaname:rir:#");
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }

            try
            {
                obj = orb.string_to_object("corbaname:rir:/");
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }

            try
            {
                obj = orb.string_to_object("corbaname:rir:NameService");
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }

	    System.out.println("done. ");       
	    System.exit(0);
	}
	catch (Exception e) 
	{
	    e.printStackTrace();
	    System.exit(1);
	}
    }
}








