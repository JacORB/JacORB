/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface FixedDefOperations extends org.omg.CORBA.IDLTypeOperations {

    public short digits();
    public void digits(short digits);
    public short scale();
    public void scale(short scale);

}
