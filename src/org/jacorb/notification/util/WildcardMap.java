package org.jacorb.notification.util;

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

import java.util.Vector;
import org.apache.log.Logger;
import org.apache.log.Hierarchy;

/**
 * An Object that maps String Keys to Values. A WildcardMap cannot
 * contain duplicate 
 * keys. Each Key has exactly one Entry associated. A Key can contain
 * the Wildcard Character '*' which matches zero or more characters of
 * the key. The WildcardMap supports two semantics of accessing the
 * entries. The first way is to ignore the special meaning of the
 * Wildcard character and to 
 * just return the entries as they were inserted.<br>
 * This way you could put some entries in a WildcardMap and fetch them
 * again using the Operation {@link #getNoExpansion(Object) getNoExpansion()}:
 * <pre>
 * WildcardMap wc = new WildcardMap();
 * wc.put("abc", new Integer(1));
 * wc.put("a*", new Integer(2));
 * wc.getNoExpansion("abc") => 1
 * wc.getNoExpansion("a*") => 2
 * wc.getNoExpansion("xyz") => null
 * </pre>
 * This behaviour is similiar to a {@link java.util.Map Map}.<br>
 * The other way using the WildcardMap is to call the Operation {@link
 * #getWithExpansion(Object) getWithExpansion()}. This Operations
 * matches the requested Key to all contained Keys. If the Key of an Entry
 * contains the Wildcard Character '*' it is matched
 * as expected by the semantic of '*'. The Operations returns an array
 * of all matching entries: 
 * <pre>
 * wc.getWithExpansion("abc") => [1,2]
 * wc.getWithExpansion("a") => [2]
 * wc.getWithExpansion("abcd") => [2]
 * wc.getWithExpansion("xyz") => []
 * </pre>
 *
 * @author Alphonse Bendt
 * @version $Id$
 */

public class WildcardMap
{

    static Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(WildcardMap.class.getName());

    static final int DEFAULT_TOPLEVEL_SIZE = 4;

    EntryList topLevel_;

    public WildcardMap( int topLevelSize, int secondLevelSize )
    {
        this( topLevelSize );
        EntryList.DEFAULT_INITIAL_SIZE = secondLevelSize;
    }

    public WildcardMap( int topLevelSize )
    {
        super();
        topLevel_ = new EntryList( topLevelSize );
    }

    public WildcardMap()
    {
        this( DEFAULT_TOPLEVEL_SIZE );
    }

    public void clear()
    {
        topLevel_.clear();
    }

    public Object remove
        ( Object key )
    {
        char[] _key = key.toString().toCharArray();
        return topLevel_.remove( _key, 0, _key.length );
    }

    /**
     * The operation <code>put</code> associates the specified value
     * with the specified key in this map. The String representation
     * of the Key {@link java.lang.Object#toString() toString()} is
     * used. If the map previously
     * contained a mapping for this key, the old value is replaced by
     * the specified value.
     *
     * @param key key with which String representation the specified value
     * is to be associated. 
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or null
     * if there was no mapping for key. 
     */
    public Object put( Object key, Object value )
    {
        char[] _key = key.toString().toCharArray();

        WCEntry _entry = new WCEntry( _key, 0, _key.length, value );
        Object _ret = topLevel_.put( _entry );

        return _ret;
    }

    /**
     * Returns the value to which this map maps the specified
     * key. Returns null if the map contains no mapping for this key. 
     * @param key key whose associated value is to be returned
     * @return the value to which this map maps the specified key, or
     * null if the map contains no mapping for this key.  
     */
    public Object getNoExpansion( Object key )
    {
        char[] _key = key.toString().toCharArray();
        return topLevel_.getSingle( _key, 0, _key.length );
    }

    /**
     * Returns the value to which this map maps the specified
     * key. Additionaly return all Values which keys contain a
     * Wildcard and match the requested key. Returns null if the map
     * contains no mapping for this key.  
     * @param key key whose associated value is to be returned
     * @return an Array of all Matching entries or null if no matching
     * entry could be found.
     */
    public Object[] getWithExpansion( Object key )
    {
        char[] _key = key.toString().toCharArray();
        return topLevel_.getMultiple( _key, 0, _key.length );
    }

    /**
     * @return a String representation of this WildcardMap
     */
    public String toString()
    {
        return topLevel_.toString();
    }

    static int countStars( char[] key, int start, int end )
    {
        int _starCount = 0;
        int x = start;

        while ( x < end )
        {
            if ( key[ x ] == '*' )
            {
                ++_starCount;
            }

            ++x;
        }

        return _starCount;
    }


} // WildcardMap

class EntryList
{

    static Logger logger_ = 
	Hierarchy.getDefaultHierarchy().getLoggerFor(EntryList.class.getName());

    static int DEFAULT_INITIAL_SIZE = 2;

    PatternWrapper myPattern_;

    char[] key_;
    int start_;
    int end_;
    int depth_;

    int splitted = 0;

    WCEntry myEntry_;

    EntryList[] entries_;

    ////////////////////////////////////////
    // Constructors

    EntryList()
    {
        this( null, 0, 0, 0, null, DEFAULT_INITIAL_SIZE );
    }

    EntryList( int size )
    {
        this( null, 0, 0, 0, null, size );
    }

    EntryList( char[] key )
    {
        this( key, 0, 0, 0, null, DEFAULT_INITIAL_SIZE );
    }

    EntryList( char[] key, int start, int end, int depth )
    {
        this( key, start, end, depth, null, DEFAULT_INITIAL_SIZE );
    }

    EntryList( char[] key, int start, int end, int depth, WCEntry value )
    {
        this( key, start, end, depth, value, DEFAULT_INITIAL_SIZE );
    }

    EntryList( char[] key, int start, int end, int depth, WCEntry entry, int size )
    {
        myEntry_ = entry;
        key_ = key;
        end_ = end;
        start_ = start;
        depth_ = depth;
        entries_ = new EntryList[ size ];
        initPattern( key_, start_, end_ );
    }

    ////////////////////////////////////////

    synchronized void clear()
    {
        entries_ = new EntryList[ DEFAULT_INITIAL_SIZE ];
    }

    synchronized Object put( WCEntry entry )
    {
        char _first = entry.key_[ 0 ];
        ensureSpace( _first );

        int _idx = computeIndex( _first, entries_.length );

        if ( entries_[ _idx ] == null )
        {
            entries_[ _idx ] = new EntryList( entry.key_, 0, entry.key_.length, 0, entry );
            return null;
        }
        else
        {
            return entries_[ _idx ].put( entry.key_, 0, entry.key_.length, 0, entry, false );
        }
    }

    Object put( char[] key, int start, int stop, int depth, WCEntry value, boolean addLeadingStar )
    {

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "put(" 
			   + new String( key, start, stop - start ) 
			   + ", " 
			   + depth 
			   + ", " 
			   + value 
			   + ")" );
        }

        int _insertKeyLength = stop - start;
        int _myKeyLength = end_ - start_;

        int _prefixLength = findCommonPrefix( key, start, stop );

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "common Prefix Length: " + _prefixLength );
            logger_.debug( "common Prefix: " + new String( key, start, _prefixLength ) );
        }

        if ( _prefixLength == _insertKeyLength )
        {
            logger_.debug( "prefixLength == insertKeyLength" );

            if ( endsWithStar() )
            {
                split( this, _prefixLength );
            }

            Object _old = null;
            // overwrite

            if ( myEntry_ != null )
            {
                _old = myEntry_.value_;
            }

            myEntry_ = value;

            return _old;
        }
        else if ( _prefixLength < _myKeyLength )
        {
            split( this, _prefixLength );

            boolean _addStar = false;

            if ( endsWithStar() )
            {
                _addStar = true;
            }

            put( key, start, stop, depth + _prefixLength, value, _addStar );

        }
        else
        {
            char _firstRemainingChar = key[ start + _prefixLength ];
            ensureSpace( _firstRemainingChar );

            int idx = computeIndex( _firstRemainingChar, entries_.length );

            if ( entries_[ idx ] == null )
            {
                entries_[ idx ] = new EntryList( key,
                                                 start + _prefixLength,
                                                 stop,
                                                 depth_ + _prefixLength,
                                                 value );

                if ( addLeadingStar )
                {
                    entries_[ idx ].addLeadingStar();
                }
            }
            else
            {
                entries_[ idx ].put( key,
                                     start + _prefixLength,
                                     stop,
                                     depth + _prefixLength,
                                     value,
                                     false );
            }
        }

        return null;
    }

    synchronized Object getSingle( char[] key, int start, int stop )
    {
        Object _result = null;
        EntryList _entryList = lookup( key[ start ] );
        int _position = start;

        while ( _entryList != null )
        {
            int _currentSubKeyLength = _entryList.end_ - _entryList.start_;
            int _remainingKeyLength = stop - _position;


            int _devoured = _entryList.compare( key,
                                                start + _entryList.depth_,
                                                start + _entryList.depth_ + _remainingKeyLength,
                                                false );

            if ( _devoured == _remainingKeyLength )
            {
                return ( _entryList.myEntry_.value_ );
            }
            else if ( _devoured > 0 )
            {
                char _firstRemainingChar = key[ start + _entryList.depth_ + _devoured ];
                int _oldDepth = _entryList.depth_;

                _entryList = _entryList.lookup( _firstRemainingChar );

                if ( _entryList != null )
                {
                    _position += _entryList.depth_ - _oldDepth;
                }
            }
        }

        return null;
    }

    boolean endsWithStar()
    {
        return key_[ end_ -1 ] == '*';
    }

    synchronized Object[]
    getMultiple( char[] key, int start, int stop )
    {
        Vector _nodes = new Vector();
        Vector _result = new Vector();

        EntryList _list;
        Cursor _startCursor;

        if ( ( _list = lookup( key[ start ] ) ) != null )
        {
            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "found outgoing " + key[ start ] );
            }

            _startCursor = new Cursor();
            _startCursor.cursor = start;
            _startCursor.list = _list;
            _nodes.add( _startCursor );
        }

        if ( ( _list = lookup( '*' ) ) != null )
        {
            logger_.debug( "found outgoing *" );

            _startCursor = new Cursor();
            _startCursor.cursor = start;
            _startCursor.list = _list;
            _nodes.add( _startCursor );
        }

        while ( !_nodes.isEmpty() )
        {
            Cursor _current = ( Cursor ) _nodes.firstElement();

            int _currentSubKeyLength = _current.list.end_ - _current.list.start_;
            int _remainingKeyLength = stop - _current.cursor;

            // try to match the search key to the part of key which is
            // associated with the current node
            int _devoured = _current.list.compare( key,
                                                   start + _current.list.depth_,
                                                   start + _current.list.depth_ + _remainingKeyLength,
                                                   true );

            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "could devour " + _devoured + " chars" );
            }

            if ( _devoured >= _remainingKeyLength )
            {
                // the whole key could be matched
                logger_.debug( "devoured >= remainingKey" );

                if ( _current.list.myEntry_ != null )
                {
                    // if the current node has a result add it.
                    _result.add( _current.list.myEntry_.value_ );
                }

                if ( ( _remainingKeyLength > 0 ) && _current.list.endsWithStar() )
                {
                    // current key ends with *
                    // this means the last compare matched everything
                    // nontheless there still might be outgoing edges
                    // which must be eval'd if we have some more chars in
                    // the key left.
                    logger_.debug( "ends with *" );

                    for ( int x = 0; x < _current.list.entries_.length; ++x )
                    {
                        if ( _current.list.entries_[ x ] != null )
                        {
                            Cursor _c = new Cursor();
                            _c.list = _current.list.entries_[ x ];
                            _c.cursor = _current.list.depth_ + 1;

                            _nodes.add( _c );
                        }
                    }
                }

                if ( _current.list.lookup( '*' ) != null )
                {
                    // if there is a outgoing '*' visit it
                    // because it might match the end of a key
                    logger_.debug( "lookup(*) != null" );
                    _current.list = _current.list.lookup( '*' );
                    _current.cursor += _devoured;
                }
                else
                {
                    logger_.debug( "remove cursor" );
                    _nodes.removeElementAt( 0 );
                }
            }
            else if ( _devoured > 0 )
            {
                // a part could be matched
                char _firstRemainingChar = key[ start + _current.list.depth_ + _devoured ];

                int _oldDepth = _current.list.depth_;

                // * always matches

                if ( _current.list.lookup( '*' ) != null )
                {
                    Cursor c = new Cursor();
                    c.list = _current.list.lookup( '*' );
                    c.cursor = _current.cursor + c.list.depth_ - _oldDepth;
                    _nodes.add( c );
                }

                if ( ( _current.list = _current.list.lookup( _firstRemainingChar ) ) != null )
                {
                    // instead of removing the old and adding a new
                    // cursor we reuse the old cursor
                    _current.cursor += _current.list.depth_ - _oldDepth;
                }
                else
                {
                    _nodes.removeElementAt( 0 );
                }
            }
            else
            {
                // no part of the search key could be matched
                _nodes.removeElementAt( 0 );
            }
        }

        return _result.toArray();
    }

    synchronized Object remove
        ( char[] key, int start, int stop )
    {

        return remove
                   ( this, key, start, stop );
    }

    static Object remove
        ( EntryList l, char[] key, int start, int stop )
    {
        int _cursor = start;
        EntryList _current = l;

        while ( true )
        {
            int _devoured = findCommonPrefix( key, _cursor, stop, _current.key_, _current.start_, _current.end_ );
            _cursor += _devoured;

            if ( _cursor == stop )
            {
                Object _old = null;

                if ( _current.myEntry_ != null )
                {
                    _old = _current.myEntry_.value_;
                    _current.myEntry_ = null;
                }

                return _old;
            }

            char _firstNext = key[ start + _devoured ];
            _current = _current.lookup( _firstNext );

            if ( _current == null )
            {
                return null;
            }
        }
    }

    ////////////////////////////////////////
    // private methods

    private EntryList lookup( char key )
    {
        int idx = computeIndex( key, entries_.length );

        if ( entries_[ idx ] != null && entries_[ idx ].key() == key )
        {
            return entries_[ idx ];
        }
        else
        {
            return null;
        }
    }

    private class Cursor
    {
        int cursor;
        int offset;
        EntryList list;

        public String toString()
        {
            String _rest = new String( list.key_, cursor, list.end_ - cursor );
            return "Cursor: " + _rest;
        }
    }

    void addLeadingStar()
    {
        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "old Key: " + new String( key_ , start_, end_ - start_ ) );
        }

        int _newLength = end_ - start_ + 1;

        char[] _newKey = new char[ _newLength ];
        System.arraycopy( key_, start_, _newKey, 1, end_ - start_ );
        _newKey[ 0 ] = '*';

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "new Pattern: " + new String( _newKey, 0, _newLength ) );
        }

        initPattern( _newKey, 0, _newLength );
    }

    private void initPattern()
    {
        initPattern( key_, start_, end_ );
    }

    private void initPattern( char[] key, int start, int stop )
    {
        myPattern_ = null;

        int _starCount = WildcardMap.countStars( key, start, stop );

        if ( _starCount > 0 )
        {
            char[] _pattern = new char[ stop - start + _starCount + 1 ];
            _pattern[ 0 ] = '^';
            int x = 0;
            int _offset = 1;

            while ( x < ( stop - start ) )
            {
                char _x = key[ start + x ];
                _pattern[ x + _offset ] = _x;

                if ( _pattern[ x + _offset ] == '*' )
                {
                    _pattern[ x + _offset ] = '.';
                    _pattern[ x + _offset + 1 ] = '*';
                    ++_offset;
                }

                ++x;
            }

            String _patternString = new String( _pattern, 0 , stop - start + _starCount + 1 );
            myPattern_ = PatternWrapper.init( _patternString );
        }
    }

    private char key()
    {
        return key_[ start_ ];
    }

    private int keyLength()
    {
        return end_ - start_;
    }

    private void ensureSpace( char a )
    {
        int idx = computeIndex( a, entries_.length );

        while ( true )
        {
            if ( idx >= entries_.length )
            {
                //      Debug.myAssert(false, "error: computet Index: " + idx + ", " + entries_.length + ", " + entries_.length + ")");
            }

            if ( entries_[ idx ] == null || entries_[ idx ].key() == a )
            {
                return ;
            }

            doubleCapacity();
            idx = computeIndex( a, entries_.length );
        }
    }

    private void doubleCapacity()
    {
        int _newSize = entries_.length * 2;
        EntryList[] _newList = new EntryList[ _newSize ];

        for ( int x = 0; x < entries_.length; ++x )
        {
            if ( entries_[ x ] != null )
            {
                int _arrayPos = entries_[ x ].key() % _newSize;
                _newList[ _arrayPos ] = entries_[ x ];
            }
        }

        entries_ = _newList;
    }

    private int compare( char[] a, int start, int stop, boolean wildcard )
    {
        if ( wildcard && myPattern_ != null )
        {
            return compareChar( a, start, stop, myPattern_ );
        }
        else
        {
            return compareChar( a, start, stop, key_, start_, end_ );
        }
    }

    private int findCommonPrefix( char[] key, int start, int stop )
    {
        return findCommonPrefix( key, start, stop, key_, start_, end_ );
    }

    private void printToStringBuffer( StringBuffer sb, String offset )
    {
        if ( key_ != null )
        {
            sb.append( " --" );
            sb.append( key() );
            sb.append( "-->\n" );
            sb.append( offset );
            sb.append( "depth: " );
            sb.append( depth_ );
            sb.append( "\n" );
            sb.append( offset );
            sb.append( "key: " );
            sb.append( new String( key_, start_, end_ - start_ ) );
            sb.append( "\n" );
        }

        if ( myEntry_ != null )
        {
            sb.append( offset + myEntry_ );
            sb.append( "\n" );
        }

        for ( int x = 0; x < entries_.length; x++ )
        {
            sb.append( offset + x );
            sb.append( ":" );

            if ( entries_[ x ] == null )
            {
                sb.append( "empty" );
            }
            else
            {
                entries_[ x ].printToStringBuffer( sb, offset + "   " );
            }

            sb.append( "\n" );
        }
    }

    public String toString()
    {
        // return "start: " + start_ + " end: " +end_;

        StringBuffer _b = new StringBuffer();
        printToStringBuffer( _b, "" );
        return _b.toString();
    }

    ////////////////////////////////////////
    // static methods

    private static void split( EntryList l, int offset )
    {

        logger_.debug( "split" );

        EntryList _ret = new EntryList( l.key_,
                                        l.start_ + offset,
                                        l.end_ ,
                                        l.depth_ + offset,
                                        l.myEntry_ ,
                                        l.entries_.length );

        System.arraycopy( l.entries_, 0, _ret.entries_, 0, l.entries_.length );

        l.entries_ = new EntryList[ DEFAULT_INITIAL_SIZE ];

        char _key = l.key_[ l.start_ + offset ];

        int _idx = computeIndex( _key, l.entries_.length );

        l.entries_[ _idx ] = _ret;
        l.myEntry_ = null;
        l.splitted++;
        l.end_ = l.start_ + offset;

        if ( l.endsWithStar() )
        {
            if ( logger_.isDebugEnabled() )
            {
                logger_.debug( "ends with star: " + l.endsWithStar() );
            }

            _ret.addLeadingStar();
        }

        // System.out.println(l);

        l.initPattern();
    }

    private static int computeIndex( char c, int size )
    {
        return c % size;
    }

    static int compareChar( char[] a, int start1, int stop1, char[] b, int start2, int stop2 )
    {
        if ( logger_.isDebugEnabled() )
        {
            logger_.debug ( "compare(" + new String( a, start1, stop1 - start1 ) +
                    " == " + new String( b, start2, stop2 - start2 ) + ")" );
        }

        int length1 = stop1 - start1;
        int length2 = stop2 - start2;
        int _guard = ( length1 > length2 ) ? length2 : length1;

        int _ret = 0;

        while ( _ret < _guard )
        {
            if ( a[ start1 + _ret ] != b[ start2 + _ret ] )
            {
                return _ret;
            }

            ++_ret;
        }

        return _ret;
    }

    static int compareChar( char[] string1,
                            int start1,
                            int stop1,
                            PatternWrapper p )
    {

        if ( logger_.isDebugEnabled() )
        {
            logger_.debug( "compare " 
			   + new String( string1, start1, stop1 - start1 ) 
			   + " == " 
			   + p 
			   + ")" );
        }

        String _other = new String( string1, start1, stop1 - start1 );

        return p.match( _other );
    }

    private static int findCommonPrefix( char[] string1,
                                         int start1,
                                         int stop1,
                                         char[] string2,
                                         int start2,
                                         int stop2 )
    {
        int _x = 0;
        int _length1 = stop1 - start1;
        int _length2 = stop2 - start2;

        int _guard = ( _length1 >= _length2 ) ? _length2 : _length1;

        while ( ( _x < _guard ) && ( string1[ start1 ] == string2[ start2 ] ) )
        {
            ++start1;
            ++start2;
            ++_x;
        }

        return _x;
    }
}


class WCEntry
{

    static Logger logger_ = Hierarchy.getDefaultHierarchy().getLoggerFor(WCEntry.class.getName());

    char[] key_;
    int start_;
    int stop_;
    Object value_;

    WCEntry( char[] key, int start, int stop, Object value )
    {
        key_ = key;
        start_ = start;
        stop_ = stop;
        value_ = value;
    }

    public boolean compare( char[] key, int start, int stop )
    {
	if (logger_.isDebugEnabled()) {
	    logger_.debug( "compare(" 
			   + new String( key, start, stop - start ) 
			   + " == " 
			   + new String( key_ , start_ , stop_ - start_ ) 
			   + ")" );
	}

        int _myLength = stop_ - start_;
        int _otherLength = stop - start;

        if ( _myLength != _otherLength )
        {
            return false;
        }

        int x = 0;

        while ( x < _myLength )
        {
            if ( key[ start + x ] != key_[ start_ + x ] )
            {
                return false;
            }
        }

        return true;
    }

    public boolean equals( Object o )
    {
        try
        {
            WCEntry _other = ( WCEntry ) o;
            return ( EntryList.compareChar( key_, 
					    start_, 
					    stop_, 
					    _other.key_, 
					    _other.start_, 
					    _other.stop_ ) > 0 );
        }
        catch ( ClassCastException c )
        {
            return super.equals( o );
        }
        catch ( NullPointerException n )
        {
            return false;
        }
    }

    public String toString()
    {
        StringBuffer _b = new StringBuffer();

        _b.append( "['" );
        _b.append( new String( key_, start_ , stop_ - start_ ) );
        _b.append( "' => " );
        _b.append( value_ );
        _b.append( "]" );

        return _b.toString();
    }
}
