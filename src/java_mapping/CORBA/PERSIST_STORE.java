package org.omg.CORBA;
public final class PERSIST_STORE
	extends org.omg.CORBA.SystemException
{
	public PERSIST_STORE()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public PERSIST_STORE(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public PERSIST_STORE(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public PERSIST_STORE(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


