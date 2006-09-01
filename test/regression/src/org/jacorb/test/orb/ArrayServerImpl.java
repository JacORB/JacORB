package org.jacorb.test.orb;

import org.jacorb.test.*;

/**
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id$
 */
public class ArrayServerImpl extends ArrayServerPOA
{
    public void ping()
    {
        return;
    }
    
    public int sum_short_sequence(short[] seq)
    {
        int sum = 0;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
    }
    
    public int sum_ulonglong_sequence(long[] seq)
    {
        int sum = 0;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
    }
    
    public int sum_ushort_sequence(short[] seq)
    {
        int sum = 0;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
    }

    public float sum_float_sequence(float[] seq)
    {
        float sum = 0.0F;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
    }

    public double sum_double_sequence(double[] seq)
    {
        double sum = 0;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
    }
}
