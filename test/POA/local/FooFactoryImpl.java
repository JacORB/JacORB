package test.POA.local;

import org.omg.PortableServer.*;

public class FooFactoryImpl 
    extends FooFactoryPOA 
{
    private static FooImpl single = new FooImpl();
    private POA myPOA;

    public FooFactoryImpl(POA myPOA)
    {
        this.myPOA = myPOA;
    }

    public POA _default_POA() 
    {
        return myPOA;
    }

    public Foo createFoo(String id) 
    {
        Foo result = null;
        try 
        {
            POA fooPOA = _default_POA().find_POA(Server.fooPOAName, true);

            byte[] oid = id.getBytes();
            result = FooHelper.narrow(fooPOA.create_reference_with_id(oid, FooHelper.id()));

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        System.out.println("[ Foo created with id: "+id+" ]");
        return result;
    }

}
