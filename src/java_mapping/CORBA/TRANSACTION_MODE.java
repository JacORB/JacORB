package org.omg.CORBA;
public final class TRANSACTION_MODE
	extends org.omg.CORBA.SystemException
{
	public TRANSACTION_MODE()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public TRANSACTION_MODE(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public TRANSACTION_MODE(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public TRANSACTION_MODE(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


