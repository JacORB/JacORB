package org.omg.CORBA;
public final class DATA_CONVERSION
	extends org.omg.CORBA.SystemException
{
	public DATA_CONVERSION()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public DATA_CONVERSION(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
	}

	public DATA_CONVERSION(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public DATA_CONVERSION(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


