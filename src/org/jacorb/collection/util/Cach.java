package org.jacorb.collection.util;

import java.util.*;

public class Cach{
/* ------------------------------------------------------------------------- */
    class Node {
        Object key;
        Object element;
        Node( Object key, Object element ){
            this.key =key;
            this.element = element;
        }
    }
/* ------------------------------------------------------------------------- */
    private Vector data;
    private int capacity;
/* ------------------------------------------------------------------------- */
    public Cach( int capacity ){
        data = new Vector( capacity );
        this.capacity = capacity;
    }
/* ------------------------------------------------------------------------- */
    public Object getElement( Object key ){
        Enumeration enum = data.elements();
        while( enum.hasMoreElements() ){
            Node n =(Node)enum.nextElement();
            if( n.key == key ){
                data.removeElement( n );
                data.insertElementAt( n, 0 );
                return n.element;
            }
        }
        return null;
    };
/* ------------------------------------------------------------------------- */
    public void putElement( Object key, Object element ){
        if( data.size() >= capacity ){
            data.removeElementAt( data.size()-1 );
        }
        Node n = new Node( key, element );
        data.insertElementAt( n, 0 );
    };
/* ------------------------------------------------------------------------- */
    public void clear(){
        data.removeAllElements();
    };






