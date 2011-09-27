package org.jacorb.test.bugs.bugrtj634;

import org.jacorb.test.ComplexTimingServerPOA;
import org.jacorb.test.EmptyException;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.Object;

public class TimingServerImpl extends ComplexTimingServerPOA
{
   private int return_value = 0;


   public TimingServerImpl()
   {
      return_value = 0;
   }

   public TimingServerImpl(int ret_value)
   {
      return_value = ret_value;
   }

   public int operation(int id, int delay)
   {
      return return_value;
   }

   public char ex_op(char ch, int delay) throws EmptyException
   {
      throw new NO_IMPLEMENT();
   }

   public long server_time(int delay)
   {
      throw new NO_IMPLEMENT();
   }

   public void setServerConfig (int fwdPoint, Object fwd)
   {
     SInterceptor.OBJ_2 = fwd;
   }

   public int forwardOperation (int id, int delay)
   {
      throw new NO_IMPLEMENT ("NYI");
   }
}
