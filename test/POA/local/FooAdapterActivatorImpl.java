package test.POA.local;

import org.omg.PortableServer.*;

public class FooAdapterActivatorImpl 
    extends AdapterActivatorPOA 
{
    public boolean unknown_adapter(POA parent, String name) 
    {
        try {

            org.omg.CORBA.Policy [] policies = {
                parent.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER),
                parent.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN),
                parent.create_id_assignment_policy(IdAssignmentPolicyValue.USER_ID),
                parent.create_lifespan_policy(LifespanPolicyValue.PERSISTENT)
            };
            POA newPOA = parent.create_POA(name, parent.the_POAManager(), policies);
            for (int i=0; i<policies.length; i++) policies[i].destroy();
				
            newPOA.set_servant_manager(new FooServantLocatorImpl()._this(_orb()));
            return true;

        } catch (org.omg.PortableServer.POAPackage.AdapterAlreadyExists aae) {
            aae.printStackTrace();
        } catch (org.omg.PortableServer.POAPackage.InvalidPolicy ip) {
            ip.printStackTrace();
        } catch (org.omg.PortableServer.POAPackage.WrongPolicy wp) {
            wp.printStackTrace();
        }
        return false;
    }
}
