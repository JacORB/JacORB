package org.jacorb.demo.appserver.ejb;

import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.jacorb.demo.appserver.GoodDay;
import org.jacorb.demo.appserver.GoodDayHelper;
import org.jacorb.demo.appserver.GoodDayImpl;
import org.jboss.logging.Logger;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * Startup a CORBA GoodDay Server on deployment
 */
@Singleton
@Startup
//@ApplicationScoped
public class CorbaService
{

    private static final Logger logger = Logger.getLogger(CorbaService.class.getName());

    private ORB orb;

    private POA poa;

    private GoodDay goodDayServer;

    private String ior;

    @PostConstruct
    public void init()
    {
        Properties orbProps = new Properties();
        orbProps.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
        orbProps.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");
        orbProps.setProperty("jacorb.interop.null_string_encoding", "true");

        orb = ORB.init((String[]) null, orbProps);
        logger.info("===> Created ORB " + orb.toString());

        try
        {
            poa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

            poa.the_POAManager().activate();

            goodDayServer = GoodDayHelper.narrow(poa.servant_to_reference(new GoodDayImpl()));

            ior = orb.object_to_string(goodDayServer);

            logger.info("Created GoodDay service...");
        }
        catch (InvalidName invalidName)
        {
            invalidName.printStackTrace();
        }
        catch (AdapterInactive adapterInactive)
        {
            adapterInactive.printStackTrace();
        }
        catch (WrongPolicy wrongPolicy)
        {
            wrongPolicy.printStackTrace();
        }
        catch (ServantNotActive servantNotActive)
        {
            servantNotActive.printStackTrace();
        }
    }

    @PreDestroy
    public void shutdown()
    {
        orb.shutdown(true);
    }

    public GoodDay getServer()
    {
        return goodDayServer;
    }

    public String getIOR()
    {
        return ior;
    }

}
