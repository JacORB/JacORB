package org.jacorb.collection;

import org.omg.CosCollection.OperationsOperations;
import org.omg.CORBA.Any;
import org.jacorb.collection.util.*;

class SortedRelationComparator implements ObjectComparator {
    private OperationsOperations ops;
    private Any current = null;
    private Any current_key = null;
/* ------------------------------------------------------------------------- */
    SortedRelationComparator( OperationsOperations ops ) {
        this.ops = ops;
    };
/* ------------------------------------------------------------------------- */
    public synchronized int compare( Object obj1, Object obj2 ) throws ObjectInvalid {
        if( obj1 == null || obj2 == null ){
            throw new ObjectInvalid();
        };
        check_object( obj1 );
        check_object( obj2 );
        Any key1 = ops.key( (Any)obj1 );
        Any key2 = ops.key( (Any)obj2 );
        int result = ops.key_compare( key1, key2 );
        if( result == 0 ){
            result = ops.compare( (Any)obj1, (Any)obj2 );
        }
        return result;
    };
/* ------------------------------------------------------------------------- */
    public synchronized void element( Object obj ) throws ObjectInvalid {
        check_object( obj );
        current = (Any) obj;
        if( current != null ){
            current_key = ops.key( current );
        } else {
            current_key = null;
        }
    };
/* ------------------------------------------------------------------------- */
    public synchronized Object element() {
        return current;
    };
/* ------------------------------------------------------------------------- */
    public synchronized int compare_with( Object obj ) throws ObjectInvalid {
        if( obj == null || current == null ) {
            throw new ObjectInvalid();
        }
        check_object( obj );
        Any key = ops.key( (Any)obj );
        int result = ops.key_compare( current_key, key );
        if( result == 0 ){
            result = ops.compare( current, (Any)obj );
        }
        return result;
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean equal( Object obj1, Object obj2 ) throws ObjectInvalid {  
        if( obj1 == null || obj2 == null ){
            throw new ObjectInvalid();
        };
        check_object( obj1 );
        check_object( obj2 );
        return ops.equal( (Any)obj1, (Any)obj2 );
    };
/* ------------------------------------------------------------------------- */
    public synchronized boolean equal( Object obj1 ) throws ObjectInvalid {
        if( obj1 == null || current == null ) {
            throw new ObjectInvalid();
        }
        check_object( obj1 );
        return ops.equal( current, (Any)obj1 );
    };
/* ------------------------------------------------------------------------- */
    private void check_object( Object obj ) throws ObjectInvalid {
        if( !( obj instanceof Any )
            || !((Any)obj).type().equal( ops.element_type() ) ){
            throw new ObjectInvalid();
        }
    };






