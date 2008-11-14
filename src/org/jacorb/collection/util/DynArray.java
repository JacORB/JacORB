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
package org.jacorb.collection.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class DynArray 
{
    protected Object elementData[];
    protected int elementCount = 0;
    public DynArray(int initialCapacity) {
	this.elementData = ArrayFactory.get_array( initialCapacity );
    }
    public DynArray() {
	this.elementData = new Object[16];
    }
    public void copyInto(Object anArray[]) {
        System.arraycopy( elementData, 0, anArray, 0, elementCount );
    }

    public void ensureCapacity(int minCapacity) {
	if (minCapacity > elementData.length) {
	    ensureCapacityHelper(minCapacity);
	}
    }

    private void ensureCapacityHelper(int minCapacity) {
        Object [] oldData = elementData;
	elementData = ArrayFactory.get_array( minCapacity );
	System.arraycopy(oldData, 0, elementData, 0, elementCount);
        ArrayFactory.free_array( oldData );
    }
    
    public void setSize(int newSize) {
	if (newSize > elementCount) {
            if (newSize > elementData.length) {
	        ensureCapacityHelper(newSize);
            } 
	    for (int i = newSize ; i >= elementCount ;) {
		elementData[--i] = null;
	    }
	} else {
            for(int i = newSize; i<elementCount; i++ ){
                elementData[i] = null;
            }
	}
        elementCount = newSize;
    }

    public int capacity() {
	return elementData.length;
    }

    public int size() {
	return elementCount;
    }

    public boolean isEmpty() {
	return elementCount == 0;
    }

    public  Enumeration elements() {
	return new DynArrayEnumerator(this);
    }
    
    public boolean contains(Object elem) {
	return indexOf(elem, 0) >= 0;
    }

    public int indexOf(Object elem) {
	return indexOf(elem, 0);
    }

    public int indexOf(Object elem, int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index);
	}
	for (int i = index ; i < elementCount ; i++) {
	    if (elem.equals(elementData[i])) {
		return i;
	    }
	}
	return -1;
    }

    public Object elementAt(int index) {
        return elementData[index];
    }

    public void setElementAt(Object obj, int index) {
	if (index >= elementCount) {
	    throw new ArrayIndexOutOfBoundsException(index + " >= " + 
						     elementCount);
	}
	elementData[index] = obj;
    }

    public void removeElementAt(int index) {
	if (index >= elementCount  || index < 0) {
	    throw new ArrayIndexOutOfBoundsException(index);
	}
	int j = elementCount - index - 1;
	if (j > 0) {
	    System.arraycopy(elementData, index + 1, elementData, index, j);
	}
	elementCount--;
	elementData[elementCount] = null; 
    }

    public void insertElementAt(Object obj, int index) {
	int newcount = elementCount + 1;
	if (index >= newcount) {
	    throw new ArrayIndexOutOfBoundsException(index);
	}
	if (newcount > elementData.length) {
	    ensureCapacityHelper(newcount);
	}
	System.arraycopy(elementData, index, elementData, index + 1, elementCount - index);
	elementData[index] = obj;
	elementCount++;
    }

    public void addElement(Object obj) {
	int newcount = elementCount + 1;
	if (newcount > elementData.length) {
	    ensureCapacityHelper(newcount);
	}
	elementData[elementCount++] = obj;
    }

    public boolean removeElement(Object obj) {
	int i = indexOf(obj);
	if (i >= 0) {
	    removeElementAt(i);
	    return true;
	}
	return false;
    }

    public void removeAllElements() {
	for (int i = 0; i < elementCount; i++) {
	    elementData[i] = null;
	}
	elementCount = 0;
    }

    public Object firstElement() {
	if (elementCount == 0) {
	    throw new NoSuchElementException();
	}
	return elementData[0];
    }

    public Object lastElement() {
	if (elementCount == 0) {
	    throw new NoSuchElementException();
	}
	return elementData[elementCount - 1];
    }

    protected void finalize(){
        if( elementData != null ){
            ArrayFactory.free_array( elementData );
        }
    }
}

final
class DynArrayEnumerator implements java.util.Enumeration {
    Object [] data;
    int count;
    int elementCount;
    DynArrayEnumerator(DynArray da) {
	data = ArrayFactory.get_array( da.elementCount );
        elementCount = da.elementCount;
        System.arraycopy(da.elementData, 0, data, 0, da.elementCount );
	count = 0;
    }

    public boolean hasMoreElements() {
	return count < elementCount;
    }

    public Object nextElement() {
        if (count < elementCount) {
            return data[count++];
	}
        if( data != null ){
            ArrayFactory.free_array( data );
        }
	throw new NoSuchElementException("DynArrayEnumerator");
    }
    protected void finalize(){
        if( data != null ){
            ArrayFactory.free_array( data );
        }
    }
}






