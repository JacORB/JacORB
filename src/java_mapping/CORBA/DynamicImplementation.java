package org.omg.CORBA;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * 
 * @deprecated org.omg.CORBA.DynamicImplementation
 *
 * last modified: 02/03/99 RT
 */
public class DynamicImplementation extends org.omg.CORBA.portable.ObjectImpl {

	/** 
	 * @deprecated Deprecated by Portable Object Adapter
	 */
	public void invoke(org.omg.CORBA.ServerRequest request) {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	/** 
	 * @deprecated
	 */
	public String[] _ids() {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}
} 


