package org.omg.CORBA.ORBPackage;

/**
 * Generated from IDL interface "InvalidName".
 * @author JacORB IDL compiler
 * @version 2.1.3.2, 3-Oct-2005
 *
 * IDL sourced from 03-01-09 Updated CORBA 3.1
 * interface ORB
 * {
 *    exception InvalidName{};
 * };
 */
public final class InvalidName
   extends org.omg.CORBA.UserException
{
   public InvalidName()
   {
      super(org.omg.CORBA.ORBPackage.InvalidNameHelper.id());
   }

   public InvalidName(String value)
   {
      super(value);
   }
}
