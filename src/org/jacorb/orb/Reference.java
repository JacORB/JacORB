package org.jacorb.orb;

public class Reference
    extends javax.rmi.CORBA.Stub // which extends org.omg.CORBA_2_3.portable.ObjectImpl
    implements java.rmi.Remote
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








