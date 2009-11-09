package demo.poa_monitor.user_poa;

import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;

import demo.poa_monitor.foox.*;

public class FooImpl extends FooPOA {
    private String id;
    public FooImpl(String _id) {		
        id = _id;
    }
    public void compute(int time) {
        try {		
            for (int i=1; i<=time; i=i+100) {
                Thread.currentThread().sleep(100);			
            }
        } catch (InterruptedException e) {
        }
    }
    public void deactivate() {
        try 
            {
                org.omg.PortableServer.Current current =
                    org.omg.PortableServer.CurrentHelper.narrow(_orb().resolve_initial_references("POACurrent"));
                
                POA myPOA = current.get_POA();
                byte[] myoid = current.get_object_id();

                myPOA.deactivate_object(myoid);
            } 
        catch (org.omg.CORBA.ORBPackage.InvalidName in) 
            {
                System.out.println("[ object deactivation fails: POACurrent not available ]");
                
            } 
        catch (org.omg.PortableServer.CurrentPackage.NoContext nc)
            {
                System.out.println("[ object deactivation fails: no invocation context ]");
            }
        catch (ObjectNotActive na) 
            {
                System.out.println("[ object deactivation fails: object not active ]");
            } 
        catch (WrongPolicy wp) 
            {
                System.out.println("[ object deactivation fails: wrong poa policy ]");
            } 
    }
}
