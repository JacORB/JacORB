/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
package org.jacorb.collection;

import org.jacorb.collection.util.ObjectInvalid;
import org.jacorb.collection.util.SortedVector;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosCollection.ElementInvalidReason;
import org.omg.CosCollection.EmptyCollection;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.KeyCollection;
import org.omg.CosCollection.KeyInvalid;
import org.omg.CosCollection.KeySortedCollectionOperations;
import org.omg.CosCollection.OperationsOperations;
import org.omg.CosCollection.PositionInvalid;
import org.omg.PortableServer.POA;

class KeySortedCollectionImpl extends OrderedCollectionImpl implements KeySortedCollectionOperations {
    protected SortedVector keys;
    protected KeyNode test_key = new KeyNode();
/* ========================================================================= */
    KeySortedCollectionImpl( OperationsOperations ops, POA poa, IteratorFactory iterator_factory ){
        super( ops, poa, iterator_factory );
        keys = new SortedVector( new KeyComparator( ops ) );
    };
/* ========================================================================= */
    public org.omg.CORBA.TypeCode key_type(){
        return ops.key_type();
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean contains_element_with_key(Any key) throws KeyInvalid{
        check_key( key );
        test_key.key = key;
        try {
            if( keys.indexOf( test_key ) >= 0 ){
                return true;
            } else {
                return false;
            } 
        } catch ( ObjectInvalid e ){
            throw new KeyInvalid();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean contains_all_keys_from(KeyCollection collector) throws KeyInvalid{
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_or_add_element_with_key(Any element) throws ElementInvalid{
        check_element( element );
        test_key.key = ops.key( element );
        try { 
            if( keys.indexOf( test_key ) < 0 ){
                return add_element( element );
            };
            return false;
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_or_add_element_with_key_set_iterator(Any element,org.omg.CosCollection. Iterator where) throws ElementInvalid,IteratorInvalid{
        check_element( element );
        test_key.key = ops.key( element );
        try { 
            if( keys.indexOf( test_key ) < 0 ){
                return add_element_set_iterator( element, where );
            };
            return false;
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean add_or_replace_element_with_key(Any element) throws ElementInvalid{
        check_element( element );
        test_key.key = ops.key( element );
        try { 
            int pos = keys.indexOf( test_key );
            pos = ((KeyNode)keys.elementAt( pos )).start_position;
            if(  pos < 0 ){
                return add_element( element );
            } else {
                element_replace( pos, element );
                return false;
            }
        } catch ( ObjectInvalid e ) {
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        } catch ( PositionInvalid e ) {
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean add_or_replace_element_with_key_set_iterator(Any element, org.omg.CosCollection.Iterator where) throws ElementInvalid,IteratorInvalid{
        check_element( element );
        test_key.key = ops.key( element );
        try { 
            int pos = keys.indexOf( test_key );
            pos = ((KeyNode)keys.elementAt( pos )).start_position;
            if( pos < 0 ){
                return add_element_set_iterator( element, where );
            } else {
                PositionalIteratorImpl i = check_iterator( where );
                element_replace( pos, element );
                i.set_pos(pos);
                i.set_in_between( false );
                return false;
            }
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        } catch ( PositionInvalid e ) {
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean remove_element_with_key(Any key) throws KeyInvalid{
        check_key( key );
        test_key.key = key;
        try { 
            int pos = keys.indexOf( test_key );
            if( pos < 0 ){
                return false;
            }
            pos = ((KeyNode)keys.elementAt( pos )).start_position;
            element_remove( pos );
            return true;
        } catch ( ObjectInvalid e ){
            throw new KeyInvalid();
        } catch ( EmptyCollection e ){
            throw new KeyInvalid();
        } catch ( PositionInvalid e ){
            throw new KeyInvalid();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized int remove_all_elements_with_key(Any key) throws KeyInvalid{
        check_key( key );
        test_key.key = key;
        try { 
            int pos = keys.indexOf( test_key );
            if( pos < 0 ){
                return 0;
            } 
            KeyNode node = (KeyNode)keys.elementAt( pos );
            pos = node.start_position;
            for( int i=node.count; i>0; i-- ){ 
                element_remove( pos+i-1 );
            };
            return node.count;
        } catch ( ObjectInvalid e ){
            throw new KeyInvalid();
        } catch ( EmptyCollection e ){
            throw new KeyInvalid();
        } catch ( PositionInvalid e ){
            throw new KeyInvalid();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean replace_element_with_key(Any element) throws ElementInvalid{
        check_element( element );
        test_key.key = ops.key( element );
        try { 
            int pos = keys.indexOf( test_key );
            if( pos < 0 ){
                return false;
            } 
            KeyNode node = (KeyNode)keys.elementAt( pos );
            pos = node.start_position;
            element_replace( pos, element );
            return true;
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        } catch ( PositionInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean replace_element_with_key_set_iterator(Any element, org.omg.CosCollection.Iterator where) throws ElementInvalid,IteratorInvalid{
        check_element( element );
        PositionalIteratorImpl i = check_iterator( where );
        test_key.key = ops.key( element );
        try { 
            int pos = keys.indexOf( test_key );
            if( pos < 0 ){
                return false;
            } 
            KeyNode node = (KeyNode)keys.elementAt( pos );
            pos = node.start_position;
            element_replace( pos, element );
            i.set_pos( pos );
            i.set_in_between( false );
            return true;
        } catch ( ObjectInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        } catch ( PositionInvalid e ){
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean retrieve_element_with_key(Any key, AnyHolder element) throws KeyInvalid{
        check_key( key );
        test_key.key = key;
        try { 
            int pos = keys.indexOf( test_key );
            if( pos < 0 ){
                return false;
            } 
            KeyNode node = (KeyNode)keys.elementAt( pos );
            pos = node.start_position;
            element.value = element_retrieve( pos );
            return true;
        } catch ( ObjectInvalid e ){
            throw new KeyInvalid();
        } catch ( PositionInvalid e ){
            throw new KeyInvalid();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized void key(Any element, AnyHolder a_key) throws ElementInvalid{
        check_element( element );
        a_key.value = ops.key( element );
    };
/* ------------------------------------------------------------------------- */
    public synchronized void keys(Any[] elements, AnySequenceHolder a_keys) throws ElementInvalid{
        for( int i=0; i<elements.length; i++ ){
            check_element( elements[i] );
        }
        a_keys.value = new Any[ elements.length ];
        for( int i=0; i<elements.length; i++ ){
            a_keys.value[i] = ops.key( elements[i] );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_element_with_key(Any key, org.omg.CosCollection.Iterator where) throws KeyInvalid,IteratorInvalid{
        check_key( key );
        PositionalIteratorImpl i = check_iterator( where );
        test_key.key = key;
        try { 
            int pos = keys.indexOf( test_key );
            if( pos < 0 ){
                i.invalidate();
                return false;
            } 
            KeyNode node = (KeyNode)keys.elementAt( pos );
            i.set_pos( node.start_position );
            i.set_in_between( false );
            return true;
        } catch ( ObjectInvalid e ){
            throw new KeyInvalid();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_next_element_with_key(Any key, org.omg.CosCollection.Iterator where) throws KeyInvalid,IteratorInvalid{
        check_key( key );
        PositionalIteratorImpl i = check_iterator( where );
        i.check_invalid();
        int pos = i.get_pos();
        int new_pos = i.is_in_between()?pos:pos+1;
        if( data.size() <= new_pos ){
            i.invalidate();
        }
        test_key.key = key;
        Any this_key = ops.key((Any)data.elementAt( new_pos ));
        if( ops.key_equal( key, this_key ) ){
            i.set_pos( new_pos );
            i.set_in_between( false );
            return true;
        } else {
            i.invalidate();
            return false;
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_next_element_with_different_key(org.omg.CosCollection.Iterator where) throws IteratorInBetween,IteratorInvalid{
        PositionalIteratorImpl i = check_iterator( where );
        i.check_iterator();
        int pos = i.get_pos();
        Any key = ops.key((Any)data.elementAt( pos ) );
        test_key.key = key;
        try { 
            int key_pos = keys.indexOf( test_key );
            if( key_pos == keys.size()-1 ){
                i.invalidate();
                return false;
            } else {
                KeyNode node = (KeyNode)keys.elementAt( key_pos+1 );
                i.set_pos( node.start_position );
                return true;
            }
        } catch ( ObjectInvalid e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized int number_of_different_keys(){
        return keys.size();
    };
/* ------------------------------------------------------------------------- */
    public synchronized int number_of_elements_with_key(Any key) throws KeyInvalid{
        check_key( key );
        test_key.key = key;
        try {
            int pos = keys.indexOf( test_key );
            if( pos < 0 ){
                return 0;
            };
            KeyNode node = (KeyNode)keys.elementAt( pos );
            return node.count;
        } catch ( ObjectInvalid e ){
            throw new KeyInvalid();
        }
    };
/* ------------------------------------------------------------------------- */
// ----- KeySorted -----
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_first_element_with_key(Any key, org.omg.CosCollection.Iterator where) throws KeyInvalid,IteratorInvalid{
        check_key( key );
        PositionalIteratorImpl i = check_iterator( where );
        try { 
            test_key.key = key;
            int pos = keys.indexOf( test_key );
            if( pos >= 0 ){
                KeyNode node = (KeyNode)keys.elementAt( pos );
                i.set_pos( node.start_position );
                i.set_in_between( false );
                return true;
            } else {
                i.invalidate();
                return false;
            }
        } catch ( ObjectInvalid e ){
            throw new KeyInvalid();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_last_element_with_key(Any key, org.omg.CosCollection.Iterator where) throws KeyInvalid,IteratorInvalid{
        check_key( key );
        PositionalIteratorImpl i = check_iterator( where );
        try { 
            test_key.key = key;
            int pos = keys.indexOf( test_key );
            if( pos >= 0 ){
                KeyNode node = (KeyNode)keys.elementAt( pos );
                i.set_pos( node.start_position + node.count - 1 );
                i.set_in_between( false );
                return true;
            } else {
                i.invalidate();
                return false;
            }
        } catch ( ObjectInvalid e ){
            throw new KeyInvalid();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_previous_element_with_key(Any key, org.omg.CosCollection.Iterator where) throws KeyInvalid,IteratorInvalid{
        check_key( key );
        PositionalIteratorImpl i = check_iterator( where );
        i.check_invalid();
        int pos = i.get_pos();
        if( pos == 0 ){
            i.invalidate();
            return false;
        }
        Any element = (Any)data.elementAt( pos-1 );
        if( ops.key_equal( key, ops.key( element ) ) ){
            i.set_pos( pos-1 );
            i.set_in_between( false );
            return true;
        }
        i.invalidate();
        return false;
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean locate_previous_element_with_different_key(org.omg.CosCollection.Iterator where) throws IteratorInBetween,IteratorInvalid{
        PositionalIteratorImpl i = check_iterator( where );
        i.check_iterator();
        int pos = i.get_pos();
        Any key = ops.key( (Any)data.elementAt( pos ) );
        if( pos-- == 0 ){
            i.invalidate();
            return false;
        }
        while( pos >= 0 ) { 
            Any element = (Any)data.elementAt( pos );
            if( !ops.key_equal( key, ops.key( element ) ) ){
                i.set_pos( pos );
                i.set_in_between( false );
                return true;
            }
            pos--;
        }
        i.invalidate();
        return false;
    };
/* ========================================================================= */
/* ========================================================================= */
    protected void element_inserted( int pos ) {
        super.element_inserted( pos );
        Any key = ops.key( (Any)data.elementAt(pos) );
        key_inserted( key, pos );
    };
/* ------------------------------------------------------------------------- */
    protected void element_removed( int pos, Any old ){
        super.element_removed( pos, old );
        Any key = ops.key( old );
        key_removed( key );
    };
/* ------------------------------------------------------------------------- */
    protected void element_replaced( int pos, Any old ){
        super.element_replaced( pos, old );
        Any old_key = ops.key( old );
        Any new_key = ops.key( (Any)data.elementAt(pos) );
        if( !ops.equal( old_key, new_key ) ){
            key_removed( old_key );
            key_inserted( new_key, pos );
        }
    };
/* ------------------------------------------------------------------------- */
    protected void check_key( Any key ) throws KeyInvalid {
        if( !ops.check_key_type( key ) ){
            throw new KeyInvalid();
        }
    }; 
/* ------------------------------------------------------------------------- */
    protected void key_inserted( Any key, int pos ){
        try {
            test_key.key = key;
            int key_pos = keys.indexOf( test_key );
            KeyNode node;
            if( key_pos == -1 ){
                node = new KeyNode();
                node.key = key;
                node.count = 0;
                key_pos = keys.addElement( node );
                node.start_position = pos;
            } else {
                node = (KeyNode)keys.elementAt( key_pos );
            }
            node.count++;
            for( int i=key_pos+1; i<keys.size(); i++ ){
                node = (KeyNode)keys.elementAt( i );
                node.start_position++;
            }
        } catch ( ObjectInvalid e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* ------------------------------------------------------------------------- */
    protected void key_removed( Any key ){
        try {
            test_key.key = key;
            int key_pos = keys.indexOf( test_key );
            KeyNode node = (KeyNode)keys.elementAt( key_pos );
            node.count--;
            for( int i=key_pos+1; i<keys.size(); i++ ){
                node = (KeyNode)keys.elementAt( i );
                node.start_position--;
            }
            if( node.count == 0 ){
                keys.removeElementAt( key_pos );
            }
        } catch ( ObjectInvalid e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* ------------------------------------------------------------------------- */
};






