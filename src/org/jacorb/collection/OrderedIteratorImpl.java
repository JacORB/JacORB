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

import java.util.Vector;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CORBA.BooleanHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInvalidReason;
import org.omg.CosCollection.OrderedIteratorOperations;
import org.omg.CosCollection.PositionInvalid;

class OrderedIteratorImpl extends PositionalIteratorImpl 
                          implements OrderedIteratorOperations {
/* ========================================================================= */
    protected boolean reverse; // reverse now not supperted
/* ========================================================================= */
    OrderedIteratorImpl( OrderedCollectionImpl collection ){
        super( collection );
        reverse = false;
    };
/* ------------------------------------------------------------------------- */
    OrderedIteratorImpl( OrderedCollectionImpl collection, boolean read_only ){
        super( collection, read_only );
        reverse = false;
    };
/* ------------------------------------------------------------------------- */
    OrderedIteratorImpl( OrderedCollectionImpl collection, boolean read_only, boolean reverse ){
        super( collection, read_only );
        if( reverse ){
            throw new org.omg.CORBA.NO_IMPLEMENT();
        }
        this.reverse = reverse;
    };
/* ========================================================================= */
    public boolean set_to_last_element(){
        synchronized( collection ){
            set_pos( collection.data.size()-1 );
            set_in_between( false );
            return is_valid();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_previous_element() throws IteratorInvalid{
        return set_to_nth_previous_element( 1 );
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_nth_previous_element(int n) throws IteratorInvalid{
        synchronized( collection ) {
            check_invalid();
            int new_pos = get_pos()-n;
            if( collection.number_of_elements() > new_pos && new_pos >= 0 ){
                set_pos( new_pos );
            } else {
                invalidate();
                throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
            }
            set_in_between( false );
            return is_valid();
        }
    };
/* ------------------------------------------------------------------------- */
    public void set_to_position(int position) throws PositionInvalid{
        synchronized( collection ){
            if( position <0 || position >= collection.data.size() ){
                throw new PositionInvalid();
            }
            set_pos( position );
            set_in_between( false );
        };
    };
/* ------------------------------------------------------------------------- */
    public int position() throws IteratorInvalid{
        synchronized( collection ){
            check_invalid();
            return get_pos();
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean retrieve_element_set_to_previous( AnyHolder element, BooleanHolder more) throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ){
            check_iterator();
            try {
                element.value = collection.element_retrieve( pos );
            } catch ( PositionInvalid e ){
                invalidate();
                throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
            };
            more.value = (get_pos() > 0);
            return set_to_previous_element();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean retrieve_previous_n_elements(int n, AnySequenceHolder result, BooleanHolder more) throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ) {
            check_iterator();
            Vector v = new Vector(n);
            AnyHolder a = new AnyHolder();
            for( int i=0; ( i<n || n==0 ) && is_valid(); i++ ){
                 try {
                     a.value = collection.element_retrieve( get_pos() );
                     set_pos( get_pos()-1 );
                 } catch ( PositionInvalid e ){
                     invalidate();
                     throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
                 };
                 v.addElement( a.value );
                 a.value = null;
            }
            more.value = (get_pos() > 0);
            if( v.size() > 0 ){
                result.value = new Any[ v.size() ];
                v.copyInto( (java.lang.Object []) result.value );
                return true;
            } else {
                return false;
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean not_equal_retrieve_element_set_to_previous(org.omg.CosCollection.Iterator test, 
							      AnyHolder element) 
	throws IteratorInvalid,IteratorInBetween
    {
        synchronized( collection ) {
            check_iterator();
            PositionalIteratorImpl iter = collection.check_iterator( test );
            iter.check_iterator();
            if( is_equal( test ) ){
                retrieve_element( element );
                return false;
            } else {
                retrieve_element_set_to_previous( element, new org.omg.CORBA.BooleanHolder() );
                return true;
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean remove_element_set_to_previous() throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ) {
            remove_element();
            return set_to_previous_element();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean remove_previous_n_elements(int n, org.omg.CORBA.IntHolder actual_number) throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ) {
            actual_number.value = 0;
            for( int i = 0; ( i<n || n == 0 ) && is_valid(); i++, actual_number.value++ ){
                remove_element_set_to_previous();
            }
            return is_valid();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean not_equal_remove_element_set_to_previous(org.omg.CosCollection.Iterator test) 
	throws IteratorInvalid,IteratorInBetween
    {
        synchronized( collection ) {
            check_iterator();
            PositionalIteratorImpl iter = collection.check_iterator( test );
            iter.check_iterator();
            if( is_equal( test ) ){
                remove_element();
                return false;
            } else {
                remove_element_set_to_previous();
                return true;
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean replace_element_set_to_previous(org.omg.CORBA.Any element) throws IteratorInvalid,IteratorInBetween,ElementInvalid{
        synchronized( collection ) {
            replace_element( element );
            return set_to_previous_element();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean replace_previous_n_elements(org.omg.CORBA.Any[] elements, org.omg.CORBA.IntHolder actual_number) throws IteratorInvalid,IteratorInBetween,ElementInvalid{
        synchronized( collection ) {
            actual_number.value = 0;
            for( int i=0; i<elements.length && is_valid(); i++,actual_number.value++ ){
                replace_element_set_to_previous( elements[i] );
            }
            return is_valid();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean not_equal_replace_element_set_to_previous(org.omg.CosCollection.Iterator test, Any element) throws IteratorInvalid,IteratorInBetween,ElementInvalid{
        synchronized( collection ) {
            check_iterator();
            PositionalIteratorImpl iter = collection.check_iterator( test );
            iter.check_iterator();
            if( is_equal( test ) ){
                replace_element( element );
                return false;
            } else {
                replace_element_set_to_previous( element );
                return true;
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean is_first(){
        return get_pos() == 0;
    };
/* ------------------------------------------------------------------------- */
    public boolean is_last(){
        synchronized( collection ){
            return collection.data.size()-1 == get_pos();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean is_for_same(org.omg.CosCollection.Iterator test){
        synchronized( collection ){
            try {
                collection.check_iterator( test );
                return true;
            } catch ( IteratorInvalid e ){
                return false;
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean is_reverse(){
        return reverse;
    };
/* ========================================================================= */
};






