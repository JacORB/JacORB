package org.omg.CORBA.portable;

/**
 * CORBA V2.3 - 1.3 July 1998 (merged version) see o. p. 23-97
 * last modified: 02/03/99 RT
 */
public class ApplicationException extends Exception {

	private String repid;
	private InputStream in;

	public ApplicationException(String repid, InputStream in) {
		super();
		this.repid = repid;
		this.in = in;
	}

	public String getId() {
		return repid;
	}

	public InputStream getInputStream() {
		return in;
	}
}


