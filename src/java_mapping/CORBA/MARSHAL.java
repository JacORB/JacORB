package org.omg.CORBA;
public final class MARSHAL
	extends org.omg.CORBA.SystemException
{
	public MARSHAL()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public MARSHAL(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public MARSHAL(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public MARSHAL(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


