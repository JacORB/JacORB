package org.omg.CORBA;
public final class OBJECT_NOT_EXIST
	extends org.omg.CORBA.SystemException
{
	public OBJECT_NOT_EXIST()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public OBJECT_NOT_EXIST(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public OBJECT_NOT_EXIST(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public OBJECT_NOT_EXIST(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


