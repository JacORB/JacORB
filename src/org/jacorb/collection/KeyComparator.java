package org.jacorb.collection;

import org.omg.CosCollection.*;
import org.jacorb.collection.util.*;

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







