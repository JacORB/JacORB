package org.jacorb.idl;

import java.io.*;

public class StateMember extends Member 
{
  
    public boolean isPublic = false;

    public StateMember (int num)
    {
        super (num);
    }

    /**
     * Creates a new Member that is similar to this one, 
     * but only for declarator d.
     */
    public Member extractMember (Declarator d)
    {
        StateMember result = new StateMember (new_num());
        result.declarator  = d;
        result.isPublic    = this.isPublic;
        return result;
    }

    public void print (PrintWriter ps)
    {
        if (this.isPublic)
            member_print (ps, "\tpublic ");
        else
            member_print (ps, "\tprotected ");
    }

    public String writeStatement (String outStreamName)
    {
        return type_spec.printWriteStatement (declarator.name(), 
                                              outStreamName);
    }

    public String readStatement (String inStreamName)
    {
        return type_spec.printReadStatement (declarator.name(),
                                             inStreamName);
    }
}
