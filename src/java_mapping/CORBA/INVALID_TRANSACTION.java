package org.omg.CORBA;
public final class INVALID_TRANSACTION
	extends org.omg.CORBA.SystemException
{
	public INVALID_TRANSACTION()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public INVALID_TRANSACTION(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public INVALID_TRANSACTION(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public INVALID_TRANSACTION(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


