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

}
