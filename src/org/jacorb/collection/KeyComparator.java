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

import org.jacorb.collection.util.ObjectComparator;
import org.jacorb.collection.util.ObjectInvalid;
import org.omg.CosCollection.OperationsOperations;

class KeyComparator implements ObjectComparator {
    private KeyNode current = null;
    private OperationsOperations ops;
    KeyComparator( OperationsOperations ops ){
        this.ops = ops;
    };
    public int compare( Object obj1, Object obj2 ) throws ObjectInvalid{
        if( obj1 == null || obj2 == null ){
            throw new ObjectInvalid();
        }
        check_object( obj1 );
        check_object( obj2 );
        return ops.key_compare( ((KeyNode)obj1).key, ((KeyNode)obj2).key );
    };
    public void element( Object obj ) throws ObjectInvalid{
        check_object( obj );
        current = (KeyNode)obj;
    };
    public Object element(){
        return current;
    };
    public int compare_with( Object obj ) throws ObjectInvalid{
        if( current == null || obj == null ){
            throw new ObjectInvalid();
        }
        check_object( obj );
        return ops.key_compare( current.key, ((KeyNode)obj).key );
    };
    public boolean equal( Object obj1, Object obj2 ) throws ObjectInvalid{
        if( obj1 == null || obj2 == null ){
            throw new ObjectInvalid();
        }
        check_object( obj1 );
        check_object( obj2 );
        return ops.key_equal( ((KeyNode)obj1).key, ((KeyNode)obj2).key );
    };
    public boolean equal( Object obj ) throws ObjectInvalid{
        if( current == null || obj == null ){
            throw new ObjectInvalid();
        }
        check_object( obj );
        return ops.key_equal( current.key, ((KeyNode)obj).key );
    };
    private void check_object( Object obj ) throws ObjectInvalid {
        if( !( obj instanceof KeyNode ) ){
            throw new ObjectInvalid();
        }
    };
};







