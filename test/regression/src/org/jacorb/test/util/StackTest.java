package org.jacorb.test.util;

import java.util.EmptyStackException;

import junit.framework.TestCase;

import org.jacorb.util.Stack;

/**
 * @author Alphonse Bendt
 * @version $Id$
 */
public class StackTest extends TestCase
{
    private final Stack objectUnderTest = new Stack();

    private final Integer value1 = new Integer(1);
    private final Integer value2 = new Integer(2);

    public void testIsEmpty()
    {
        assertTrue(objectUnderTest.empty());
    }

    public void testPush()
    {
        assertEquals(value1, objectUnderTest.push(value1));

        assertFalse(objectUnderTest.empty());
    }

    public void testPop()
    {
        objectUnderTest.push(value1);

        assertFalse(objectUnderTest.empty());

        assertEquals(value1, objectUnderTest.pop());

        assertTrue(objectUnderTest.empty());
    }

    public void testPopEmpty()
    {
        try
        {
            objectUnderTest.pop();
        }
        catch (EmptyStackException e)
        {
            // ok
        }
    }

    public void testPeek()
    {
        objectUnderTest.push(value1);

        assertEquals(value1, objectUnderTest.peek());

        assertFalse(objectUnderTest.empty());
    }

    public void testSearch()
    {
        java.util.Stack jdkStack = new java.util.Stack();

        assertEquals(jdkStack.search(value1), objectUnderTest.search(value1));

        jdkStack.push(value1);
        jdkStack.push(value2);
        jdkStack.push(value1);
        objectUnderTest.push(value1);
        objectUnderTest.push(value2);
        objectUnderTest.push(value1);

        assertEquals(jdkStack.search(value1), objectUnderTest.search(value1));
        assertEquals(jdkStack.search(value2), objectUnderTest.search(value2));
    }
}
