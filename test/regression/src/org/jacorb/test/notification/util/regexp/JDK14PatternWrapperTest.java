package org.jacorb.test.notification.util.regexp;

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

import junit.framework.TestSuite;
import org.jacorb.notification.util.JDK14PatternWrapper;
import org.jacorb.notification.util.PatternWrapper;

/**
 * @author Alphonse Bendt
 */
public class JDK14PatternWrapperTest extends AbstractPatternWrapperTestCase
{
    public PatternWrapper newPattern()
    {
        return new JDK14PatternWrapper();
    }

    public static TestSuite suite()
    {
        return new TestSuite(JDK14PatternWrapperTest.class);
    }
}
