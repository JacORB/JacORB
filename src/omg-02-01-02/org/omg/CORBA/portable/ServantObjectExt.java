/***** Copyright (c) 2002 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

package org.omg.CORBA.portable;

abstract public class ServantObjectExt extends ServantObject {
    abstract public void normalCompletion() ;

    abstract public void exceptionalCompletion( Throwable thr ) ;
}
