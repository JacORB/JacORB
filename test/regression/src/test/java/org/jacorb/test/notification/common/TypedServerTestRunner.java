/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

package org.jacorb.test.notification.common;

import org.jacorb.notification.AbstractChannelFactory;
import org.jacorb.notification.ConsoleMain;

/**
 * testrunner for TypedEventChannel integration tests.
 * this runner will start a TypedEventChannelFactory and
 * print its IOR to stdout.
 * 
 * @author Alphonse Bendt
 */
public class TypedServerTestRunner
{
    public static void main(String[] args) throws Exception
    {
        AbstractChannelFactory factory = ConsoleMain.newFactory(new String[] {"-typed"});
        
        System.out.println("SERVER IOR: " + factory.getIOR());
    }
}
