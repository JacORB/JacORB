package org.jacorb.test.notification.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Random;
import java.util.Map;
import java.util.Hashtable;
import org.apache.log.Hierarchy;
import org.jacorb.notification.util.WildcardMap;

/**
 *  Unit Test for class WildcardMap
 *
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class WildcardMapTest extends TestCase {

    protected WildcardMap map_;

    public void setUp() throws Exception {
	map_ = new WildcardMap();
    }

    public void testBugInsertAfterSplit() throws Exception {
	map_.put("abcd", "ABCD");
	map_.put("abef", "ABEF");
	map_.put("ab", "AB");

	assertEquals("ABCD", ((Object[])map_.getWithExpansion("abcd"))[0]);
	assertEquals("AB", ((Object[])map_.getWithExpansion("ab"))[0]);
	assertEquals("ABEF", ((Object[])map_.getWithExpansion("abef"))[0]);
    }

    public void performancePut() throws Exception {

	long _wcTime = 0;
	long _hashTime = 0;
	int _iterations = 20;

	
	if (Hierarchy.getDefaultHierarchy().getLoggerFor("org.jacorb.notification.util").isInfoEnabled()) {
	    Hierarchy.getDefaultHierarchy().getRootLogger().info("you should disable logging for testPerformance");
	}

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

		assertEquals(_testValues[x],
			     (((Object[])_wcMap.getWithExpansion(_testKeys[x]))[0]));

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

	System.out.println("Measure time for multiple (" + _testSize + ") put Operations");	

	System.out.println("Total Time:");
	System.out.println("  Hashtable:\t" + _hashTime);
	System.out.println("  WildcardMap:\t" + _wcTime);

	System.out.println("Average Time:");
	System.out.println("  Hashtable:\t" + _hashTime / _iterations);
	System.out.println("  WildcardMap:\t" + _wcTime / _iterations);
    }

    public void performanceGet() throws Exception {
	long _wcTime = 0;

	int testRuns = 10;
	int initialTestSize = 100;
	int totalTests = 10;

	Hashtable testData = new Hashtable();


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
    }

    long wcMapGet(WildcardMap map, Object[] keys, int runs) {
	long _start = System.currentTimeMillis();	    

	// fetch every n'th key
	int n=10;

	for (int r=0; r<runs; ++r) {
	    for (int x=0; x<keys.length/n; ++x) {
		map.getWithExpansion(keys[x*n]);
	    }
	}
	long _stop = System.currentTimeMillis();
	
	return (_stop - _start) / (keys.length / n);
    }

    public void testRemove() throws Exception {
	map_.put("hallo", "Hallo");
	map_.put("hallo2", "Hallo2");
	map_.put("hallo3", "Hallo3");
	map_.put("hallo4", "Hallo4");	

	Object o = map_.remove("hallo2");
	assertEquals("Hallo2", o);

	Object[] l = (Object[])map_.getWithExpansion("hallo2");

	assertTrue(l.length == 0);
    }

    public void testAddStar1() throws Exception {
	map_.put("ha*o", "default");
	Object[] _res = (Object[])map_.getWithExpansion("hallo");
	assertTrue(_res.length == 1);

	_res = (Object[])map_.getWithExpansion("hall");
	assertTrue(_res.length == 0);

	_res = (Object[])map_.getWithExpansion("hao");
	assertTrue(_res.length == 1);

	_res = (Object[])map_.getWithExpansion("hadddddo");
	assertTrue(_res.length == 1);
    }

    public void testAddStar2() throws Exception {
	map_.put("*abc*de", "value");
	Object[] _res = (Object[])map_.getWithExpansion("abcde");
	assertTrue(_res.length == 1);

	_res = (Object[])map_.getWithExpansion("halloabcde");
	assertTrue(_res.length == 1);
	
	_res = (Object[])map_.getWithExpansion("abcbla bla blade");
	assertTrue(_res.length == 1);

	_res = (Object[])map_.getWithExpansion("abcde");
	assertTrue(_res.length == 1);
    }

    public void testAddStar() throws Exception {
	map_.put("abc*", "value1");
	map_.put("abcd", "value2");

	Object[] _res = map_.getWithExpansion("abc");
	assertTrue(_res.length == 1);
	assertEquals("value1", _res[0]);

	_res = (Object[])map_.getWithExpansion("abcd");
	assertTrue(_res.length == 2);
	assertTrue("value1".equals(_res[0]) || "value1".equals(_res[1]));
	assertTrue("value2".equals(_res[0]) || "value2".equals(_res[1]));
    }

    public void testSplitAfterStar() throws Exception {
	map_.put("abc*def", "value1");
	map_.put("abc*ef", "value2");
	map_.put("abc", "value3");

	Object[] _res = map_.getWithExpansion("abcxyzdef");
	assertTrue(_res.length == 2);

	_res = map_.getWithExpansion("abcxyzef");
	assertTrue(_res.length == 1);

	_res = map_.getWithExpansion("abc");
	assertTrue(_res.length == 1);
    }

    public void testExactGet() throws Exception {
	map_.put("name1", "value1");
	map_.put("name2", "value2");
	map_.put("nane1", "value3");
	map_.put("nane2", "value4");
	map_.put("na*", "value5");
	map_.put("na*e1", "value6");

	assertEquals("value1", map_.getNoExpansion("name1"));
	assertEquals("value2", map_.getNoExpansion("name2"));
	assertEquals("value3", map_.getNoExpansion("nane1"));
	assertEquals("value4", map_.getNoExpansion("nane2"));
	assertEquals("value5", map_.getNoExpansion("na*"));
	assertEquals("value6", map_.getNoExpansion("na*e1"));
    }

    public void testAdd() throws Exception {
	map_.put("name", "wert");
	map_.put("java", "Programming Language");
	
	Object _old = map_.put("name","neuer wert");
	
	assertEquals("wert", _old);

	Object[] o1 = (Object[])map_.getWithExpansion("name");
	Object[] o2 = (Object[])map_.getWithExpansion("java");
	
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
    public static Test suite(){
	TestSuite suite = new TestSuite();

	suite = new TestSuite(WildcardMapTest.class);

	// these are no real tests
	// used for performance monitoring
	//	suite.addTest(new WildcardMapTest("performancePut"));
	//	suite.addTest(new WildcardMapTest("performanceGet"));

	return suite;
    }

    /** 
     * Entry point 
     */ 
    public static void main(String[] args) {
	junit.textui.TestRunner.run(suite());
    }

}


