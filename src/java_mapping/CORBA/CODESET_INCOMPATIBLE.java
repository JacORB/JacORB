package org.omg.CORBA;
public final class CODESET_INCOMPATIBLE
	extends org.omg.CORBA.SystemException
{
	public CODESET_INCOMPATIBLE()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public CODESET_INCOMPATIBLE(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public CODESET_INCOMPATIBLE(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public CODESET_INCOMPATIBLE(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


