package org.omg.CORBA;
public final class BAD_OPERATION
	extends org.omg.CORBA.SystemException
{
	public BAD_OPERATION()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public BAD_OPERATION(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public BAD_OPERATION(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public BAD_OPERATION(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


