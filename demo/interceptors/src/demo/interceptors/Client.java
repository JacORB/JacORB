package demo.interceptors;

import java.io.BufferedReader;
import java.io.FileReader;

public class Client
{
    public static void main(String args[]) throws Exception
    {
        java.util.Properties props = new java.util.Properties();
        props.put("org.omg.PortableInterceptor.ORBInitializerClass.ForwardInit", "demo.interceptors.ClientInitializer");

        BufferedReader reader = new BufferedReader(new FileReader(args[1]));
        props.put("ORBInitRef.Target", reader.readLine());
        reader.close ();

        MyServer grid;
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args,props);

        reader = new BufferedReader(new FileReader(args[0]));

        grid = MyServerHelper.narrow(orb.string_to_object(reader.readLine()));
        reader.close ();

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
}
