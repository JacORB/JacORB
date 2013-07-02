package org.jacorb.test.bugs.bugjac685;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer._ServantActivatorLocalBase;
import org.omg.PortableServer._ServantLocatorLocalBase;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;


public class SessionFactoryServant
    extends SessionFactoryPOA
{
    private ORB orb;
    private POA poaList[];
    private POA currentPoa;
    private byte [][] objectIds;

    private int poa_kind = 0;
    private int nextId = 0;

    private SessionServant defServant;

    public class SessionLocator extends _ServantLocatorLocalBase
    {
        public void postinvoke(byte[] oid,
                               POA adapter,
                               String operation,
                               java.lang.Object the_cookie,
                               Servant the_servant)
        {
        }

        public Servant preinvoke(byte[] oid,
                                 POA adapter,
                                 String operation,
                                 CookieHolder the_cookie)
            throws org.omg.PortableServer.ForwardRequest
        {
            return defServant;
        }
    }

    public class SessionActivator extends _ServantActivatorLocalBase
    {
        public Servant incarnate(byte[] oid, POA adapter) throws ForwardRequest
        {
            objectIds[nextId++] = oid;

            ///
            try
            {
                NamingContextExt nc =
                NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));

                nc.resolve_str("ServantScaling/SessionFactory");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            ///

            return new SessionServant(oid);
        }

        public void etherealize(byte[] oid,
                                POA adapter,
                                Servant serv,
                                boolean cleanup_in_progress,
                                boolean remaining_activations)
        {
        }
    }

    public SessionFactoryServant( ORB orb)
    {
        try
        {
            this.orb = orb;

            org.omg.PortableServer.Current current =
            org.omg.PortableServer.CurrentHelper.narrow(orb.resolve_initial_references("POACurrent"));

            defServant = new SessionServant(current);
            ServantManager locator = new SessionLocator();
            ServantManager activator = new SessionActivator();

            POA rootPOA =
            POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            poaList = new POA[4];
            Policy userIdPolicy =
            rootPOA.create_id_assignment_policy (IdAssignmentPolicyValue.USER_ID);
            Policy multiIdPolicy =
            rootPOA.create_id_uniqueness_policy (IdUniquenessPolicyValue.MULTIPLE_ID);
            Policy retainPolicy =
            rootPOA.create_servant_retention_policy (ServantRetentionPolicyValue.NON_RETAIN);
            Policy defServPolicy =
            rootPOA.create_request_processing_policy (RequestProcessingPolicyValue.USE_DEFAULT_SERVANT);

            Policy servMangPolicy =
            rootPOA.create_request_processing_policy (RequestProcessingPolicyValue.USE_SERVANT_MANAGER);

            poaList[0] = rootPOA.create_POA ("sysId",
                                             rootPOA.the_POAManager(),
                                             new Policy[] {multiIdPolicy});
            poaList[1] = rootPOA.create_POA ("userId",
                                             rootPOA.the_POAManager(),
                                             new Policy[] {userIdPolicy, multiIdPolicy});
            poaList[2] = rootPOA.create_POA ("defserv",
                                             rootPOA.the_POAManager(),
                                             new Policy[]
                                             {userIdPolicy, multiIdPolicy, defServPolicy, retainPolicy});
            poaList[2].set_servant (defServant);

            poaList[3] = rootPOA.create_POA ("servloc",
                                             rootPOA.the_POAManager(),
                                             new Policy[]
                                             {userIdPolicy, multiIdPolicy, servMangPolicy});
            poaList[3].set_servant_manager (activator);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        currentPoa = poaList[0];
    }

    private void kill_old_ids ()
    {
        if (objectIds == null)
        {
            return;
        }

        try
        {
            for (int i = 0; i < nextId; i++)
            {
                currentPoa.deactivate_object (objectIds[i]);
            }

        }
        catch (Exception ex) {}

        nextId = 0;
        objectIds = null;
    }

    public void set_poa (POA_Kind kind)
    {
        kill_old_ids();
        poa_kind = kind.value();
        currentPoa = poaList[poa_kind];
    }

    public void create_sessions(int count)
    {
        kill_old_ids();
        try
        {
            if (poa_kind != 2)
            {
                objectIds = new byte[count][];
            }

            if (poa_kind > 1)
            {
                return;  // nothing to do references returned on demand
            }

            for (nextId = 0; nextId < count; nextId++)
            {
                byte objId[];
                SessionServant servant = new SessionServant();

                if (poa_kind == 1)
                {
                    objId = ("object " + Integer.toString(nextId)).getBytes();
                    currentPoa.activate_object_with_id(objId,defServant);
                }
                else
                {
                    objId = currentPoa.activate_object (servant);
                }

                servant.setID(objId);
                objectIds[nextId] = objId;
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new org.omg.CORBA.UNKNOWN();
        }
    }

    public Session get_session (int index)
    {
        try
        {
            org.omg.CORBA.Object obj = poa_kind < 2 ?
            currentPoa.id_to_reference(objectIds[index]) :
            currentPoa.create_reference_with_id (("object "
                                                  + Integer.toString(index)).getBytes(),
                                                 "IDL:org.jacorb.test.bugs.bugjac685/Session:1.0");

            return SessionHelper.narrow(obj);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new org.omg.CORBA.UNKNOWN();
        }
    }
}
