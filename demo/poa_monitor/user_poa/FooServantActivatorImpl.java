package demo.poa_monitor.user_poa;

import demo.poa_monitor.foox.*;
import org.omg.PortableServer.*;

public class FooServantActivatorImpl 
    extends _ServantActivatorLocalBase
{
    private org.omg.CORBA.Object forwardRef = null;

    public FooServantActivatorImpl( org.omg.CORBA.ORB orb ) 
    {
	if (Server.kind == 2) 
        {
            try 
            {
                forwardRef = new FooImpl("0")._this(orb);
            } 
            catch (Throwable e) 
            {
                e.printStackTrace();
            }
	}	
    }


    public void etherealize( byte[] oid, 
                             POA adapter, 
                             Servant servant, 
                             boolean cleanup_in_progress,
                             boolean remaining_activations ) 
    {
        String oidStr = new String(oid);
        System.out.println("[ etherialize servant for oid: "+oidStr+" ]");
    }


    public Servant incarnate( byte[] oid, POA adapter) 
        throws ForwardRequest 
    {
        String oidStr = new String(oid);				
        int oidInt    = Integer.parseInt(oidStr);
        if (oidInt >= 1000) {
            if (Server.kind == 2 && (oidInt % 2) == 0) {
                System.out.println("[ trying to incarnate servant for oid: "+oidStr+" ]");
                System.out.println("[ forward request ]");
                throw new ForwardRequest(forwardRef);			
		
            } else {
                System.out.println("[ incarnate servant for oid: "+oidStr+" ]");
                return new FooImpl(oidStr);
            }	
        } else {
            throw new org.omg.CORBA.OBJECT_NOT_EXIST();

        }		
    }
}
