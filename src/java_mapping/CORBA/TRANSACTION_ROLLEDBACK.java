package org.omg.CORBA;
public final class TRANSACTION_ROLLEDBACK
	extends org.omg.CORBA.SystemException
{
	public TRANSACTION_ROLLEDBACK()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public TRANSACTION_ROLLEDBACK(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public TRANSACTION_ROLLEDBACK(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public TRANSACTION_ROLLEDBACK(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


