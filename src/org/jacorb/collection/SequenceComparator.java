package org.jacorb.collection;

import org.omg.CosCollection.OperationsOperations;
import org.omg.CORBA.Any;
import org.jacorb.collection.util.*;

class SequenceComparator implements ObjectComparator {
    private OperationsOperations ops;
    private Any current = null;
/* ------------------------------------------------------------------------- */
    SequenceComparator( OperationsOperations ops ) {
        this.ops = ops;
    };
/* ------------------------------------------------------------------------- */
    public synchronized int compare( Object obj1, Object obj2 ) throws ObjectInvalid {
        if( obj1 == null || obj2 == null ){
            throw new ObjectInvalid();
        };
        check_object( obj1 );
        check_object( obj2 );
        return ops.compare( (Any)obj1, (Any)obj2 );
    };
/* ------------------------------------------------------------------------- */
    public synchronized void element( Object obj ) throws ObjectInvalid {
        check_object( obj );
        current = (Any) obj;
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
        return ops.compare( current, (Any)obj );
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






