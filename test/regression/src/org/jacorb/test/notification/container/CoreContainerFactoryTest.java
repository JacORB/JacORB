/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

package org.jacorb.test.notification.container;

import org.apache.avalon.framework.configuration.Configuration;
import org.jacorb.notification.container.PicoContainerFactory;
import org.omg.CORBA.ORB;
import org.omg.CosNotifyFilter.FilterFactory;
import org.omg.PortableServer.POA;
import org.picocontainer.PicoContainer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class CoreContainerFactoryTest extends TestCase
{
    ORB orb_;

    PicoContainer picoContainer_;

    public void setUp() throws Exception
    {
        orb_ = ORB.init(new String[0], null);
        picoContainer_ = PicoContainerFactory.createRootContainer((org.jacorb.orb.ORB)orb_);
    }

    public void testGetORB()
    {
        ORB _orb = (ORB) picoContainer_.getComponentInstance(ORB.class);

        assertNotNull(_orb);
    }

    public void testGetPOA()
    {
        POA _poa = (POA) picoContainer_.getComponentInstance(POA.class);

        assertNotNull(_poa);
    }

    public void testGetConfiguration()
    {
        Configuration config = (Configuration) picoContainer_.getComponentInstance(Configuration.class);
        
        assertNotNull(config);
    }
    
    public void testGetFilterFactory()
    {
        FilterFactory filterFactory = (FilterFactory)picoContainer_.getComponentInstance(FilterFactory.class);
        
        assertNotNull(filterFactory);
    }
    
    public static Test suite()
    {
        return new TestSuite(CoreContainerFactoryTest.class);
    }
}