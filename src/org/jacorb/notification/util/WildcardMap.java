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
package org.jacorb.notification.util;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.log4j.Logger;

/*
 *        JacORB - a free Java ORB
 */

/**
 * WildcardMap.java
 *
 *
 *
 * @author <a href="mailto:bendt@inf.fu-berlin.de">Alphonse Bendt</a>
 * @version $Id$
 */

public class WildcardMap extends AbstractMap implements Map {

    public static final int DEFAULT_TOPLEVEL_SIZE = 4;

    EntryList topLevel_;

    protected Logger logger_;

    public WildcardMap(int topLevelSize, int secondLevelSize) {
	this(topLevelSize);
	EntryList.DEFAULT_INITIAL_SIZE = secondLevelSize;
    }

    public WildcardMap(int topLevelSize) {
	super();
	logger_ = Logger.getLogger("WildcardMap");
	topLevel_ = new EntryList(topLevelSize);
    }

    public WildcardMap() {
	this(DEFAULT_TOPLEVEL_SIZE);
    }

    public Set entrySet() {
	return null;
    }

    public void clear() {
	topLevel_.clear();
    }

    public Object remove(Object key) {
	logger_.info("remove(" + key + ")");

	char[] _key = key.toString().toCharArray();
	return topLevel_.remove(_key, 0, _key.length);
    }

    public Object put(Object key, Object value) {
	logger_.info("put(" + key + ", " + value + ")");

	char[] _key = key.toString().toCharArray();
	WCEntry _entry = new WCEntry(_key, 0, _key.length, value);
	Object _ret = topLevel_.put(_entry);

	return _ret;
    }

    public Object get(Object key) {
	return get(key, true);
    }

    public Object get(Object key, boolean wildcard) {
	logger_.info("get(" + key + ", " + wildcard + ")");
	char[] _key = key.toString().toCharArray();
	
	if (wildcard) {
	    return topLevel_.getMultiple(_key, 0, _key.length);
	} else {
	    return topLevel_.getSingle(_key, 0, _key.length);
	}
    }

    public String toString() {
	return topLevel_.toString();
    }

    public static void main(String[] args) {
	Map wc = new WildcardMap();

	wc.put("a", "A");
// 	wc.put("ab", "AB");
// 	wc.put("abc*", "ABC*");
// 	wc.put("abcd", "ABCD");
	System.out.println(wc);	
	//	System.out.println(l.get("ab"));
	// 	System.out.println(l.get("abc"));
	Object[] res = (Object[])wc.get("abcd");
	System.out.println(res.length);
	
	for (int x=0; x<res.length; x++) {
	    System.out.println(res[x]);
	}
    }

}// WildcardMap

class EntryList {
    static Logger logger_ = Logger.getLogger("WildcardMap.EntryList");

    static boolean DEBUG = true;
    static int DEFAULT_INITIAL_SIZE = 2;

    char[] key_;
    Pattern myPattern_;

    int start_;
    int end_;
    int depth_;

    int splitted = 0;

    WCEntry myEntry_;
    
    EntryList[] entries_;

    ////////////////////////////////////////
    // Constructors

    EntryList() {
	this(null, 0, 0, 0, null, DEFAULT_INITIAL_SIZE);
    }

    EntryList(int size) {
	this(null, 0, 0, 0, null, size);
    }

    EntryList(char[] key) {
	this(key, 0, 0, 0, null, DEFAULT_INITIAL_SIZE);
    }

    EntryList(char[] key, int start, int end, int depth) {
	this(key, start, end, depth, null, DEFAULT_INITIAL_SIZE);
    }

    EntryList(char[] key, int start, int end, int depth, WCEntry value) {
	this(key, start, end, depth, value, DEFAULT_INITIAL_SIZE);
    }

    EntryList(char[] key, int start, int end, int depth, WCEntry entry, int size) {
	myEntry_ = entry;
	key_ = key;
	end_ = end;
	start_ = start;
	depth_ = depth;
	entries_ = new EntryList[size];
	initPattern();

	if (key != null) {
	    logger_.info("new entry with key " + new String(key, start, end - start) + " depth: " + depth);
	}
    }

    ////////////////////////////////////////

    synchronized void clear() {
	entries_ = new EntryList[DEFAULT_INITIAL_SIZE];
    }

    synchronized Object put(WCEntry entry) {
	char _first = entry.key_[0];
	ensureSpace(_first);

	logger_.debug("put " + entry);

	int idx = computeIndex(_first, entries_.length);
	if (entries_[idx] == null) {
	    logger_.debug("new toplevel node " + _first);
	    entries_[idx] = new EntryList(entry.key_, 0, entry.key_.length, 0, entry);
	    return null;
	} else {
	    return entries_[idx].put(entry.key_, 0, entry.key_.length, 0, entry);
	}
    }

    Object put(char[] key, int start, int stop, int depth, WCEntry value) {
	logger_.debug("insert " + new String(key, start, stop-start) + " on " + new String(key_, start_, end_-start_));

	int _insertKeyLength = stop - start;
	int _myKeyLength = end_ - start_;

	int _prefixLength = findCommonPrefix(key, start, stop);

	if (_prefixLength == _insertKeyLength) {
	    logger_.debug("insertkey == mykey");
	    
	    Object _old = null;
	    // overwrite
	    if (myEntry_ != null) {
		_old = myEntry_.value_;
	    }
	    myEntry_ = value;
	    return _old;
	} else if (_prefixLength < _myKeyLength) {
	    split(this, _prefixLength);
	    put(key, start, stop, depth + _prefixLength, value);
	} else {
	    char _firstRemainingChar = key[start + _prefixLength];
	    logger_.debug("folge kante " + _firstRemainingChar);
	    ensureSpace(_firstRemainingChar);

	    int idx = computeIndex(_firstRemainingChar, entries_.length);

	    if (entries_[idx]==null) {
		logger_.debug("neuer eintrag an " + idx);
		entries_[idx] = new EntryList(key, start + _prefixLength, stop, depth_ + _prefixLength, value);
	    } else {
		entries_[idx].put(key, start + _prefixLength, stop, depth + _prefixLength, value);
	    }
	}
	return null;
    }

    synchronized Object getSingle(char[] key, int start, int stop) {
	Object _result = null;
	EntryList _entryList = lookup(key[start]);
	int _position = start;

	while (_entryList != null) {	
	    int _currentSubKeyLength = _entryList.end_ - _entryList.start_;
	    int _remainingKeyLength = stop - _position;

	    logger_.debug("remaining key  : " + new String(key, _position, _remainingKeyLength));
	    logger_.debug("current subkey : " + new String(_entryList.key_, _entryList.start_, _currentSubKeyLength));

	    int _devoured = _entryList.compare(key, start + _entryList.depth_, start + _entryList.depth_ + _remainingKeyLength, false);

	    logger_.debug("could match " + _devoured + " chars");
	    if (_devoured == _remainingKeyLength) {
		return (_entryList.myEntry_.value_);
	    } else if (_devoured > 0) {
		char _firstRemainingChar = key[start + _entryList.depth_ + _devoured];
		int _oldDepth = _entryList.depth_;
		
		logger_.debug("check for outgoing " + _firstRemainingChar);
		_entryList = _entryList.lookup(_firstRemainingChar);
		if (_entryList != null) {
		    _position += _entryList.depth_ - _oldDepth;
		}
	    }
	}
	return null;
    }

    synchronized Object getMultiple(char[] key, int start, int stop) {
	Vector _nodes = new Vector();
	Vector _result = new Vector();
	EntryList _list;
	Cursor _startCursor;

	if ((_list = lookup(key[start])) != null) {
	    _startCursor = new Cursor();
	    _startCursor.cursor = start;
	    _startCursor.list = _list;
	    _nodes.add(_startCursor);
	} 

	if ((_list = lookup('*')) != null) {
	    _startCursor = new Cursor();
	    _startCursor.cursor = start;
	    _startCursor.list = _list;
	    _nodes.add(_startCursor);
	}

	while (!_nodes.isEmpty()) {
	    Cursor _current = (Cursor)_nodes.firstElement();
	    int _currentSubKeyLength = _current.list.end_ - _current.list.start_;
	    int _remainingKeyLength = stop - _current.cursor;

	    logger_.debug("remaining key   : " + new String(key, _current.cursor, _remainingKeyLength));
	    logger_.debug("current sub key : " + new String(_current.list.key_, _current.list.start_, _currentSubKeyLength));

	    // try to match the search key to the sub key of the
	    // current node
	    int _devoured = _current.list.compare(key,
						  start + _current.list.depth_,
						  start + _current.list.depth_ + _remainingKeyLength, 
						  true);

	    logger_.debug("could match " + _devoured + " chars");

	    assert(_devoured <= _remainingKeyLength);

	    if (_devoured == _remainingKeyLength) {
		// the whole key could be matched
		if (_current.list.myEntry_ != null) {
		    _result.add(_current.list.myEntry_.value_);
		    logger_.debug(_current.list.myEntry_.value_ + " added to result set");
		}

		if (_current.list.lookup('*') != null) {
		    _current.list = _current.list.lookup('*');
		    _current.cursor += _devoured;
		} else {
		    _nodes.removeElementAt(0);
		}
	    } else if (_devoured > 0) {
		// a part could be matched
		char _firstRemainingChar = key[start + _current.list.depth_ + _devoured];


		int _oldDepth = _current.list.depth_;
		logger_.debug("check for outgoing " + _firstRemainingChar);
		logger_.debug("current depth: " + _oldDepth);
		
		// * always matches
		if (_current.list.lookup('*') != null) {
		    Cursor c = new Cursor();
		    c.list = _current.list.lookup('*');
		    c.cursor = _current.cursor + c.list.depth_ - _oldDepth;
		    _nodes.add(c);
		    logger_.debug("add *");
		}

		if ((_current.list = _current.list.lookup(_firstRemainingChar)) != null) {
		    // instead of removing the old and adding a new
		    // cursor we reuse the old cursor
		    _current.cursor += _current.list.depth_ - _oldDepth;
		    logger_.debug("add " + _firstRemainingChar);
		} else {
		    _nodes.removeElementAt(0);
		}
	    } else {
		// no part of the search key could be matched
		_nodes.removeElementAt(0);
	    }
	}
	return _result.toArray();
    }

    synchronized Object remove(char[] key, int start, int stop) {
	return remove(this, key, start, stop);
    }

    static Object remove(EntryList l, char[] key, int start, int stop) {
	logger_.debug("remove " + new String(key, start, stop - start));

	int _cursor = start;
	EntryList _current = l;

	logger_.debug ("stop: " + stop);

	while (true) {
	    logger_.debug("cursor is now: " + _cursor);
	    
	    if (_current.key_ != null) {
	    logger_.debug("match " + new String(key, _cursor, stop - _cursor) + " and " + new String(_current.key_, _current.start_, _current.end_ - _current.start_));
	    }
	    int _devoured = findCommonPrefix(key, _cursor, stop, _current.key_, _current.start_, _current.end_);
	    logger_.debug("match " + _devoured + " chars");

	    _cursor += _devoured;
	    if (_cursor == stop) {
		logger_.debug("key zuende");
		Object _old = null;
		if (_current.myEntry_ != null) {
		    _old = _current.myEntry_.value_;
		    _current.myEntry_ = null;
		}
		return _old;
	    }
	    char _firstNext = key[start + _devoured];
	    logger_.debug("lookup " + _firstNext);
	    _current = _current.lookup(_firstNext);

	    if (_current == null) {
		return null;
	    }
	}
    }

    ////////////////////////////////////////
    // private methods

    private EntryList lookup(char key) {
	int idx = computeIndex(key, entries_.length);
	if (entries_[idx] != null && entries_[idx].key() == key) {
	    return entries_[idx];
	} else {
	    return null;
	}
    }

    private class Cursor {
	int cursor;
	EntryList list;
    }


    private int countStars() {
	int _starCount = 0;
	for (int x=start_; x < end_; ++x) {
	    if (key_[x] == '*') {
		++_starCount;
	    }	    
	}	
	return _starCount;
    }

    private void initPattern() {
	myPattern_ = null;
	int _starCount = countStars();
	if (_starCount > 0) {
	    char[] _pattern = new char[end_ - start_ + _starCount + 1];
	    _pattern[0] = '^';
	    int x=0;
	    int _offset = 1;
	    while (x < (end_ - start_)) {
		char _x = key_[start_ + x];
		_pattern[x + _offset] = _x;

		if (_pattern[x + _offset] == '*') {
		    _pattern[x + _offset] = '.';
		    _pattern[x + _offset + 1] = '*';
		    ++_offset;
		}
		++x;
	    }
	    String _patternString = new String(_pattern, 0 , end_ - start_ + _starCount + 1);
	    logger_.debug("pattern string: " + _patternString);
	    myPattern_ = Pattern.compile(_patternString);
	} 
    }

    private char key() {
	return key_[start_];
    }

    private int keyLength() {
	return end_ - start_;
    }

    private void ensureSpace(char a) {
	int idx = computeIndex(a, entries_.length);
	while(true) {
	    if (idx >= entries_.length) {
	    	System.err.println("error: computet Index: " + idx + ", " + entries_.length + ", " + entries_.length + ")");
	    }
	    if (entries_[idx] == null || entries_[idx].key() == a) {
		return;
	    }
	    doubleCapacity();
	    idx = computeIndex(a, entries_.length);
	}
    }

    private void doubleCapacity() {
	logger_.debug("double capacity");
	
	int _newSize = entries_.length * 2;
	EntryList[] _newList = new EntryList[_newSize];
	for (int x=0; x<entries_.length; ++x) {
	    if (entries_[x] != null) {
		int _arrayPos = entries_[x].key() % _newSize;
		_newList[_arrayPos] = entries_[x];
	    }
	}
	entries_ = _newList;
    }

//     private int compare(char[] a, int start, int stop) {
// 	return compare(a, start, stop, true);
//     }

    private int compare(char[] a, int start, int stop, boolean wildcard) {
	logger_.debug("compare " + new String(a, start, stop - start) + " == " + new String(key_, start_, end_ - start_));

	if (wildcard && myPattern_ != null) {
	    return compareChar(a, start, stop, myPattern_);
	} else {
	    return compareChar(a, start, stop, key_, start_, end_);
	}
    }

    private int findCommonPrefix(char[] key, int start, int stop) {
	return findCommonPrefix(key, start, stop, key_, start_, end_);
    }

    private void printToStringBuffer(StringBuffer sb, String offset) {
	if (key_ != null) {
	    sb.append(" --");
	    sb.append(key());
	    sb.append("-->\n");
	    sb.append(offset);
	    sb.append("depth: ");
	    sb.append(depth_);
	    sb.append("\n");
	    sb.append(offset + "key: ");
	    sb.append(new String(key_, start_, end_ - start_));
	    sb.append("\n");   
	}

	if (myEntry_ != null) {
	    sb.append(offset + myEntry_);
	    sb.append("\n");
	}

	for (int x=0; x<entries_.length; x++) {
	    sb.append(offset + x);
	    sb.append(":");
	    if (entries_[x] == null) {
		sb.append("empty");
	    } else {
		entries_[x].printToStringBuffer(sb, offset + "   ");
	    }
	    sb.append("\n");
	}
    }

    public String toString() {
	StringBuffer _b = new StringBuffer();
	printToStringBuffer(_b, "");
	return _b.toString();
    }

    ////////////////////////////////////////
    // static methods

    private static void split(EntryList l, int offset) {
	logger_.debug("split offset: " + offset);

	EntryList _ret = new EntryList(l.key_, 
				       l.start_ + offset, 
				       l.end_ , 
				       l.depth_ + offset, 
				       l.myEntry_ , 
				       l.entries_.length);

	System.arraycopy(l.entries_, 0, _ret.entries_, 0, l.entries_.length);

	l.entries_ = new EntryList[DEFAULT_INITIAL_SIZE];

	char _key = l.key_[l.start_ + offset];
	
	int _idx = computeIndex(_key, l.entries_.length);

 	assert (_idx <= l.entries_.length);

	l.entries_[_idx] = _ret;
	l.myEntry_ = null;
	l.splitted++;
	l.end_ = l.start_ + offset;
	l.initPattern();
    }

    private static int computeIndex(char c, int size) {
	return c % size;
    }

    static int compareChar(char[] a, int start1, int stop1, char[] b, int start2, int stop2) {
	int length1 = stop1 - start1;
	int length2 = stop2 - start2;
	int _guard = (length1 > length2) ? length2 : length1;

	int _ret = 0;

	while (_ret < _guard) {
	    if (a[start1 + _ret] != b[start2 + _ret]) {
		return _ret;
	    }
	    ++_ret;
	}
	return _ret;
    }

    static int compareChar(char[] a, int aStart, int aStop, Pattern p) {
	String _other = new String(a, aStart, aStop - aStart);
	Matcher _m = p.matcher(_other);
	if (_m.find()) {
	    return _m.end();
	} else {
	    return 0;
	}
    }

    private static int findCommonPrefix(char[] s1, int start1, int stop1, char[] s2, int start2, int stop2) {
	int x=0;
	int l1 = stop1 - start1;
	int l2 = stop2 - start2;

	int guard = (l1 >= l2) ? l2 : l1;
	
	while (( x < guard) && (s1[start1] == s2[start2])) {
	    ++start1;
	    ++start2;
	    ++x;
	}
	return x;
    }
}

class WCEntry {
    char[] key_;
    int start_;
    int stop_;

    Object value_;

    WCEntry(char[] key, int start, int stop, Object value) {
	key_ = key;
	start_ = start;
	stop_ = stop;
	value_ = value;
    }

    public boolean equals(Object o) {
	try {
	    WCEntry _other = (WCEntry)o;
	    return (EntryList.compareChar(key_, start_, stop_, _other.key_, _other.start_, _other.stop_) > 0);
	} catch (ClassCastException c) {
	    return super.equals(o);
	} catch (NullPointerException n) {
	    return false;
	}
    }

    public String toString() {
	return ("['" + new String(key_, start_, stop_ - start_) + "' => " + value_ + "]");
    }
}
