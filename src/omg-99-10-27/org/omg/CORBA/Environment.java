/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public abstract class Environment  {
    abstract public void exception(java.lang.Exception exception);
    abstract public java.lang.Exception exception();
    abstract public void clear();
}
