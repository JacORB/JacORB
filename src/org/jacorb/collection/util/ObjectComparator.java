package org.jacorb.collection.util;

public interface ObjectComparator{
    public int compare( Object obj1, Object obj2 ) throws ObjectInvalid;
    public void element( Object obj ) throws ObjectInvalid;
    public Object element();
    public int compare_with( Object obj ) throws ObjectInvalid;
    public boolean equal( Object obj1, Object obj2 ) throws ObjectInvalid;
    public boolean equal( Object obj1 ) throws ObjectInvalid;
}





