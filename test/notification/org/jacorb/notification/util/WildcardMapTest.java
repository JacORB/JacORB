package org.jacorb.notification.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Random;
import java.util.Map;
import java.util.Hashtable;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *  Unit Test for class WildcardMap
 *
 *
 * Created: Sun Oct 20 17:26:27 2002
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */
public class WildcardMapTest extends TestCase {
    WildcardMap map_;

    public void setUp() {
	map_ = new WildcardMap();
    }

    public void testBugInsertAfterSplit() throws Exception {
	map_.put("abcd", "ABCD");
	map_.put("abef", "ABEF");
	map_.put("ab", "AB");

	assertEquals("ABCD", ((Object[])map_.get("abcd"))[0]);
	assertEquals("AB", ((Object[])map_.get("ab"))[0]);
	assertEquals("ABEF", ((Object[])map_.get("abef"))[0]);
    }

    public void testPerformance1() throws Exception {
	long _wcTime = 0;
	long _hashTime = 0;
	int _iterations = 20;

	Level _level = Logger.getRootLogger().getLevel();
	Logger.getRootLogger().setLevel(Level.OFF);

	Random _random = new Random(System.currentTimeMillis());

	int _testSize = 100;
	
	Integer[] _testValues = new Integer[_testSize];
	String[] _testKeys = new String[_testSize];

	for (int x=0; x<_testSize; ++x) {
	    // only positive values
	    _testValues[x] = new Integer(_random.nextInt(Integer.MAX_VALUE));
	    _testKeys[x] = _testValues[x].toString();
	}

	for (int runs=0; runs<_iterations; ++runs) {
	    WildcardMap _wcMap = new WildcardMap();
   
	    long _start = System.currentTimeMillis();
	    for (int x=0; x<_testSize; x++) {
		_wcMap.put(_testKeys[x], _testValues[x]);
	    }
	    long _stop = System.currentTimeMillis();
	    
	    for (int x=0; x<_testSize; ++x) {
		assertEquals(_testValues[x],(((Object[])_wcMap.get(_testKeys[x]))[0]));
	    }
	    _wcTime += (_stop - _start);

	    Map _hashTable = new Hashtable();
	    _start = System.currentTimeMillis();
	    for (int x=0; x<_testSize; ++x) {
		_hashTable.put(_testKeys[x], _testValues[x]);
	    }
	    _stop = System.currentTimeMillis();

	    _hashTime += (_stop - _start);
	}

	Logger.getRootLogger().setLevel(_level);

	System.out.println("Measure time for multiple (" + _testSize + ") put Operations");	

	System.out.println("Total Time:");
	System.out.println("  Hashtable:\t" + _hashTime);
	System.out.println("  WildcardMap:\t" + _wcTime);

	System.out.println("Average Time:");
	System.out.println("  Hashtable:\t" + _hashTime / _iterations);
	System.out.println("  WildcardMap:\t" + _wcTime / _iterations);
    }

    public void testPerformance2() throws Exception {
	long _wcTime = 0;

	int testRuns = 10;
	int initialTestSize = 100;
	int totalTests = 10;

	Hashtable testData = new Hashtable();

	Level _level = Logger.getRootLogger().getLevel();
	Logger.getRootLogger().setLevel(Level.FATAL);

	System.out.println("Measure Average Time for one get Operation with various Map Sizes");

	Random _random = new Random(System.currentTimeMillis());

	for (int t = 0; t < totalTests; ++t) {
	
	    Integer[] _testValues = new Integer[initialTestSize];
	    String[] _testKeys = new String[initialTestSize];
	    for (int x=0; x<initialTestSize; ++x) {
		// only positive values
		_testValues[x] = new Integer(_random.nextInt(Integer.MAX_VALUE));
		_testKeys[x] = _testValues[x].toString();
		testData.put(_testValues[x], "YES");
	    }

	    WildcardMap _wcMap = new WildcardMap();

	    for (int x=0; x<initialTestSize; x++) {
		_wcMap.put(_testKeys[x], _testValues[x]);
	    }

	    _wcTime = wcMapGet(_wcMap, _testKeys, testRuns);

	    System.out.println("\tget() MapSize:" + (initialTestSize/10) + " Average Time:" + _wcTime);

	    initialTestSize *= 2;// testSizeIncrement;
	}
	Logger.getRootLogger().setLevel(_level);
    }

    long wcMapGet(WildcardMap map, Object[] keys, int runs) {
	long _start = System.currentTimeMillis();	    
	for (int r=0; r<runs; ++r) {
	    for (int x=0; x<keys.length/10; ++x) {
		map.get(keys[x*10]);
	    }
	}
	long _stop = System.currentTimeMillis();
	
	return (_stop - _start) / (keys.length / 10);
    }

    public void testRemove() throws Exception {
	map_.put("hallo", "Hallo");
	map_.put("hallo2", "Hallo2");
	map_.put("hallo3", "Hallo3");
	map_.put("hallo4", "Hallo4");

	System.out.println(map_);
	

	Object o = map_.remove("hallo2");
	assertEquals("Hallo2", o);

	System.out.println(map_);

	Object[] l = (Object[])map_.get("hallo2");

	assertTrue(l.length == 0);
    }

    public void testAddStar1() throws Exception {
	map_.put("ha*o", "default");
	Object[] _res = (Object[])map_.get("hallo");
	assertTrue(_res.length == 1);

	_res = (Object[])map_.get("hall");
	assertTrue(_res.length == 0);

	_res = (Object[])map_.get("hao");
	assertTrue(_res.length == 1);

	_res = (Object[])map_.get("hadddddo");
	assertTrue(_res.length == 1);
    }

    public void testAddStar2() throws Exception {
	map_.put("*abc*de", "value");
	Object[] _res = (Object[])map_.get("abcde");
	assertTrue(_res.length == 1);

	_res = (Object[])map_.get("halloabcde");
	assertTrue(_res.length == 1);
	
	_res = (Object[])map_.get("abcbla bla blade");
	assertTrue(_res.length == 1);

	_res = (Object[])map_.get("abcde");
	assertTrue(_res.length == 1);
    }

    public void testAddStar() throws Exception {
	map_.put("abc*", "value1");
	map_.put("abcd", "value2");
	
	System.out.println(map_);

	Object[] _res = (Object[])map_.get("abc");
	assertTrue(_res.length == 1);
	assertEquals("value1", _res[0]);

	_res = (Object[])map_.get("abcd");
	assertTrue(_res.length == 2);
	assertTrue("value1".equals(_res[0]) || "value1".equals(_res[1]));
	assertTrue("value2".equals(_res[0]) || "value2".equals(_res[1]));
    }

    public void testExactGet() throws Exception {
	map_.put("name1", "value1");
	map_.put("name2", "value2");
	map_.put("nane1", "value3");
	map_.put("nane2", "value4");
	map_.put("na*", "value5");
	map_.put("na*e1", "value6");

	System.out.println(map_);

	assertEquals("value1", map_.get("name1", false));
	assertEquals("value2", map_.get("name2", false));
	assertEquals("value3", map_.get("nane1", false));
	assertEquals("value4", map_.get("nane2", false));
	assertEquals("value5", map_.get("na*", false));
	assertEquals("value6", map_.get("na*e1", false));
    }

    public void testAdd() throws Exception {
	map_.put("name", "wert");
	map_.put("java", "Programming Language");
	System.out.println(map_);
	
	Object _old = map_.put("name","neuer wert");
	System.out.println(map_);
	
	assertEquals("wert", _old);

	Object[] o1 = (Object[])map_.get("name");
	Object[] o2 = (Object[])map_.get("java");
	
	assertTrue(o1.length == 1);
	assertEquals("neuer wert", o1[0]);

	assertTrue(o2.length == 1);
	assertEquals("Programming Language", o2[0]);
    }

    /** 
     * Creates a new <code>WildcardMapTest</code> instance.
     *
     * @param name test name
     */
    public WildcardMapTest (String name){
	super(name);
    }

    /**
     * @return a <code>TestSuite</code>
     */
    public static TestSuite suite(){
	TestSuite suite = new TestSuite();

	suite.addTest(new WildcardMapTest("testBugInsertAfterSplit"));
	suite = new TestSuite(WildcardMapTest.class);	

	return suite;
    }

    static {
	BasicConfigurator.configure();
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }
}// WildcardMapTest


