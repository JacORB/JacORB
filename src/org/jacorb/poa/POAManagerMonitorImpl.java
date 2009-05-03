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
import org.jacorb.poa.gui.POAManagerMonitorController;
import org.jacorb.poa.gui.POAManagerMonitorView;


/**
 * This class extends the POAManager with a monitoring gui for interacting
 * with the POAManager and start and stop poa monitoring.
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version 1.05, 12/08/99, RT
 */

public class POAManagerMonitorImpl
    implements POAManagerMonitor, POAManagerMonitorController, Configurable
{
    private POAManager model = null;
    private POAManagerMonitorView view = null;

    /** the configuration object for this POA instance */
    private Configuration configuration = null;
    private Logger logger;

    public void configure(Configuration myConfiguration)
        throws ConfigurationException
    {
        this.configuration = (org.jacorb.config.Configuration)myConfiguration;
        logger = configuration.getLogger("jacorb.poa.manager_monitor");
    }


    public void actionClosePOAMonitor(String name) 
    {
        if (model != null) 
        {
            try 
            {
                printMessage("invoke \"closeMonitor()\" on POAMonitor "+name);
                model.getRegisteredPOA(reducePOAName(name)).getMonitor().closeMonitor();
                printMessage("closeMonitor() on POAMonitor "+name+" is returned successfully");
            }
            catch (Throwable e) 
            {
                printMessage("exception ocurred: "+e);
            }
        }
    }

    public void actionCloseView() 
    {
        closeMonitor();
    }

    public void actionDestroyPOA(String name) 
    {
        if (model != null) 
        {
            try 
            {
                printMessage("invoke \"destroy()\" on POA "+name);
                model.getRegisteredPOA(reducePOAName(name)).destroy(true, true);
                printMessage("destroy() on POA "+name+" is returned successfully");
            } 
            catch (Throwable e) 
            {
                printMessage("exception ocurred: "+e);
            }
        }
    }
    public void actionOpenPOAMonitor(String name) {
        if (model != null) {
            try {
                printMessage("invoke \"openMonitor()\" on POAMonitor "+name);
                model.getRegisteredPOA(reducePOAName(name)).getMonitor().openMonitor();
                printMessage("openMonitor() on POAMonitor "+name+" is returned successfully");
            } catch (Throwable e) {
                printMessage("exception ocurred: "+e);
            }
        }
    }
    public void actionSetToActive() {
        if (model != null) {
            try {
                printMessage("invoke \"activate()\" on POAManager");
                model.activate();
                printMessage("activate() on POAManager is returned successfully");
            } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive e) {
                printMessage("exception ocurred: "+e);
                resetState();
            }
        }
    }
    public void actionSetToDiscarding(boolean wait) {
        if (model != null) {
            try {
                printMessage("invoke \"discard_requests("+wait+")\" on POAManager");
                model.discard_requests(wait);
                printMessage("discard_requests("+wait+") on POAManager is returned successfully");
            } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive e) {
                printMessage("exception ocurred: "+e);
                resetState();
            }
        }
    }
    public void actionSetToHolding(boolean wait) {
        if (model != null) {
            try {
                printMessage("invoke \"hold_requests("+wait+")\" on POAManager");
                model.hold_requests(wait);
                printMessage("hold_requests("+wait+") on POAManager is returned successfully");
            } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive e) {
                printMessage("exception ocurred: "+e);
                resetState();
            }
        }
    }
    public void actionSetToInactive(boolean wait, boolean etherialize) 
    {
        if (model != null) 
        {
            try {
                printMessage("invoke \"deactivate("+etherialize+", "+wait+")\" on POAManager");
                model.deactivate(etherialize, wait);
                printMessage("deactivate("+etherialize+", "+wait+") on POAManager is returned successfully");
            } catch (org.omg.PortableServer.POAManagerPackage.AdapterInactive e) {
                printMessage("exception ocurred: "+e);
                resetState();
            }
        }
    }

    public synchronized void addPOA(String name) 
    {
        if (view != null) 
        {
            try 
            {
                view._addPOA(expandPOAName(name));
            } 
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception in addPOA()" + exception.getMessage());
                }
            }
            printMessage("register POA "+name);
        }
    }

    public synchronized void closeMonitor() 
    {
        if (view != null) 
        {
            try 
            {
                POAManagerMonitor newMonitor = 
                    (POAManagerMonitor)Class.forName("org.jacorb.poa.POAManagerMonitorImpl").newInstance();
                newMonitor.init(model);
                newMonitor.configure(configuration);
                model.setMonitor(newMonitor);
                POAManagerMonitorView tmp = view;
                view = null;
                tmp._destroy();
            } 
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception in closeMonitor" + exception.getMessage());
                }
            }
        }
    }
    private String expandPOAName(String name) 
    {
        return name.equals("") ?
            POAConstants.ROOT_POA_NAME :
            POAConstants.ROOT_POA_NAME+POAConstants.OBJECT_KEY_SEPARATOR+name;
    }

    public void init(POAManager poaManager) 
    {
        model = poaManager;
    }

    public synchronized void openMonitor() 
    {
        try 
        {
            view = new org.jacorb.poa.gui.pm.POAManagerFrame(this);
            view._setVisible(true);
        } 
        catch (Throwable exception) 
        {
            if (logger.isErrorEnabled())
            {
                logger.error( "Exception in openMonitor" + exception.getMessage());
            }            
        }
    }

    public synchronized void printMessage(String str) 
    {
        if (view != null) {
            try 
            {
                view._printMessage(str);
            } 
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception int printMessage(): " + exception.getMessage());
                } 
            }
        }
    }

    private String reducePOAName(String name) 
    {
        return name.equals(POAConstants.ROOT_POA_NAME) ? 
            "" : 
            name.substring(POAConstants.ROOT_POA_NAME.length()+1);
    }

    public synchronized void removePOA(String name) 
    {
        if (view != null) 
        {
            try 
            {
                view._removePOA(expandPOAName(name));
            } 
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception in printMessage(): " + exception.getMessage());
                } 
            }
            printMessage("unregister POA "+name);
        }
    }

    protected synchronized  void resetState() {
        if (view != null) 
        {
            try 
            {
                view._resetState();
            } 
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception in resetState(): " + exception.getMessage());
                } 
            }
        }
    }
    public synchronized void setToActive() 
    {
        if (view != null) 
        {
            try 
            {
                view._setToActive();
            } 
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception in setToActive(): " + exception.getMessage());
                } 
            }
            printMessage("POAManager is set to \"active\"");
        }
    }

    public synchronized void setToDiscarding(boolean wait) 
    {
        if (view != null) 
        {
            try 
            {
                view._setToDiscarding(wait);
            } 
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception in setToActive(): " + exception.getMessage());
                } 
            }
            printMessage("POAManager is set to \"discarding\"");
        }
    }

    public synchronized void setToHolding(boolean wait) 
    {
        if (view != null) 
        {
            try 
            {
                view._setToHolding(wait);
            } 
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception in setToHolding(): " + exception.getMessage());
                } 
            }
            printMessage("POAManager is set to \"holding\"");
        }
    }
    public synchronized void setToInactive(boolean wait, boolean etherialize)
    {
        if (view != null) 
        {
            try 
            {
                view._setToInactive(wait, etherialize);
            }
            catch (Throwable exception) 
            {
                if (logger.isErrorEnabled())
                {
                    logger.error( "Exception in setToInactive(): " + exception.getMessage());
                } 
            }
            printMessage("POAManager is set to \"inactive\"");
        }
    }
}
