package org.omg.CORBA;
public final class IMP_LIMIT
	extends org.omg.CORBA.SystemException
{
	public IMP_LIMIT()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public IMP_LIMIT(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public IMP_LIMIT(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public IMP_LIMIT(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


