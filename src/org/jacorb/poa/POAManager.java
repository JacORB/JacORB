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

import java.util.Enumeration;
import java.util.Vector;
import org.omg.CORBA.INTERNAL;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAManagerPackage.State;

/**
 * The poa manager class, an implementation of org.omg.PortableServer.POAManager
 *
 * @author Reimo Tiedemann, FU Berlin
 * @version $Id$
 */

public class POAManager
    extends org.omg.PortableServer._POAManagerLocalBase
{
    public State state = State.HOLDING;
    private org.jacorb.orb.ORB orb;
    private Vector poas = new Vector();
    private POAManagerMonitor monitor;
    protected boolean poaCreationFailed;


    protected POAManager(org.jacorb.orb.ORB _orb)
    {
        orb = _orb;
        monitor = new POAManagerMonitorLightImpl();
        monitor.init(this);
        monitor.openMonitor();
        monitor.printMessage("ready");
    }


    public void activate() throws AdapterInactive
    {
        checkCreation ();

        switch (state.value())
        {
            case State._INACTIVE :
            throw new AdapterInactive();
            case State._ACTIVE :
            break;
            default:
            state = State.ACTIVE;
            monitor.setToActive();

            final POA [] poaArray;

            synchronized (this)
            {
                poaArray = new POA[poas.size()];
                poas.copyInto(poaArray);
            }
            // notify all registered poas
            Thread thread = new Thread()
            {
                public void run()
                {
                    for (int i=0; i<poaArray.length; i++)
                    {
                        try {
                            poaArray[i].changeToActive();
                        } catch (Throwable e) {}
                    }
                }
            };
            thread.start();
        }
    }


    public void deactivate
        (boolean etherealize_objects, boolean wait_for_completion)
        throws AdapterInactive
    {
        checkCreation ();

        if (wait_for_completion && isInInvocationContext())
        {
            throw new org.omg.CORBA.BAD_INV_ORDER();
        }

        switch (state.value())
        {
            case State._INACTIVE :
            throw new AdapterInactive();
            default :
            state = State.INACTIVE;
            monitor.setToInactive(wait_for_completion, etherealize_objects);

            final boolean etherealize = etherealize_objects;
            final POA [] poaArray;

            synchronized (this)
            {
                poaArray = new POA[poas.size()];
                poas.copyInto(poaArray);
            }
            // notify all registered poas
            Thread thread = new Thread()
            {
                public void run()
                {
                    for (int i=poaArray.length-1; i>=0; i--)
                    {
                        try {
                            poaArray[i].changeToInactive(etherealize);
                        } catch (Throwable e) {}
                    }
                }
            };
            thread.start();
            if (wait_for_completion)
            {
                try {
                    thread.join();
                } catch (InterruptedException e) {}
            }
        }
    }


    public void discard_requests(boolean wait_for_completion)
        throws AdapterInactive
    {
        checkCreation ();

        if (wait_for_completion && isInInvocationContext())
        {
            throw new org.omg.CORBA.BAD_INV_ORDER();
        }

        switch (state.value())
        {
            case State._INACTIVE :
            throw new AdapterInactive();
            case State._DISCARDING :
            break;
            default :
            state = State.DISCARDING;
            monitor.setToDiscarding(wait_for_completion);

            final POA [] poaArray;

            synchronized (this)
            {
                poaArray = new POA[poas.size()];
                poas.copyInto(poaArray);
            }
            // notify all registered poas
            Thread thread = new Thread()
            {
                public void run()
                {
                    for (int i=poaArray.length-1; i>=0; i--)
                    {
                        try {
                            poaArray[i].changeToDiscarding();
                        } catch (Throwable e) {}
                    }
                }
            };
            thread.start();
            if (wait_for_completion)
            {
                try {
                    thread.join();
                } catch (InterruptedException e) {}
            }
        }
    }


    public State get_state()
    {
        return state;
    }


    protected synchronized POA getRegisteredPOA(String name)
    {
        POA result;
        Enumeration en = poas.elements();
        while (en.hasMoreElements())
        {
            result = (POA) en.nextElement();
            if (name.equals(result._getQualifiedName()))
            {
                return result;
            }
        }
        throw new INTERNAL
        (
            "POA not registered: " +
            POAConstants.ROOT_POA_NAME+
            POAConstants.OBJECT_KEY_SEPARATOR+
            name
        );
    }


    public void hold_requests(boolean wait_for_completion)
        throws AdapterInactive
    {
        checkCreation ();

        if (wait_for_completion && isInInvocationContext())
        {
            throw new org.omg.CORBA.BAD_INV_ORDER();
        }
        switch (state.value())
        {
            case State._INACTIVE :
            throw new AdapterInactive();
            case State._HOLDING :
            break;
            default :
            state = State.HOLDING;
            monitor.setToHolding(wait_for_completion);

            final POA [] poaArray;

            synchronized (this)
            {
                poaArray = new POA[poas.size()];
                poas.copyInto(poaArray);
            }
            // notify all registered poas
            Thread thread = new Thread()
            {
                public void run()
                {
                    for (int i=poaArray.length-1; i>=0; i--)
                    {
                        try {
                            poaArray[i].changeToHolding();
                        } catch (Throwable e) {}
                    }
                }
            };
            thread.start();
            if (wait_for_completion)
            {
                try {
                    thread.join();
                } catch (InterruptedException e) {}
            }
        }
    }


    /**
     * it returns true if the current thread is not in an invocation
     * context dispatched by some POA belonging to the same ORB as this POAManager.
     */
    private boolean isInInvocationContext()
    {
        try {
            if (orb.getPOACurrent().getORB() == orb) return true;

        } catch (org.omg.PortableServer.CurrentPackage.NoContext e) {}
        return false;
    }


    protected synchronized void registerPOA(POA poa)
    {
        if (!poas.contains(poa))
        {
            poas.addElement(poa);
            monitor.addPOA(poa._getQualifiedName());
        }
    }


    protected void setMonitor(POAManagerMonitor _monitor)
    {
        monitor = _monitor;
    }


    protected synchronized void unregisterPOA(POA poa)
    {
        poas.removeElement(poa);
        monitor.removePOA(poa._getQualifiedName());
    }


    private void checkCreation ()
    {
        if (poaCreationFailed)
        {
            throw new org.omg.CORBA.INTERNAL ("POA Creation failed; unable to deactive");
        }
    }
}
