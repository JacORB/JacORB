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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.jacorb.util.ObjectUtil;


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
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getNamedLogger("jacorb.poa.manager_monitor");
        doMonitor = 
            configuration.getAttributeAsBoolean("jacorb.poa.monitoring",false);
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







