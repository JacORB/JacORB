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
 *  Unit Test for class EvaluationResult
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class EvaluationResultTest extends TestCase {

    EvaluationResult evaluationResult_;

    public void setUp() throws Exception {
        evaluationResult_ = new EvaluationResult();
    }

    public void testPlus() throws Exception {
        EvaluationResult a, b;
        a = new EvaluationResult();
        b = new EvaluationResult();

        a.setLong(10);
        b.setLong(10);

        EvaluationResult c = EvaluationResult.plus(a,b);
        assertTrue(20 == c.getLong());

        a.setFloat(10);
        b.setFloat(10);

        assertTrue(a.isFloat());
        assertTrue(b.isFloat());

        c = EvaluationResult.plus(a,b);
        assertTrue(20 == c.getFloat());
        assertTrue(c.isFloat());
    }

    public void testSetString() throws Exception {
        evaluationResult_.setString("hallo");
    }

    public void testSetInt() throws Exception {
        evaluationResult_.setLong(1);
        assertTrue(evaluationResult_.getLong() == 1);
    }

    public void testSetFloat() throws Exception {
        evaluationResult_.setFloat(2f);
        assertTrue(evaluationResult_.getFloat() == 2f);
    }

    public void testSetBoolean() throws Exception {
        evaluationResult_ = EvaluationResult.BOOL_TRUE;
        assertTrue(evaluationResult_.getLong() == 1);
        assertTrue(evaluationResult_.getFloat() == 1f);
        assertTrue(evaluationResult_.getBool());

        evaluationResult_ = EvaluationResult.BOOL_FALSE;
        assertTrue(evaluationResult_.getLong() == 0);
        assertTrue(evaluationResult_.getFloat() == 0f);
        assertTrue(!evaluationResult_.getBool());
    }

    /**
     * Creates a new <code>EvaluationResultTest</code> instance.
     *
     * @param name test name
     */
    public EvaluationResultTest(String name){
        super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(EvaluationResultTest.class);

        return suite;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

}
