/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.portable;

public interface ValueFactory {

    java.io.Serializable read_value(org.omg.CORBA_2_3.portable.InputStream is);
}
