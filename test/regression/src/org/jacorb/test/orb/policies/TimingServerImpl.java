package org.jacorb.test.orb.policies;

import org.jacorb.Tests.EmptyException;
import org.jacorb.Tests.TimingServerPOA;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;

public class TimingServerImpl extends TimingServerPOA
{
    public int operation(int id, int delay)
    {
        synchronized (this)
        {
            try
            {
                if (delay > 0) this.wait (delay);
            }
            catch (InterruptedException ex)
            {
                System.out.println ("wait interrupted");
            }
        }
        return id;
    }

    public char ex_op(char ch, int delay) throws EmptyException
    {
        synchronized (this)
        {
            try
            {
                if (delay > 0) this.wait (delay);
            }
            catch (InterruptedException ex)
            {
                System.out.println ("wait interrupted");
            }
        }
        if (ch == 'e')
            throw new EmptyException();
        else if (ch == '$')
            return '\u20AC';  // euro sign, will raise DATA_CONVERSION
        else
            return ch;
    }

}
