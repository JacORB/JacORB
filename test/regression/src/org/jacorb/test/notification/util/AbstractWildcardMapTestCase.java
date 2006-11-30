package org.jacorb.test.notification.util;

import junit.framework.TestCase;

import org.jacorb.notification.util.WildcardMap;

/**
 * @author Alphonse Bendt
 */
public abstract class AbstractWildcardMapTestCase extends TestCase
{
    protected WildcardMap objectUnderTest_;

    ////////////////////////////////////////

    public void setUp() throws Exception
    {
        objectUnderTest_ = newWildcardMap();
    }

    abstract WildcardMap newWildcardMap();

    public void testToString()
    {
        assertNotNull(objectUnderTest_.toString());
    }
    
    public void testBugInsertAfterSplit() throws Exception
    {
        objectUnderTest_.put("abcd", "ABCD");
        objectUnderTest_.put("abef", "ABEF");
        objectUnderTest_.put("ab", "AB");

        assertEquals("ABCD", (objectUnderTest_.getWithExpansion("abcd"))[0]);
        assertEquals("AB", (objectUnderTest_.getWithExpansion("ab"))[0]);
        assertEquals("ABEF", (objectUnderTest_.getWithExpansion("abef"))[0]);
    }

    public void testRemove() throws Exception
    {
        objectUnderTest_.put("hallo", "Hallo");
        objectUnderTest_.put("hallo2", "Hallo2");
        objectUnderTest_.put("hallo3", "Hallo3");
        objectUnderTest_.put("hallo4", "Hallo4");

        Object _removed = objectUnderTest_.remove("hallo2");
        assertEquals("Hallo2", _removed);

        Object[] _result = objectUnderTest_.getWithExpansion("hallo2");

        assertEquals(0, _result.length);
    }

    public void testClear() throws Exception
    {
        assertEquals(0, objectUnderTest_.getWithExpansion("hallo").length);
    
        objectUnderTest_.put("hallo", "Hallo");
        objectUnderTest_.put("hallo*", "Hallo2");
        
        assertEquals(2, objectUnderTest_.getWithExpansion("hallo").length);
        
        objectUnderTest_.clear();
        
        assertEquals(0, objectUnderTest_.getWithExpansion("hallo").length);
    }
    
    public void testAddStar1() throws Exception
    {
        objectUnderTest_.put("ha*o", "default");
        Object[] _result = objectUnderTest_.getWithExpansion("hallo");
        assertTrue(_result.length == 1);

        _result = objectUnderTest_.getWithExpansion("hall");
        assertTrue(_result.length == 0);

        _result = objectUnderTest_.getWithExpansion("hao");
        assertTrue(_result.length == 1);

        _result = objectUnderTest_.getWithExpansion("hadddddo");
        assertTrue(_result.length == 1);
    }

    public void testAddStar2() throws Exception
    {
        objectUnderTest_.put("*abc*de", "value");
        Object[] _result = objectUnderTest_.getWithExpansion("abcde");
        assertTrue(_result.length == 1);

        _result = objectUnderTest_.getWithExpansion("halloabcde");
        assertTrue(_result.length == 1);

        _result = objectUnderTest_.getWithExpansion("abcbla bla blade");
        assertTrue(_result.length == 1);

        _result = objectUnderTest_.getWithExpansion("abcde");
        assertTrue(_result.length == 1);

        _result = objectUnderTest_.getWithExpansion("ab cde");
        assertEquals(0, _result.length);
    }

    public void testAddStar() throws Exception
    {
        objectUnderTest_.put("abc*", "value1");
        objectUnderTest_.put("abcd", "value2");

        Object[] _result = objectUnderTest_.getWithExpansion("abc");
        assertEquals(1, _result.length);
        assertEquals("value1", _result[0]);

        _result = objectUnderTest_.getWithExpansion("abcd");
        assertEquals(2, _result.length);
        assertTrue("value1".equals(_result[0]) || "value1".equals(_result[1]));
        assertTrue("value2".equals(_result[0]) || "value2".equals(_result[1]));
    }

    public void testSplitAfterStar() throws Exception
    {
        objectUnderTest_.put("abc*def", "value1");
        objectUnderTest_.put("abc*ef", "value2");
        objectUnderTest_.put("abc", "value3");

        Object[] _result = objectUnderTest_.getWithExpansion("abcxyzdef");
        assertEquals(2, _result.length);

        _result = objectUnderTest_.getWithExpansion("abcxyzef");
        assertEquals(1, _result.length);

        _result = objectUnderTest_.getWithExpansion("abc");
        assertEquals(1, _result.length);
    }

    public void testExactGet() throws Exception
    {
        objectUnderTest_.put("name1", "value1");
        objectUnderTest_.put("name2", "value2");
        objectUnderTest_.put("nane1", "value3");
        objectUnderTest_.put("nane2", "value4");
        objectUnderTest_.put("na*", "value5");
        objectUnderTest_.put("na*e1", "value6");

        assertEquals("value1", objectUnderTest_.getNoExpansion("name1"));
        assertEquals("value2", objectUnderTest_.getNoExpansion("name2"));
        assertEquals("value3", objectUnderTest_.getNoExpansion("nane1"));
        assertEquals("value4", objectUnderTest_.getNoExpansion("nane2"));
        assertEquals("value5", objectUnderTest_.getNoExpansion("na*"));
        assertEquals("value6", objectUnderTest_.getNoExpansion("na*e1"));
    }

    public void testAdd() throws Exception
    {
        objectUnderTest_.put("name", "wert");
        objectUnderTest_.put("java", "Programming Language");

        Object _oldValue = objectUnderTest_.put("name", "neuer wert");

        assertEquals("wert", _oldValue);

        Object[] _value1 = objectUnderTest_.getWithExpansion("name");
        Object[] _value2 = objectUnderTest_.getWithExpansion("java");

        assertEquals(1, _value1.length);
        assertEquals("neuer wert", _value1[0]);

        assertEquals(1, _value2.length);
        assertEquals("Programming Language", _value2[0]);
    }
}

