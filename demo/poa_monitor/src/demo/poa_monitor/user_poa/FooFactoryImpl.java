package demo.poa_monitor.user_poa;

import demo.poa_monitor.foox.*;
import org.omg.PortableServer.*;

public class FooFactoryImpl 
    extends FooFactoryPOA 
{
    private static FooImpl single = new FooImpl("0");

    public POA _default_POA() 
    {
        POA rootPOA = super._default_POA();
        try 
        {
            return rootPOA.find_POA(Server.factoryPOAName, false);			
        } 
        catch( org.omg.PortableServer.POAPackage.AdapterNonExistent e ) 
        {
            e.printStackTrace();
            return rootPOA;
        }
    }

    public Foo createFoo(String id) 
    {
        Foo result = null;
        try {			
            POA fooPOA = _default_POA().find_POA(Server.fooPOAName, true);

            if (Server.kind == 1 || Server.kind == 2 || Server.kind == 3) 
            {
                byte[] oid = id.getBytes();
                result = FooHelper.narrow(fooPOA.create_reference_with_id(oid, FooHelper.id()));
            } 
            else if (Server.kind == 4) 
            {
                result = FooHelper.narrow(fooPOA.create_reference(FooHelper.id()));
			
            } 
            else if (Server.kind == 5) 
            {
                result = FooHelper.narrow(fooPOA.servant_to_reference(single));
				/* for testing the colocation optimization */
                System.out.println("[ invocation of the coolocated Foo object ... ]");
                result.compute(0);				
                System.out.println("[ ... done ]");
				/*******************************************/
            } 
            else 
            {	
                result = FooHelper.narrow(fooPOA.servant_to_reference(new FooImpl("0")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("[ Foo created with id: "+id+" ]");
        return result;
    }

    public String getServerDescription() 
    {
        String myOid = "unknown";
        try 
        {
            org.omg.PortableServer.Current current =
                org.omg.PortableServer.CurrentHelper.narrow(_orb().resolve_initial_references("POACurrent"));
            myOid = new String(current.get_object_id());
			
        } 
        catch (org.omg.CORBA.ORBPackage.InvalidName in) 
        {
            System.out.println("POACurrent not available!");
			
        } 
        catch (org.omg.PortableServer.CurrentPackage.NoContext nc) {
            System.out.println("Cannot determine the context oid!");
        }

        System.out.println("[ "+myOid+": description requested (\""+Server.description+"\") ]");
        return Server.description;
    }
}
