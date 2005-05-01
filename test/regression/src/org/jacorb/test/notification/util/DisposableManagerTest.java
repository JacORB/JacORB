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

package org.jacorb.test.notification.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.easymock.MockControl;
import org.jacorb.notification.interfaces.Disposable;
import org.jacorb.notification.util.DisposableManager;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class DisposableManagerTest extends TestCase
{
    private DisposableManager objectUnderTest_;

    private MockControl controlDisposable_;

    private Disposable mockDisposable_;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();

        objectUnderTest_ = new DisposableManager();
        controlDisposable_ = MockControl.createControl(Disposable.class);
        mockDisposable_ = (Disposable) controlDisposable_.getMock();
    }

    /**
     * Constructor for DisposableManagerTest.
     * 
     * @param name
     */
    public DisposableManagerTest(String name)
    {
        super(name);
    }

    public void testAddDisposable()
    {
        controlDisposable_.replay();
        objectUnderTest_.addDisposable(mockDisposable_);
        controlDisposable_.verify();
    }

    public void testDispose()
    {
        mockDisposable_.dispose();
        controlDisposable_.replay();
        objectUnderTest_.addDisposable(mockDisposable_);
        objectUnderTest_.dispose();
        controlDisposable_.verify();
    }

    public void testDisposeIsDelegatedOnceOnly()
    {
        mockDisposable_.dispose();
        controlDisposable_.replay();
        objectUnderTest_.addDisposable(mockDisposable_);
        objectUnderTest_.dispose();
        objectUnderTest_.dispose();
        controlDisposable_.verify();
    }

    public static Test suite()
    {
        return new TestSuite(DisposableManagerTest.class);
    }
}