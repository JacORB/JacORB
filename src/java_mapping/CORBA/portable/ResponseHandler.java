package org.omg.CORBA.portable;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version)
 * last modified: 02/03/99 RT
 */
public interface ResponseHandler {

	/**
	 * Called by servant during a method invocation. The servant should call
	 * this method to create a reply marshal buffer if no except occurred
	 *
	 * Returns an OutputStream suitable for marshalling reply.
	 */
	OutputStream createReply();

	/**
	 * Called by servant during a method invocation. The servant should call
	 * this method to create a reply marshal buffer if no except occurred
	 *
	 * Returns an OutputStream suitable for marshalling the exception ID
	 * and the user exception body
	 */
	OutputStream createExceptionReply();
}


