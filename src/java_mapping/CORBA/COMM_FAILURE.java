package org.omg.CORBA;
public final class COMM_FAILURE
	extends org.omg.CORBA.SystemException
{
	public COMM_FAILURE()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public COMM_FAILURE(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public COMM_FAILURE(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public COMM_FAILURE(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


