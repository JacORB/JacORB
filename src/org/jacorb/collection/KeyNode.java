package org.jacorb.collection;

public class KeyNode{
    public KeyNode( org.omg.CORBA.Any k ){
        key = k;
    };
    public KeyNode(){
        key = null;
    };
    public org.omg.CORBA.Any key;
    public int start_position = 0;
    public int count   = 0;
