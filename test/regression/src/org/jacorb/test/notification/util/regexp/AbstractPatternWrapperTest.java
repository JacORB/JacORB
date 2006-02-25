package org.jacorb.test.notification.util.regexp;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2003  Gerald Brose.
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

import junit.framework.TestCase;
import org.jacorb.notification.util.PatternWrapper;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */

public abstract class AbstractPatternWrapperTest extends TestCase
{
    protected PatternWrapper objectUnderTest_;

    public final void setUp()
    {
        objectUnderTest_ = newPattern();
    }

    protected abstract PatternWrapper newPattern();

    public void testNoMatch()
    {
        objectUnderTest_.compile("abc*d");

        String test = "xyzdef";

        assertEquals(0, objectUnderTest_.match(test));
    }

    public void testMatch()
    {
        objectUnderTest_.compile("abc*d");

        String test = "abcccccccd";

        int i = objectUnderTest_.match(test);

        assertEquals(10, i);
    }

    public void testToString()
    {
        String pattern = "a*b*d";

        objectUnderTest_.compile(pattern);

        assertEquals(pattern, objectUnderTest_.toString());
    }
}
