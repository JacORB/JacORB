package org.omg.CORBA;
public final class TRANSACTION_REQUIRED
	extends org.omg.CORBA.SystemException
{
	public TRANSACTION_REQUIRED()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public TRANSACTION_REQUIRED(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public TRANSACTION_REQUIRED(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public TRANSACTION_REQUIRED(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


