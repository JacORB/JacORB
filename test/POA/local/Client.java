
package test.POA.local;

import test.POA.local.*;
import org.omg.CosNaming.*;
import java.io.*;

public class Client 
{
    private static FooFactory factory;
    private static Foo [] foos;

    public static void main(String args[]) 
    {
        try 
        {		
            org.omg.CORBA.ORB orb = 
                org.omg.CORBA.ORB.init(args, null);
            // get hold of the naming service and create a fooFactory object
            NamingContextExt nc = 
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
            factory = 
                FooFactoryHelper.narrow(nc.resolve(nc.to_name("FooFactory.service") ));

            Foo foo = factory.createFoo("1");
            Foo foo_bar = factory.createFoo("2");

            foo.compute();

            foo.setFoo( foo_bar );
	
        }
        catch (Exception e) {
            e.printStackTrace();
        }		
    }

}
