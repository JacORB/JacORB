package org.jacorb.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.EmptyStackException;
import org.jacorb.util.Stack;
import org.junit.Test;

/**
 * @author Alphonse Bendt
 */
public class StackTest
{
    private final Stack objectUnderTest = new Stack();

    private final Integer value1 = new Integer(1);
    private final Integer value2 = new Integer(2);

    @Test
    public void testIsEmpty()
    {
        assertTrue(objectUnderTest.empty());
    }

    @Test
    public void testPush()
    {
        assertEquals(value1, objectUnderTest.push(value1));

        assertFalse(objectUnderTest.empty());
    }

    @Test
    public void testPop()
    {
        objectUnderTest.push(value1);

        assertFalse(objectUnderTest.empty());

        assertEquals(value1, objectUnderTest.pop());

        assertTrue(objectUnderTest.empty());
    }

    @Test
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

    @Test
    public void testPeek()
    {
        objectUnderTest.push(value1);

        assertEquals(value1, objectUnderTest.peek());

        assertFalse(objectUnderTest.empty());
    }

    @Test
    public void testSearch()
    {
        java.util.Stack<Integer> jdkStack = new java.util.Stack<Integer>();

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
