package demo.grid;

import java.io.BufferedReader;
import java.io.FileReader;
public class Client
{
    public static void main(String args[]) throws Exception
    {
        MyServer grid;

        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(args, null);

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));

        grid = MyServerHelper.narrow(orb.string_to_object(reader.readLine()));

        short x = grid.height();
        System.out.println("Height = " + x);

        short y = grid.width();
        System.out.println("Width = " + y);

        x -= 1;
        y -= 1;

        System.out.println("Old value at (" + x + "," + y +"): " +
                               grid.get( x,y));

        System.out.println("Setting (" + x + "," + y +") to 470.11");

        grid.set( x, y, new java.math.BigDecimal("470.11"));

        System.out.println("New value at (" + x + "," + y +"): " +
                               grid.get( x,y));

        try
        {
            grid.opWithException();
        }
        catch (demo.grid.MyServerPackage.MyException ex)
        {
            System.out.println("MyException, reason: " + ex.why);
        }

        orb.shutdown(true);
        System.out.println("done. ");
    }
}
