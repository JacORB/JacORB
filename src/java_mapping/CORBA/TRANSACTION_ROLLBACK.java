package org.omg.CORBA;
public final class TRANSACTION_ROLLBACK
	extends org.omg.CORBA.SystemException
{
	public TRANSACTION_ROLLBACK()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public TRANSACTION_ROLLBACK(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public TRANSACTION_ROLLBACK(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public TRANSACTION_ROLLBACK(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


