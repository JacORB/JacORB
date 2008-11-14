package org.jacorb.collection;

/*
 *        JacORB collection service
 *
 *   Copyright (C) 1999-2004 LogicLand group, Viacheslav Tararin.
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
 */

import java.util.Vector;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosCollection.AnySequenceHolder;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosCollection.EmptyCollection;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInvalidReason;
import org.omg.CosCollection.PositionInvalid;
import org.omg.PortableServer.Servant;

class PositionalIteratorImpl 
    implements org.omg.CosCollection.IteratorOperations 
{
    private Servant srvnt = null;
    protected int pos = -1;
    protected boolean in_between = false;
    protected boolean read_only = true;
    protected CollectionImpl collection;
/* ========================================================================= */
    PositionalIteratorImpl( CollectionImpl collection ){
        this.collection = collection;
    };
/* ------------------------------------------------------------------------- */
    PositionalIteratorImpl( CollectionImpl collection, boolean read_only ){
        this.collection = collection;
        this.read_only = read_only;
    };
/* ========================================================================= */
    public boolean set_to_first_element() {
        synchronized( collection ){
            check_servant();
            if( collection.is_empty() ) {
                invalidate();
            } else {
                set_pos( 0 );
            };
        };
        return get_pos() == 0;
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_next_element() throws IteratorInvalid {
        return set_to_next_nth_element( 1 );
    };
/* ------------------------------------------------------------------------- */
    public boolean set_to_next_nth_element(int n) throws IteratorInvalid {
        synchronized( collection ) {
            check_invalid();
            int new_pos = get_pos()+n;
            if( is_in_between() ){
                new_pos = get_pos()+n-1;
            }
            if( collection.number_of_elements() > new_pos && new_pos >= 0 ){
                set_pos( new_pos );
            } else {
                invalidate();
            }
            in_between = false;
            return exist_next();
        } 
    };
/* ------------------------------------------------------------------------- */
    public boolean retrieve_element(AnyHolder element) throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ){
            check_iterator();
            try {
                element.value = collection.element_retrieve( get_pos() );
                return true;
            } catch ( PositionInvalid e ){
                set_pos( -1 );
                throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean retrieve_element_set_to_next(AnyHolder element, org.omg.CORBA.BooleanHolder more) throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ){
            boolean rc = retrieve_element( element );
            set_to_next_element();
            more.value = exist_next();
            return rc;
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean retrieve_next_n_elements(int n, AnySequenceHolder result, org.omg.CORBA.BooleanHolder more) throws IteratorInvalid,IteratorInBetween{
        Vector v = new Vector( n );
        int i = 0;
        synchronized( collection ){
             check_iterator();
             boolean b = true;
             AnyHolder a = new AnyHolder();
             more.value = true;
             for ( i=0; (i<n || n==0) && get_pos() != -1; i++ ){
                 try {
                     retrieve_element_set_to_next( a, more );
                     v.addElement(a.value);
                 } catch ( IteratorInvalid e ){
                     more.value = false;
                     break;
                 }
             }
        }
        Any [] anies = new Any[v.size()];
        v.copyInto( (java.lang.Object [])anies );
        result.value = anies;
        return i>0;
    };
/* ------------------------------------------------------------------------- */
    public boolean not_equal_retrieve_element_set_to_next(org.omg.CosCollection.Iterator test, AnyHolder element) throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ){
            check_iterator();
            if( is_equal( test ) ){
                retrieve_element( element );
                return false;
            } else {
                retrieve_element( element );
                set_to_next_element();
                return true;
            }
        }
    }
/* ------------------------------------------------------------------------- */
    public void remove_element() throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ){
            check_iterator();
            check_read_only();
            try {
                collection.element_remove( get_pos() );
            } catch ( PositionInvalid e ){
                invalidate();
                throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
            } catch ( EmptyCollection e ){
                invalidate();
                throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
           };
        };
    };
/* ------------------------------------------------------------------------- */
    public boolean remove_element_set_to_next() throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ) {
            remove_element();
            return set_to_next_element();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean remove_next_n_elements(int n, org.omg.CORBA.IntHolder actual_number) throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ){
            int count = 0;
            for( int i=0; ( i<n || n==0 ) && get_pos() != -1; i++, count++ ){
                remove_element_set_to_next();
            }
            actual_number.value = count;
            return exist_next();
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean not_equal_remove_element_set_to_next(org.omg.CosCollection.Iterator test) 
	throws IteratorInvalid,IteratorInBetween{
        synchronized( collection ){
            check_iterator();
            if( is_equal( test ) ){
                remove_element();
                return false;
            } else {
                remove_element_set_to_next();
                return true;
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public void replace_element(Any element) throws IteratorInvalid,IteratorInBetween,ElementInvalid{
        synchronized( collection ){
            check_iterator();
            check_read_only();
            try {
                collection.element_replace( get_pos(), element );
            } catch ( PositionInvalid e ){
                invalidate();
                throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
            }
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean replace_element_set_to_next(Any element) throws IteratorInvalid,IteratorInBetween,ElementInvalid{
        synchronized( collection ){
            replace_element( element );
            return set_to_next_element();
        }
    };
/* ------------------------------------------------------------------------- */
    public boolean replace_next_n_elements(Any[] elements, org.omg.CORBA.IntHolder actual_number) throws IteratorInvalid,IteratorInBetween,ElementInvalid{
        synchronized( collection ){
            actual_number.value = 0;
            for( int i=0; i<elements.length && is_valid() ; i++, actual_number.value++){
                replace_element_set_to_next( elements[i] );
            }
            return exist_next();
        } 
    };
/* ------------------------------------------------------------------------- */
    public boolean not_equal_replace_element_set_to_next(org.omg.CosCollection.Iterator test, Any element) throws IteratorInvalid,IteratorInBetween,ElementInvalid{
        synchronized( collection ){
            check_iterator();
            if( is_equal( test ) ){
                replace_element( element );
                return false;
            } else {
                replace_element_set_to_next( element );
                return true;
            }
        }
    }
/* ------------------------------------------------------------------------- */
    public boolean add_element_set_iterator(Any element) throws ElementInvalid{
        synchronized( collection ){
            pos = collection.element_add( element );
            set_pos( pos );
            in_between = false;
            return true;
        } 
    };
/* ------------------------------------------------------------------------- */
    public boolean add_n_elements_set_iterator(Any[] elements, org.omg.CORBA.IntHolder actual_number) throws ElementInvalid{
        synchronized( collection ){
            actual_number.value = 0;
            int pos[] = new int[ elements.length ];
            for( int i=0; i<elements.length; i++ ){
                collection.check_element( elements[i] );
            }
            for( int i=0; i<elements.length; i++, actual_number.value++ ){
                add_element_set_iterator( elements[i] );
            }
            return true;
        } 
    };
/* ------------------------------------------------------------------------- */
    public synchronized void invalidate(){
        pos = -1;
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean is_valid(){
        return pos != -1;
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean is_in_between(){
        return in_between;
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean is_for(org.omg.CosCollection.Collection collector){
        return collection.is_this_you( collector );
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean is_const(){
        return read_only;
    };
/* ------------------------------------------------------------------------- */
    public boolean is_equal(org.omg.CosCollection.Iterator test) throws IteratorInvalid{
        synchronized( collection ){
            PositionalIteratorImpl iter = collection.check_iterator( test );
            if( !is_valid() ){
                throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
            }
            return get_pos() == iter.get_pos();
        }
    }
/* ------------------------------------------------------------------------- */
    public org.omg.CosCollection.Iterator _clone(){
        synchronized( collection ) {
            org.omg.CosCollection.Iterator iter = collection.create_iterator( read_only );
            try {
                PositionalIteratorImpl i = collection.check_iterator( iter );
                i.set_pos( get_pos() );
                return iter;
            } catch ( IteratorInvalid e ){
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        }
    }
/* ------------------------------------------------------------------------- */
    public void assign(org.omg.CosCollection.Iterator from_where) throws IteratorInvalid{
        synchronized( collection ) {
            PositionalIteratorImpl i = collection.check_iterator( from_where );
            i.set_pos( get_pos() );
            i.set_in_between( is_in_between() );
        };
    };
/* ------------------------------------------------------------------------- */
    public synchronized void destroy(){
        synchronized( collection ) { 
            check_servant();
            collection.destroy_me( this );
        }
    };
/* ========================================================================= */
    public synchronized int get_pos(){
        check_servant();
        return pos;
    };
/* ------------------------------------------------------------------------- */
    public synchronized void set_pos( int pos ){
        check_servant();
        this.pos = pos;
    };
/* ------------------------------------------------------------------------- */
    public synchronized void set_in_between( boolean in_between ){
        check_servant();
        this.in_between = in_between;
    };
/* ------------------------------------------------------------------------- */
    public synchronized void set_servant( Servant srvnt ){
        if( srvnt != null ){
            System.out.println("Error: Servant setted before!");
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized Servant get_servant(){
        if( srvnt != null ){
            System.out.println("Error: Servant must be setted before!");
            throw new org.omg.CORBA.INTERNAL();
        }
        return srvnt;
    };
/* ========================================================================= */
    protected void check_servant() {
        if( srvnt == null ){
            System.out.println("Error: Servant must be setted before!");
            throw new org.omg.CORBA.INTERNAL();
        }
    }
/* -------------------------------------------------------------------------- */
    protected void check_invalid() throws IteratorInvalid {
        check_servant();
        if( pos == -1 ){
            throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
        }
    }
/* -------------------------------------------------------------------------- */
    protected void check_in_between() throws IteratorInBetween {
        if( in_between ){
            throw new IteratorInBetween();
        }
    }
/* -------------------------------------------------------------------------- */
    protected void check_iterator() throws IteratorInBetween, IteratorInvalid {
        check_invalid();
        check_in_between();
    };
/* -------------------------------------------------------------------------- */
    protected void check_read_only() throws IteratorInvalid {
        if( read_only ){
            throw new IteratorInvalid( IteratorInvalidReason.is_const );
        }
    }
/* -------------------------------------------------------------------------- */
    protected synchronized boolean exist_next(){
        return is_valid() && collection.number_of_elements()-1 > get_pos();
    };
};






