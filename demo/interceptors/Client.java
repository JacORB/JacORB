package demo.interceptors;

import org.omg.CosNaming.*;

public class Client
{
    public static void main(String args[]) 
    { 
        try
        {
            java.util.Properties props = new java.util.Properties();
            props.put
                ("org.omg.PortableInterceptor.ORBInitializerClass.ForwardInit",
                 "demo.interceptors.ClientInitializer");
	
            MyServer grid;
            org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,props);

            NamingContextExt nc = NamingContextExtHelper.narrow
                (orb.resolve_initial_references("NameService"));

            grid = MyServerHelper.narrow(nc.resolve(nc.to_name("grid1.example")));

            short x = grid.height();
            System.out.println("Height = " + x);

            short y = grid.width();
            System.out.println("Width = " + y);

            x -= 1;
            y -= 1;

            System.out.println("Old value at (" + x + "," + y +"): " + grid.get( x,y));
            System.out.println("Setting (" + x + "," + y +") to 470.11");
		
            grid.set( x, y, new java.math.BigDecimal("470.11"));

            System.out.println("New value at (" + x + "," + y +"): " + grid.get( x,y));

            try 
            {
                grid.opWithException();
            }
            catch (demo.interceptors.MyServerPackage.MyException ex) 
            {
                System.out.println("MyException, reason: " + ex.why);
            }

            grid._release();
            System.out.println("done. ");
	

        }
        catch (Exception e) 
        {
            e.getMessage();
            jacorb.util.Debug.output(2, e);
	  
            if (e instanceof org.omg.CosNaming.NamingContextPackage.NotFound)
                System.out.println("Reason: " + ((org.omg.CosNaming.NamingContextPackage.NotFound) e).why.value());
        }
    }
}


