package org.jacorb.test.bugs.bugjac493;

import org.jacorb.test.EmptyException;
import org.jacorb.test.TimingServerPOA;

public class TimingServerImpl extends TimingServerPOA
{
    public int operation(int id, int delay)
    {
        sleep(delay);
        return id;
    }

    public char ex_op(char ch, int delay) throws EmptyException
    {
        sleep(delay);
        if (ch == 'e')
        {
            throw new EmptyException();
        }
        else if (ch == '$')
        {
            return '\u20AC';  // euro sign, will raise DATA_CONVERSION
        }
        else
        {
            return ch;
        }
    }

    public long server_time(int delay)
    {
        long time = System.currentTimeMillis();
        sleep(delay);
        return time;
    }

    private void sleep(int delay)
    {
        try
        {
            Thread.sleep(delay);
        }
        catch (InterruptedException ex)
        {
            System.out.println ("wait interrupted");
        }
    }
}
