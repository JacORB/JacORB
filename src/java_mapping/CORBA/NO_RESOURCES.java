package org.omg.CORBA;
public final class NO_RESOURCES
	extends org.omg.CORBA.SystemException
{
	public NO_RESOURCES()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public NO_RESOURCES(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public NO_RESOURCES(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public NO_RESOURCES(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


