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

package org.jacorb.test.notification.typed;

import junit.framework.Test;

import org.jacorb.test.common.IFRTestSetup;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Repository;
import org.omg.CORBA.RepositoryHelper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class NotifyIFRTestSetup extends IFRTestSetup
{
    private final static ORB orb = ORB.init(new String[0], null);
    
    public NotifyIFRTestSetup(Test test)
    {
        super(test);
    }
    
    public void setUp() throws Exception
    {
        super.setUp();
        
        Thread.sleep(1000);
        
        feedToIFR("~/JacORB/test/regression/idl/TypedNotification.idl");
    }
    
    public Repository getRepository() throws Exception
    {
        return RepositoryHelper.narrow(orb.string_to_object(getIFRRef()));
    }
    
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
}
