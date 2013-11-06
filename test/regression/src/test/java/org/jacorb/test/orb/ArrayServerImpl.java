package org.jacorb.test.orb;

import org.jacorb.test.ArrayServerPOA;
import org.jacorb.test.any_sequenceHolder;
import org.jacorb.test.boolean_sequenceHolder;
import org.jacorb.test.char_sequenceHolder;
import org.jacorb.test.color_enum;
import org.jacorb.test.long_sequenceHolder;
import org.omg.CORBA.Any;

/**
 * @author Andre Spiegel spiegel@gnu.org
 */
public class ArrayServerImpl extends ArrayServerPOA
{
    public void ping()
    {
        return;
    }

    public boolean reduce_boolean_sequence(boolean[] seq)
    {
    	boolean parity = false;
    	for (int i=0; i<seq.length; i++)
    	{
    		if (seq[i]) parity = !parity;
    	}
    	return parity;
    }
    
    public int sum_octet_sequence(byte[] seq)
    {
        int sum = 0;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
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

    public int sum_ushort_sequence(short[] seq)
    {
        int sum = 0;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
    }

    public int sum_long_sequence(int[] seq)
    {
        int sum = 0;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
    }
    
    public int sum_ulong_sequence(int[] seq)
    {
        int sum = 0;
        for (int i=0; i<seq.length; i++)
        {
            sum += seq[i];
        }
        return sum;
    }

    public int sum_longlong_sequence(long[] seq)
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
    
    public int reduce_enum_sequence(color_enum[] seq)
    {
    	return seq.length;
    }
    
    public int reduce_char_sequence(char[] seq)
    {
    	return seq.length;
    }
    
    public void return_illegal_char_sequence(char_sequenceHolder seq)
    {
        seq.value = new char[] { 'a', CharTest.EURO_SIGN, 'b' };
    }
    
    public int reduce_wchar_sequence(char[] seq)
    {
    	return seq.length;
    }
    
    public int reduce_any_sequence(Any[] seq)
    {
    	return seq.length;
    }
    
    public void bounce_boolean_sequence (boolean[] seq_in,
                                         boolean_sequenceHolder seq_out)
    {
        seq_out.value = new boolean[seq_in.length];
        System.arraycopy(seq_in, 0, seq_out.value, 0, seq_in.length);
    }

    public void bounce_long_sequence (int[] seq_in,
                                      long_sequenceHolder seq_out)
    {
        seq_out.value = new int[seq_in.length];
        System.arraycopy(seq_in, 0, seq_out.value, 0, seq_in.length);
    }

    public void bounce_any_sequence (Any[] seq_in,
                                     any_sequenceHolder seq_out)
    {
        seq_out.value = new Any[seq_in.length];
        System.arraycopy(seq_in, 0, seq_out.value, 0, seq_in.length);
    }

    
}
