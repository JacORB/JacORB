package demo.ami;

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;

public class AMI_AsyncServerHandlerImpl 
  extends AMI_AsyncServerHandlerPOA
{
    public void operation(int ami_return_val)
    {
        System.out.println ("** async reply: " + ami_return_val);
    }

    public void operation_excep(org.omg.Messaging.ExceptionHolder excep_holder)
    {
        System.out.println ("** async exception");
    }

    public void op2(int ami_return_val)
    {
        System.out.println ("** op2 async reply: " + ami_return_val);
    }

    public void op2_excep(org.omg.Messaging.ExceptionHolder excep_holder)
    {
        try
        {
            excep_holder.raise_exception();
        }
        catch (Exception e)
        {
            System.out.println ("** op2 async exception: " + e);
        }
    }

}
