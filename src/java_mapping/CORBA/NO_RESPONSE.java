package org.omg.CORBA;
public final class NO_RESPONSE
	extends org.omg.CORBA.SystemException
{
	public NO_RESPONSE()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public NO_RESPONSE(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public NO_RESPONSE(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public NO_RESPONSE(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


