package demo.poa_monitor.user_poa;

import org.omg.PortableServer.*;

public class FooAdapterActivatorImpl 
    extends _AdapterActivatorLocalBase 
{
    private  org.omg.CORBA.ORB orb;

    public FooAdapterActivatorImpl( org.omg.CORBA.ORB orb ) 
    {
        this.orb = orb;
    }

    public boolean unknown_adapter(POA parent, String name) 
    {		
        try 
        {
            if (name.equals(Server.fooPOAName) && (Server.kind == 1 ||
                                                   Server.kind == 2)) 
            {
                org.omg.CORBA.Policy [] policies = {
                    parent.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
                    parent.create_id_assignment_policy (IdAssignmentPolicyValue.USER_ID),
                    parent.create_lifespan_policy(LifespanPolicyValue.PERSISTENT)
                };

                POA newPOA = parent.create_POA(name, parent.the_POAManager(), policies);

                for (int i=0; i<policies.length; i++) 
                    policies[i].destroy();
				
                newPOA.set_servant_manager(new FooServantActivatorImpl( orb ) );		
                return true;

            } 
            else if (name.equals(Server.fooPOAName) && Server.kind == 3) 
            {	
                org.omg.CORBA.Policy [] policies = {
                    parent.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
                    parent.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
                    parent.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                    parent.create_lifespan_policy(LifespanPolicyValue.PERSISTENT)
                };
                POA newPOA = parent.create_POA(name, parent.the_POAManager(), policies);
                for (int i=0; i<policies.length; i++) policies[i].destroy();
				
                newPOA.set_servant_manager(new FooServantLocatorImpl() );
                return true;
													
            } 
            else if (name.equals(Server.fooPOAName) && Server.kind == 4) 
            {
                org.omg.CORBA.Policy [] policies = {
                    parent.create_request_processing_policy(RequestProcessingPolicyValue.USE_DEFAULT_SERVANT),
                    parent.create_id_uniqueness_policy(IdUniquenessPolicyValue.MULTIPLE_ID)
                };
                POA newPOA = parent.create_POA(name, parent.the_POAManager(), policies);
                for (int i=0; i<policies.length; i++) policies[i].destroy();

                newPOA.set_servant(new FooImpl("0"));
                return true;
						
            } 
            else if (name.equals(Server.fooPOAName) && Server.kind == 5) 
            {					
                org.omg.CORBA.Policy [] policies = {
                    parent.create_id_uniqueness_policy(IdUniquenessPolicyValue.MULTIPLE_ID),
                    parent.create_implicit_activation_policy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION)
                };

                POA newPOA = parent.create_POA(name, parent.the_POAManager(), policies);
                for (int i=0; i<policies.length; i++) 
                    policies[i].destroy();
                return true;
					
            } 
            else if (name.equals(Server.fooPOAName) && Server.kind == 6) 
            {				
                org.omg.CORBA.Policy [] policies = {
                    parent.create_thread_policy(ThreadPolicyValue.SINGLE_THREAD_MODEL),
                    parent.create_implicit_activation_policy(ImplicitActivationPolicyValue.IMPLICIT_ACTIVATION)
                };
                POA newPOA = parent.create_POA(name, parent.the_POAManager(), policies);

                for (int i=0; i<policies.length; i++) 
                    policies[i].destroy();
                return true;	
            } 
            else 
            {
                System.out.println("unknown poa name (AdapterActivator)");
            }							
        } 
        catch (org.omg.PortableServer.POAPackage.AdapterAlreadyExists aae) {
            aae.printStackTrace();
        } 
        catch (org.omg.PortableServer.POAPackage.InvalidPolicy ip) {
            ip.printStackTrace();
        } 
        catch (org.omg.PortableServer.POAPackage.WrongPolicy wp) {
            wp.printStackTrace();
        }
        return false;
    }
}
