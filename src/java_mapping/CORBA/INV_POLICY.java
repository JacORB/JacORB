package org.omg.CORBA;
public final class INV_POLICY
	extends org.omg.CORBA.SystemException
{
	public INV_POLICY()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public INV_POLICY(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public INV_POLICY(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public INV_POLICY(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


