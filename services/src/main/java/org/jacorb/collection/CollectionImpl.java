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

import java.util.Enumeration;
import org.jacorb.collection.util.DynArray;
import org.jacorb.collection.util.ObjectInvalid;
import org.jacorb.collection.util.SortedVector;
import org.omg.CORBA.Any;
import org.omg.CORBA.AnyHolder;
import org.omg.CosCollection.Command;
import org.omg.CosCollection.ElementInvalid;
import org.omg.CosCollection.ElementInvalidReason;
import org.omg.CosCollection.EmptyCollection;
import org.omg.CosCollection.IteratorHelper;
import org.omg.CosCollection.IteratorInBetween;
import org.omg.CosCollection.IteratorInvalid;
import org.omg.CosCollection.IteratorInvalidReason;
import org.omg.CosCollection.IteratorPOATie;
import org.omg.CosCollection.OperationsOperations;
import org.omg.CosCollection.PositionInvalid;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

class CollectionImpl 
{
    protected DynArray iterators = new DynArray();
    protected POA poa; 
    protected OperationsOperations ops;
    protected IteratorFactory iterator_factory;
    protected SortedVector data;
    private   Servant srvnt = null;

/* ========================================================================= */
    CollectionImpl( OperationsOperations ops, POA poa, IteratorFactory iterator_factory ){
        this.poa = poa;
        this.ops = ops;
        this.iterator_factory = iterator_factory;
    };
/* ------------------------------------------------------------------------- */
    public synchronized org.omg.CORBA.TypeCode element_type() {
        return ops.element_type();
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean add_element(Any element) throws ElementInvalid {
        element_add( element );
        return true;
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean add_element_set_iterator(Any element, org.omg.CosCollection.Iterator where) throws IteratorInvalid,ElementInvalid {
        PositionalIteratorImpl i = check_iterator( where );
        int pos = element_add( element );
        i.set_pos( pos );
        i.set_in_between( false );
        return true;
    };
/* ------------------------------------------------------------------------- */
    public synchronized void add_all_from(org.omg.CosCollection.Collection collector) 
	throws ElementInvalid 
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
/* ------------------------------------------------------------------------- */
    public synchronized void remove_element_at(org.omg.CosCollection.Iterator where) 
	throws IteratorInvalid,IteratorInBetween 
    {
        PositionalIteratorImpl i = check_iterator( where );
        if( i.is_in_between() ){
            throw new IteratorInBetween();
        }
        int pos = i.get_pos();
        try {
            element_remove( pos );
        } catch ( PositionInvalid e ){
            i.invalidate();
            throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
        } catch ( EmptyCollection e ){
            i.invalidate();
            throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized int remove_all() {
        Enumeration enumeration = iterators.elements();
        while( enumeration.hasMoreElements() ){
            PositionalIteratorImpl i = (PositionalIteratorImpl)enumeration.nextElement();
            i.invalidate();
        }
        int count = data.size();
        data.removeAllElements();
        return count;
    };
/* ------------------------------------------------------------------------- */
    public synchronized void replace_element_at(org.omg.CosCollection.Iterator where, Any element) throws IteratorInvalid,IteratorInBetween,ElementInvalid {
        PositionalIteratorImpl i = check_iterator( where );
        if( i.is_in_between() ){
            throw new IteratorInBetween();
        }
        int pos = i.get_pos();
        try {
            element_replace( pos, element );
        } catch ( PositionInvalid e ){
            i.invalidate();
            throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean retrieve_element_at(org.omg.CosCollection.Iterator where, AnyHolder element) throws IteratorInvalid,IteratorInBetween {
        PositionalIteratorImpl i = check_iterator( where );
        if( i.is_in_between() ){
            throw new IteratorInBetween();
        }
        int pos = i.get_pos();
        try{
            element.value = element_retrieve( pos );
            return true;
        } catch ( PositionInvalid e ){
            i.invalidate();
            throw new IteratorInvalid( IteratorInvalidReason.is_invalid );
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean all_elements_do(Command what) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    };
/* ------------------------------------------------------------------------- */
    public synchronized int number_of_elements() {
        return data.size();
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean is_empty() {
        return data.size() == 0;
    };
/* ------------------------------------------------------------------------- */
    public synchronized void destroy() {
        Enumeration enumeration = iterators.elements();
        while( enumeration.hasMoreElements() ){
            PositionalIteratorImpl i = (PositionalIteratorImpl)enumeration.nextElement();
            i.destroy();
        };
        try {
            byte[] ObjID = poa.servant_to_id( srvnt );
            poa.deactivate_object( ObjID );
        } catch ( Exception e ) {
            System.out.println( "Internal error: Can not deactivate object" );
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized org.omg.CosCollection.Iterator create_iterator(boolean read_only) {
        PositionalIteratorImpl iter = iterator_factory.create_iterator( this, read_only );
        IteratorPOATie servant = new IteratorPOATie( iter );
        try {
            org.omg.CosCollection.Iterator i = IteratorHelper.narrow( poa.servant_to_reference( servant ));
            iter.set_servant( servant );
            return i;
        } catch ( Exception e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* ========================================================================= */
    public synchronized int  element_add( Any element ) throws ElementInvalid {
        check_element( element );
        try {
            int pos = data.addElement( element );
            element_inserted( pos );
            return pos;
        } catch ( ObjectInvalid e ){
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized void element_remove( int pos ) throws PositionInvalid, EmptyCollection {
        if( data.size() == 0 ){
            throw new EmptyCollection();
        }
        if( pos < 0 || pos >= data.size() ){
            throw new PositionInvalid();
        }
        Any old = (Any)data.elementAt( pos );
        data.removeElementAt( pos );
        element_removed( pos, old );
    };
/* ------------------------------------------------------------------------- */
    public synchronized int  element_replace( int pos, Any element ) throws PositionInvalid, ElementInvalid {
        if( pos < 0 || pos >= data.size() ){
            throw new PositionInvalid();
        }
        check_element( element );
        try {
            Any old = (Any)data.elementAt( pos );
            data.setElementAt( element, pos );
            element_replaced( pos, old );
        } catch ( ObjectInvalid e ) {
            throw new ElementInvalid( ElementInvalidReason.positioning_property_invalid );
        }
        return pos;
    };
/* ------------------------------------------------------------------------- */
    public synchronized Any  element_retrieve( int pos ) throws PositionInvalid {
        if( pos < 0 || pos >= data.size() ){
            throw new PositionInvalid();
        }
        return (Any)data.elementAt( pos );
    };
/* ------------------------------------------------------------------------- */
    public synchronized PositionalIteratorImpl check_iterator( org.omg.CosCollection.Iterator iter ) throws IteratorInvalid {
        PositionalIteratorImpl i = null;
        Enumeration enumeration=iterators.elements();
        while( enumeration.hasMoreElements() ){
            i=(PositionalIteratorImpl)enumeration.nextElement();
            try {
                if( i.get_servant() == ( poa.reference_to_servant( iter ) ) ){
                    return (PositionalIteratorImpl)i;
                }
            } catch ( Exception e ){
                System.out.println( "Internal error: Invalid POA policy or POA internal error" );
                e.printStackTrace();
                throw new org.omg.CORBA.INTERNAL();
            }
        }
        throw new IteratorInvalid( IteratorInvalidReason.is_not_for_collection );
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean is_this_you( org.omg.CosCollection.Collection col ) {
        try {
            return srvnt == poa.reference_to_servant( col );
        } catch ( Exception e ){
            System.out.println("InternalError: Can not test Object equality");
            e.printStackTrace( System.out );
            throw new org.omg.CORBA.INTERNAL();
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized void destroy_me( PositionalIteratorImpl i ) {
        if( iterators.removeElement( i ) ){
            try {
                byte [] ObjID = poa.servant_to_id( i.get_servant() );
                poa.deactivate_object( ObjID );
            } catch ( Exception e ){
                System.out.println("Internal error: Attempt destroy not my Iterator");
                e.printStackTrace( System.out );
                throw new org.omg.CORBA.INTERNAL();
            }
        } else {
            System.out.println("Internal error: Attempt destroy not my Iterator");
            throw new org.omg.CORBA.INTERNAL();
        };
    };
/* ------------------------------------------------------------------------- */
    public void check_element( Any element ) throws ElementInvalid {
        if( !ops.check_element_type(element) ){
            throw new ElementInvalid( ElementInvalidReason.element_type_invalid );
        }
    };
/* ========================================================================= */
    synchronized void set_servant( Servant srvnt ){
        this.srvnt = srvnt;
    };
/* ------------------------------------------------------------------------- */
    protected void element_inserted( int pos ) {
        Enumeration enumeration = iterators.elements();
        while( enumeration.hasMoreElements() ){
            PositionalIteratorImpl i = (PositionalIteratorImpl)enumeration.nextElement();
            if( i.is_valid() ){
                int p = i.get_pos();
                if( p >= pos ) {
                    i.set_pos( pos+1 );
                };
            };
        }
    };
/* ------------------------------------------------------------------------- */
    protected void element_removed( int pos, Any old ){
        Enumeration enumeration = iterators.elements();
        while( enumeration.hasMoreElements() ){
            PositionalIteratorImpl i = (PositionalIteratorImpl)enumeration.nextElement();
            if( i.is_valid() ){
                int p = i.get_pos();
                if( p == pos ){
                    i.set_in_between( true );
                } else if ( p > pos ){
                    i.set_pos( p-1 );
                }
            }
        }
    };
/* ------------------------------------------------------------------------- */
    protected void element_replaced( int pos, Any old ){
    };

}; 






