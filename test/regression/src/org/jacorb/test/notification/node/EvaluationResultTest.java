package org.jacorb.test.notification.node;

/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2003 Gerald Brose
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jacorb.notification.filter.EvaluationResult;

/**
 * @author Alphonse Bendt
 */

public class EvaluationResultTest extends TestCase
{
    private EvaluationResult objectUnderTest_;

    public void setUp() throws Exception
    {
        objectUnderTest_ = new EvaluationResult();
    }

    public void testPlus() throws Exception
    {
        EvaluationResult a, b;
        a = new EvaluationResult();
        b = new EvaluationResult();

        a.setLong(10);
        b.setLong(10);

        EvaluationResult c = EvaluationResult.plus(a, b);
        assertTrue(20 == c.getLong());

        a.setFloat(10);
        b.setFloat(10);

        assertTrue(a.isFloat());
        assertTrue(b.isFloat());

        c = EvaluationResult.plus(a, b);
        assertTrue(20 == c.getFloat());
        assertTrue(c.isFloat());
    }

    public void testSetString() throws Exception
    {
        objectUnderTest_.setString("hallo");
        assertEquals("hallo", objectUnderTest_.getString());
    }

    public void testSetInt() throws Exception
    {
        objectUnderTest_.setLong(1);
        assertTrue(objectUnderTest_.getLong() == 1);
    }

    public void testSetFloat() throws Exception
    {
        objectUnderTest_.setFloat(2f);
        assertTrue(objectUnderTest_.getFloat() == 2f);
    }

    public void testSetBoolean() throws Exception
    {
        objectUnderTest_ = EvaluationResult.BOOL_TRUE;
        assertTrue(objectUnderTest_.getLong() == 1);
        assertTrue(objectUnderTest_.getFloat() == 1f);
        assertTrue(objectUnderTest_.getBool());

        objectUnderTest_ = EvaluationResult.BOOL_FALSE;
        assertTrue(objectUnderTest_.getLong() == 0);
        assertTrue(objectUnderTest_.getFloat() == 0f);
        assertTrue(!objectUnderTest_.getBool());
    }

    public void testBug790() throws Exception
    {
        EvaluationResult other1 = new EvaluationResult();
        EvaluationResult other2 = new EvaluationResult();
        objectUnderTest_.setLongLong(1000);
        other1.setLongLong(2000);
        other2.setLongLong(1000);

        assertTrue(objectUnderTest_ + " < " + other1, objectUnderTest_.compareTo(other1) < 0);
        assertTrue(other1.compareTo(objectUnderTest_) > 0);
        assertEquals(0, objectUnderTest_.compareTo(other2));
        assertEquals(0, other2.compareTo(objectUnderTest_));
    }


    public static Test suite()
    {
        return new TestSuite(EvaluationResultTest.class);
    }
}