/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA;

public interface ValueBoxDefOperations extends
                org.omg.CORBA.ContainedOperations,
                org.omg.CORBA.IDLTypeOperations {

    public org.omg.CORBA.IDLType original_type_def ();
    public void original_type_def (org.omg.CORBA.IDLType original_type_def);

}
