package org.jacorb.test.notification.util;

import junit.framework.TestCase;

import org.jacorb.notification.util.WildcardMap;

/**
 * @author Alphonse Bendt
 */
public abstract class AbstractWildcardMapTest extends TestCase
{
    protected WildcardMap objectUnderTest_;

    ////////////////////////////////////////

    public AbstractWildcardMapTest(String name)
    {
        super(name);
    }

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

        Object o = objectUnderTest_.remove("hallo2");
        assertEquals("Hallo2", o);

        Object[] l = objectUnderTest_.getWithExpansion("hallo2");

        assertEquals(0, l.length);
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
        Object[] _res = objectUnderTest_.getWithExpansion("hallo");
        assertTrue(_res.length == 1);

        _res = objectUnderTest_.getWithExpansion("hall");
        assertTrue(_res.length == 0);

        _res = objectUnderTest_.getWithExpansion("hao");
        assertTrue(_res.length == 1);

        _res = objectUnderTest_.getWithExpansion("hadddddo");
        assertTrue(_res.length == 1);
    }

    public void testAddStar2() throws Exception
    {
        objectUnderTest_.put("*abc*de", "value");
        Object[] _res = objectUnderTest_.getWithExpansion("abcde");
        assertTrue(_res.length == 1);

        _res = objectUnderTest_.getWithExpansion("halloabcde");
        assertTrue(_res.length == 1);

        _res = objectUnderTest_.getWithExpansion("abcbla bla blade");
        assertTrue(_res.length == 1);

        _res = objectUnderTest_.getWithExpansion("abcde");
        assertTrue(_res.length == 1);

        _res = objectUnderTest_.getWithExpansion("ab cde");
        assertEquals(0, _res.length);
    }

    public void testAddStar() throws Exception
    {
        objectUnderTest_.put("abc*", "value1");
        objectUnderTest_.put("abcd", "value2");

        Object[] _res = objectUnderTest_.getWithExpansion("abc");
        assertEquals(1, _res.length);
        assertEquals("value1", _res[0]);

        _res = objectUnderTest_.getWithExpansion("abcd");
        assertEquals(2, _res.length);
        assertTrue("value1".equals(_res[0]) || "value1".equals(_res[1]));
        assertTrue("value2".equals(_res[0]) || "value2".equals(_res[1]));
    }

    public void testSplitAfterStar() throws Exception
    {
        objectUnderTest_.put("abc*def", "value1");
        objectUnderTest_.put("abc*ef", "value2");
        objectUnderTest_.put("abc", "value3");

        Object[] _res = objectUnderTest_.getWithExpansion("abcxyzdef");
        assertEquals(2, _res.length);

        _res = objectUnderTest_.getWithExpansion("abcxyzef");
        assertEquals(1, _res.length);

        _res = objectUnderTest_.getWithExpansion("abc");
        assertEquals(1, _res.length);
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

        Object _old = objectUnderTest_.put("name", "neuer wert");

        assertEquals("wert", _old);

        Object[] o1 = objectUnderTest_.getWithExpansion("name");
        Object[] o2 = objectUnderTest_.getWithExpansion("java");

        assertEquals(1, o1.length);
        assertEquals("neuer wert", o1[0]);

        assertEquals(1, o2.length);
        assertEquals("Programming Language", o2[0]);
    }
}

