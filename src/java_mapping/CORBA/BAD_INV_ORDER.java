package org.omg.CORBA;
public final class BAD_INV_ORDER
	extends org.omg.CORBA.SystemException
{
	public BAD_INV_ORDER()
	{
		super( "", 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public BAD_INV_ORDER(String reason)
	{
		super( reason, 0, org.omg.CORBA.CompletionStatus.COMPLETED_NO);
	}

	public BAD_INV_ORDER(int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( "", minor, completed);
	}

	public BAD_INV_ORDER(String reason, int minor, org.omg.CORBA.CompletionStatus completed)
	{
		super( reason, minor, completed);
	}

}


