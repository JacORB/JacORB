package org.jacorb.poa;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
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

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.util.ObjectUtil;
import org.slf4j.Logger;


/**
 * A lightweight implementation of a POAManager monitor
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.03, 12/08/99, RT
 */

public class POAManagerMonitorLightImpl
    implements POAManagerMonitor, Configurable
{
    private POAManager model = null;

    private org.jacorb.config.Configuration configuration = null;
    private Logger logger;
    private boolean doMonitor;

    public void addPOA(String name)
    {
    }

    public void closeMonitor() {
    }

    public void init(POAManager poaManager)
    {
        model = poaManager;
    }

    public void configure(Configuration myConfiguration)
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;

        logger = configuration.getLogger("jacorb.poa.manager_monitor");
        doMonitor = configuration.getAttributeAsBoolean("jacorb.poa.monitoring",false);
    }

    public void openMonitor()
    {
        if ( doMonitor )
        {
            try
            {
                POAManagerMonitor newMonitor =
                    (POAManagerMonitor)ObjectUtil.classForName("org.jacorb.poa.POAManagerMonitorImpl").newInstance();
                newMonitor.init(model);
                newMonitor.configure(configuration);
                model.setMonitor(newMonitor);
                newMonitor.openMonitor();
            }
            catch (Throwable exception)
            {
                if (logger.isErrorEnabled())
                    logger.error( "Exception in closeMonitor(): " + exception.getMessage());
            }
        }
    }
    public void printMessage(String str) {
    }
    public void removePOA(String name) {
    }
    public void setToActive() {
    }
    public void setToDiscarding(boolean wait) {
    }
    public void setToHolding(boolean wait) {
    }
    public void setToInactive(boolean wait, boolean etherialize) {
    }
}







