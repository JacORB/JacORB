package org.jacorb.test.orb.value;

import java.io.Serializable;

import org.omg.CORBA.portable.ValueFactory;
import org.omg.CORBA_2_3.portable.InputStream;

public class RecordImpl extends Record implements ValueFactory
{
    public RecordImpl()
    {
        super();   
    }
    
    public RecordImpl(int i, String text)
    {
        this.id = i;
        this.text = text;
    }

    public Serializable read_value(InputStream is) 
    {
        RecordImpl result = new RecordImpl(0, null);
        return is.read_value(result);
    }
    
}
