package org.jacorb.orb;

public class Reference
    extends org.omg.CORBA.portable.ObjectImpl
{
    private String[] ids = {"","IDL:omg.org/CORBA/Object:1.0"};
    public String[] _ids()
    {
	return ids;
    }

    public Reference(String typeId)
    {
	ids[0] = typeId;
    }

}


