package org.omg.CORBA;
public final class BAD_CONTEXT
	extends org.omg.CORBA.SystemException
{
	public BAD_CONTEXT()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public BAD_CONTEXT(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public BAD_CONTEXT(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public BAD_CONTEXT(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


