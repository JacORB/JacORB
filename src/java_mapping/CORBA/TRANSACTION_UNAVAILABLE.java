package org.omg.CORBA;
public final class TRANSACTION_UNAVAILABLE
	extends org.omg.CORBA.SystemException
{
	public TRANSACTION_UNAVAILABLE()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public TRANSACTION_UNAVAILABLE(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public TRANSACTION_UNAVAILABLE(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public TRANSACTION_UNAVAILABLE(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


