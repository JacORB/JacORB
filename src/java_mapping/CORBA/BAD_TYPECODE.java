package org.omg.CORBA;
public final class BAD_TYPECODE
	extends org.omg.CORBA.SystemException
{
	public BAD_TYPECODE()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public BAD_TYPECODE(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public BAD_TYPECODE(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public BAD_TYPECODE(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


