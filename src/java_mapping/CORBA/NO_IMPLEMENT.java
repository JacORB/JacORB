package org.omg.CORBA;
public final class NO_IMPLEMENT
	extends org.omg.CORBA.SystemException
{
	public NO_IMPLEMENT()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public NO_IMPLEMENT(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public NO_IMPLEMENT(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public NO_IMPLEMENT(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


