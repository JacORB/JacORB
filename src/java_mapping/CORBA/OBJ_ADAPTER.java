package org.omg.CORBA;
public final class OBJ_ADAPTER
	extends org.omg.CORBA.SystemException
{
	public OBJ_ADAPTER()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public OBJ_ADAPTER(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public OBJ_ADAPTER(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public OBJ_ADAPTER(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


