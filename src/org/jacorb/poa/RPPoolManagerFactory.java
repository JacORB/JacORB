package org.jacorb.poa;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.config.Configuration;
import org.jacorb.orb.ORB;

/**
 * factory class to create instances of RPPoolManager
 * depending on the configuration of the ORB this
 * factory will return a new instance for every call
 * or return a shared instance.
 *
 * @author Alphonse Bendt
 * @version $Id$
 */
public class RPPoolManagerFactory
{
    private final ORB orb;
    private final Logger logger;
    private final Configuration configuration;
    private final int threadPoolMin;
    private final int threadPoolMax;
    private final FactoryDelegate delegate;

    public RPPoolManagerFactory(ORB orb) throws ConfigurationException
    {
        this.orb = orb;
        configuration = orb.getConfiguration();
        logger = configuration.getNamedLogger("jacorb.poa.controller");

        threadPoolMin =
            configuration.getAttributeAsInteger("jacorb.poa.thread_pool_min", 5);

        if (threadPoolMin < 1)
        {
            throw new ConfigurationException("jacorb.poa.thread_pool_min must be >= 1");
        }

        threadPoolMax =
            configuration.getAttributeAsInteger("jacorb.poa.thread_pool_max", 20);

        if (threadPoolMax < threadPoolMin)
        {
            throw new ConfigurationException("jacorb.poa.thread_pool_max must be >= " + threadPoolMin + "(jacorb.poa.thread_pool_min)" );
        }

        boolean poolsShouldBeShared = configuration.getAttributeAsBoolean("jacorb.poa.thread_pool_shared", false);

        if (logger.isDebugEnabled())
        {
            logger.debug("RequestProcessorPoolFactory settings: thread_pool_min=" + threadPoolMin + " thread_pool_max=" + threadPoolMax + " thread_pool_shared=" + poolsShouldBeShared);
        }

        if (poolsShouldBeShared)
        {
            delegate = new SharedPoolFactory();
        }
        else
        {
            delegate = new DefaultPoolFactory();
        }
    }

    public void destroy()
    {
        delegate.destroy();
    }

    /**
     * factory method to create a RPPoolManager instance.
     *
     * @param isSingleThreaded if true the returned poolmanager will only use one thread
     */
    public RPPoolManager newRPPoolManager(boolean isSingleThreaded)
    {
        if (isSingleThreaded)
        {
            return new RPPoolManager(orb.getPOACurrent(), 1, 1, logger, configuration)
            {
                void destroy()
                {
                    // allow destruction by clients
                    destroy(true);
                }
            };
        }

        return delegate.newRPPoolManager();
    }

    private interface FactoryDelegate
    {
        RPPoolManager newRPPoolManager();
        void destroy();
    }

    private class SharedPoolFactory implements FactoryDelegate
    {
        private final RPPoolManager sharedInstance = new RPPoolManager(orb.getPOACurrent(), threadPoolMin, threadPoolMax, logger, configuration)
        {
            void destroy()
            {
                // ignore request as this is a shared pool.
                // the pool will be destroyed with the enclosing factory.
            }
        };

        public RPPoolManager newRPPoolManager()
        {
            return sharedInstance;
        }

        public void destroy()
        {
            sharedInstance.destroy(true);
        }
    }

    private class DefaultPoolFactory implements FactoryDelegate
    {
        public RPPoolManager newRPPoolManager()
        {
            return new RPPoolManager(orb.getPOACurrent(), threadPoolMin, threadPoolMax, logger, configuration)
            {
                void destroy()
                {
                    // allow destruction by clients
                    destroy(true);
                }
            };
        }

        public void destroy()
        {
            // nothing to do. each created poolManager is associated to exactly one
            // poa which will destroy it as necessary
        }
    }
}
