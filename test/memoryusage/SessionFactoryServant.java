package test.memoryusage;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.PortableServer.*;

public class SessionFactoryServant 
    extends SessionFactoryPOA
{
    private ORB orb;
    private POA poa;
	
    public SessionFactoryServant( ORB orb, org.omg.PortableServer.POA poa) 
    {
        this.orb = orb;
        this.poa = poa;
    }
    
    public Session get_Session(String userName, String fullName, 
                               String emailAddress, String ip)
    {
        synchronized (this)
        {
            try
            {
                Session session = null;
                SessionServant servant = new SessionServant();
				//poa.activate_object(servant);
                org.omg.CORBA.Object o = poa.servant_to_reference( servant );
                session = SessionHelper.narrow(o);
					
				//session = servant._this(orb);
			
                return session;
            }
            catch( Exception e )
            {   
                jacorb.util.Debug.output(1,e);
                throw new org.omg.CORBA.UNKNOWN();
            }
        }	
    }
	
    public synchronized void releaseSession( Session session )
    {
        try
        {
            poa.deactivate_object(poa.reference_to_id( session ));
            // session._release();
        }
        catch(org.omg.PortableServer.POAPackage.WrongAdapter sna)
        {
            System.out.println(sna);
        }
        catch(org.omg.PortableServer.POAPackage.ObjectNotActive ona)
        {
            System.out.println(ona);
        }
        catch(org.omg.PortableServer.POAPackage.WrongPolicy wp)
        {
            System.out.println(wp);
        }
    }
}
