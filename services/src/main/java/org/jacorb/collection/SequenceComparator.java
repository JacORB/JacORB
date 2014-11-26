/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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

import org.jacorb.collection.util.ObjectComparator;
import org.jacorb.collection.util.ObjectInvalid;
import org.omg.CORBA.Any;
import org.omg.CosCollection.OperationsOperations;

class SequenceComparator implements ObjectComparator {
    private OperationsOperations ops;
    private Any current = null;
/* ------------------------------------------------------------------------- */
    SequenceComparator( OperationsOperations ops ) {
        this.ops = ops;
    }
/* ------------------------------------------------------------------------- */
    public synchronized int compare( Object obj1, Object obj2 ) throws ObjectInvalid {
        if( obj1 == null || obj2 == null ){
            throw new ObjectInvalid();
        }
        check_object( obj1 );
        check_object( obj2 );
        return ops.compare( (Any)obj1, (Any)obj2 );
    }
/* ------------------------------------------------------------------------- */
    public synchronized void element( Object obj ) throws ObjectInvalid {
        check_object( obj );
        current = (Any) obj;
    }
/* ------------------------------------------------------------------------- */
    public synchronized Object element() {
        return current;
    }
/* ------------------------------------------------------------------------- */
    public synchronized int compare_with( Object obj ) throws ObjectInvalid {
        if( obj == null || current == null ) {
            throw new ObjectInvalid();
        }
        check_object( obj );
        return ops.compare( current, (Any)obj );
    }
/* ------------------------------------------------------------------------- */
    public synchronized boolean equal( Object obj1, Object obj2 ) throws ObjectInvalid {  
        if( obj1 == null || obj2 == null ){
            throw new ObjectInvalid();
        }
        check_object( obj1 );
        check_object( obj2 );
        return ops.equal( (Any)obj1, (Any)obj2 );
    }
/* ------------------------------------------------------------------------- */
    public synchronized boolean equal( Object obj1 ) throws ObjectInvalid {
        if( obj1 == null || current == null ) {
            throw new ObjectInvalid();
        }
        check_object( obj1 );
        return ops.equal( current, (Any)obj1 );
    }
/* ------------------------------------------------------------------------- */
    private void check_object( Object obj ) throws ObjectInvalid {
        if( !( obj instanceof Any )
            || !((Any)obj).type().equal( ops.element_type() ) ){
            throw new ObjectInvalid();
        }
    }

}









