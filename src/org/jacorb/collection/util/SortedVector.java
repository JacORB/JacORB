package org.jacorb.collection.util;

import java.util.Enumeration; 

public class SortedVector {
    private DynArray data;
    private ObjectComparator cmpr;
/* -------------------------------------------------------------------------- */
    public SortedVector( ObjectComparator cmpr ) {
        data = new DynArray();
        this.cmpr = cmpr;
    };
/* -------------------------------------------------------------------------- */
    public SortedVector( ObjectComparator cmpr, int capacity ) {
        data = new DynArray( capacity );
        this.cmpr = cmpr;
    };
/* -------------------------------------------------------------------------- */
    public  int addElement( Object obj ) throws ObjectInvalid {
        cmpr.element( obj );
        int pos = find_nearest();
        if ( pos > -1 && cmpr.compare_with( data.elementAt(0) ) >= 0 ) {
            while( pos < data.size() && cmpr.compare_with( data.elementAt(pos) ) >= 0 ) {
               pos++;
            };
        } else {
            pos = 0;
        }
        data.insertElementAt( obj, pos );
        return pos;
    };
/* -------------------------------------------------------------------------- */
    public  int size() {
        return data.size();
    };
/* -------------------------------------------------------------------------- */
    public  Object elementAt( int index ) {
        return data.elementAt( index );
    };
/* -------------------------------------------------------------------------- */
    public  Enumeration elements() {
        return data.elements();
    };
/* -------------------------------------------------------------------------- */
    public  Object removeElementAt( int index ){
        Object obj = data.elementAt( index );
        data.removeElementAt( index );
        return obj;
    };
/* -------------------------------------------------------------------------- */
    public  void removeAllElements() {
        data.removeAllElements();
    };
/* -------------------------------------------------------------------------- */
    public  int indexOf( Object obj ) throws ObjectInvalid {
        cmpr.element( obj );
        int i = find_nearest();
        if( i == data.size() ) {
            i--;
        }
        if( i >= data.size() || i<0 ){
            return -1;
        }
        int j = i;
        while( j >= 0 && cmpr.compare_with( data.elementAt(j) ) <= 0 ){
            if( cmpr.equal( data.elementAt( j ) ) ){
                return j;
            }
            j--;
        };
        j = i+1;
        while( j < data.size() && cmpr.compare_with( data.elementAt(j) ) >= 0 ){
            if( cmpr.equal( data.elementAt( j ) ) ){
                return j;
            }
            j++;
        };
        return -1;
    };
/* -------------------------------------------------------------------------- */
    public  void setElementAt( Object obj, int index ) throws ObjectInvalid {
        if( !isIndexValid( index, obj ) ) {
            throw new ObjectInvalid();
        }
        data.setElementAt( obj, index );
    };
/* -------------------------------------------------------------------------- */
    public  boolean isIndexValid( int index, Object obj ) throws ObjectInvalid {
        cmpr.element( obj );
        if( data.size() == 0 ){
            return true;
        }
        int i = find_nearest();
        if( cmpr.equal( data.elementAt(i) ) ){
            return true;
        } else if( ( i==0 || cmpr.compare_with( data.elementAt(i-1) ) >= 0 )
                     && ( i==data.size() || cmpr.compare_with( data.elementAt(i) ) <= 0 ) ){
            return true;
        }
        return false;
    };
/* -------------------------------------------------------------------------- */
    public boolean insertElementAt( Object obj, int index ) throws ObjectInvalid {
        if( isIndexValid( index, obj ) ){
            data.insertElementAt( obj, index );
        }
        return false;
    };
/* -------------------------------------------------------------------------- */
    private int find_nearest() throws ObjectInvalid {
        if( data.size() == 0 ){
            return -1;
        }
        int first = 0;
        if( cmpr.compare_with( data.elementAt(first) ) <= 0 ){
            return first;
        }
        int last = data.size()-1;
        if( cmpr.compare_with( data.elementAt(last) ) >= 0 ){
            return data.size();
        }

        if ( first == last ) {
            if ( cmpr.compare_with( data.elementAt(last) ) <= 0 ) {
                return first;
            } else {
                return first+1;
            }
        }

        int result = 0;
        int pos = first;
        while( first < last ){
            pos = (first+last)/2;
            if ( pos == first ) {
                return ++pos;
            }
            if ( pos == last ) {
                return ++pos;
            }
            result = cmpr.compare_with( data.elementAt(pos) );
            if( result == 0 ){
                return ++pos;
            } else if( result > 0 ){
                first = pos;
            } else {
                last = pos;
            }
        };
        return pos;
    };
