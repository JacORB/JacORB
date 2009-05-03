package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import org.jacorb.config.*;
import org.slf4j.Logger;
import org.jacorb.util.ObjectUtil;


/**
 * A lightweight implementation of a POA monitor
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.02, 12/08/99, RT
 */

public class POAMonitorLightImpl
    implements POAMonitor
{
    private POA poaModel;
    private AOM aomModel;
    private RequestQueue queueModel;
    private RPPoolManager pmModel;

    /** the configuration object for this POA instance */
    private org.jacorb.config.Configuration configuration = null;
    private Logger logger;
    private boolean doMonitor;

    private String prefix;

    public void changeState(String state)
    {
    }

    public void closeMonitor()
    {
    }


    public void init(POA poa, AOM aom, RequestQueue queue, RPPoolManager pm,
                     String _prefix)
    {
        poaModel = poa;
        aomModel = aom;
        queueModel = queue;
        pmModel = pm;
        prefix = _prefix;
    }


    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getLogger("jacorb.poa.monitor");
        doMonitor =
            configuration.getAttributeAsBoolean("jacorb.poa.monitoring",false);
    }

    public void openMonitor()
    {
        if ( doMonitor )
        {
            try
            {
                POAMonitor newMonitor =
                    (POAMonitor)ObjectUtil.classForName("org.jacorb.poa.POAMonitorImpl").newInstance();
                newMonitor.init(poaModel, aomModel, queueModel, pmModel, prefix );
                newMonitor.configure(configuration);
                poaModel.setMonitor(newMonitor);
                newMonitor.openMonitor();
            }
            catch (Throwable exception)
            {
                if (logger.isWarnEnabled())
                {
                    logger.warn("Exception during openMonitor() of POAMonitorLightImpl" +
                                exception.getMessage());
                }
            }
        }
    }
}
