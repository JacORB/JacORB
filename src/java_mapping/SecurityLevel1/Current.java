package org.omg.SecurityLevel1;

//  public abstract class Current
//      implements org.omg.CORBA.Current
//  {
//      public abstract org.omg.Security.SecAttribute[] get_attributes(org.omg.Security.AttributeType[] attributes);
//  }

public interface Current
    extends org.omg.CORBA.Current
{
    org.omg.Security.SecAttribute[] get_attributes(org.omg.Security.AttributeType[] attributes);
}


