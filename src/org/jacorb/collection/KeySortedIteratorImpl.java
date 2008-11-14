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
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.KeyInvalid;
import org.omg.CosCollection.KeySortedIteratorOperations;
import org.omg.CosCollection.LowerBoundStyle;
import org.omg.CosCollection.UpperBoundStyle;

class KeySortedIteratorImpl extends OrderedIteratorImpl 
                            implements KeySortedIteratorOperations {
    KeyNode test_key = new KeyNode();
    KeySortedCollectionImpl key_collection;
/* ========================================================================= */
    KeySortedIteratorImpl( KeySortedCollectionImpl collection ){
        super( collection );
        key_collection = collection;
    }
/* ------------------------------------------------------------------------- */
    KeySortedIteratorImpl( KeySortedCollectionImpl collection, boolean read_only ){
        super( collection, read_only );
        key_collection = collection;
    }
/* ------------------------------------------------------------------------- */
    KeySortedIteratorImpl( KeySortedCollectionImpl collection, boolean read_only, boolean reverse ){
        super( collection, read_only, reverse );
        key_collection = collection;
    }
/* ========================================================================= */
/* ------------------------------------------------------------------------- */
// ---- Key Iterator 
/* ------------------------------------------------------------------------- */
    public boolean set_to_element_with_key( Any key ) throws KeyInvalid {
        synchronized( collection ){
            key_collection.check_key( key );
            test_key.key = key;
            try {
                int pos = key_collection.keys.indexOf( key );
                if( pos >=0 ){ 
                    KeyNode node = (KeyNode)key_collection.keys.elementAt( pos );
                    set_pos( node.start_position );
                    set_in_between( false );
                    return true;
                }
                invalidate();
                return false;
            } catch ( ObjectInvalid e ) {
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean set_to_next_element_with_key( Any key ) throws IteratorInvalid,KeyInvalid {
        synchronized( collection ){
            check_invalid();
            key_collection.check_key( key );
            test_key.key = key;
            try {
                int pos = key_collection.keys.indexOf( key );
                if( pos >=0 ){ 
                    KeyNode node = (KeyNode)key_collection.keys.elementAt( pos );
                    int start_pos = is_in_between()?get_pos():get_pos()+1;
                    if( start_pos <= node.start_position || start_pos < node.start_position + node.count ){
                        set_pos( start_pos );
                        set_in_between( false );
                        return true;
                    }
                }
                invalidate();
                return false;
            } catch ( ObjectInvalid e ) {
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean set_to_next_element_with_different_key() throws IteratorInBetween, IteratorInvalid {
        synchronized( collection ){
            check_iterator();
            Any key = collection.ops.key( (Any)collection.data.elementAt( get_pos() ) );
            test_key.key = key;
            try {
                int pos = key_collection.keys.indexOf( key );
                if( pos >=0 && pos < key_collection.keys.size()-1 ){ 
                    pos++;
                    KeyNode node = (KeyNode)key_collection.keys.elementAt( pos );
                    set_pos( node.start_position );
                    set_in_between( false );
                    return true;
                }
                invalidate();
                return false;
            } catch ( ObjectInvalid e ) {
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean retrieve_key( AnyHolder key ) throws IteratorInBetween, IteratorInvalid {
        synchronized( collection ){
            check_iterator();
            key.value = collection.ops.key( (Any)collection.data.elementAt( get_pos() ) );
            return true;
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean retrieve_next_n_keys( AnySequenceHolder keys ) throws IteratorInBetween, IteratorInvalid {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
/* ------------------------------------------------------------------------- */
// ---- Key Sorted Iterator 
/* ------------------------------------------------------------------------- */
    public boolean set_to_first_element_with_key( Any key, LowerBoundStyle style) throws KeyInvalid {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
/* ------------------------------------------------------------------------- */
    public boolean set_to_last_element_with_key( Any key, UpperBoundStyle style) throws KeyInvalid {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
/* ------------------------------------------------------------------------- */
    public boolean set_to_previous_element_with_key( Any key ) throws IteratorInvalid, KeyInvalid {
        synchronized( collection ){
            check_invalid();
            key_collection.check_key( key );
            test_key.key = key;
            try {
                int pos = key_collection.keys.indexOf( key );
                if( pos >=0 ){ 
                    KeyNode node = (KeyNode)key_collection.keys.elementAt( pos );
                    int start_pos = get_pos()-1;
                    if( start_pos <= node.start_position || start_pos < node.start_position + node.count ){
                        set_pos( start_pos );
                        set_in_between( false );
                        return true;
                    }
                }
                invalidate();
                return false;
            } catch ( ObjectInvalid e ) {
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean set_to_previous_element_with_different_key() throws IteratorInBetween, IteratorInvalid {
        synchronized( collection ){
            check_iterator();
            Any key = collection.ops.key( (Any)collection.data.elementAt( get_pos() ) );
            test_key.key = key;
            try {
                int pos = key_collection.keys.indexOf( key );
                if( pos > 0 ){ 
                    pos--;
                    KeyNode node = (KeyNode)key_collection.keys.elementAt( pos );
                    set_pos( node.start_position );
                    set_in_between( false );
                    return true;
                }
                invalidate();
                return false;
            } catch ( ObjectInvalid e ) {
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean retrieve_previous_n_keys( AnySequenceHolder keys ) throws IteratorInBetween, IteratorInvalid {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}




