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

package org.jacorb.test.notification.node;

import org.jacorb.notification.filter.etcl.AbstractTCLNode;
import org.jacorb.notification.filter.etcl.TCLParser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class IdentValueTest extends TestCase
{
    public void testIsStatic() throws Exception
    {
        AbstractTCLNode node = TCLParser.parse("'identifier'");

        assertTrue(node.isStatic());
    }

    public static Test suite()
    {
        return new TestSuite(IdentValueTest.class);
    }
}