package org.jacorb.test.orb.policies;

public abstract class TestConfig
{
    static org.omg.CORBA.Object fwd = null;
    static int fwdReqPoint = 0;

    static final int SEND_REQ   = 1;
    static final int RRSC       = 2;
    static final int REC_REQ    = 3;
    static final int SEND_EX    = 4;
    static final int REC_EX     = 5;
    static final int SEND_OTHER = 6;
    static final int REC_OTHER  = 7;

    static final int CALL_AT_SEND_REQ   = 11;
    static final int CALL_AT_RRSC       = 12;
    static final int CALL_AT_REC_REQ    = 13;
    static final int CALL_AT_SEND_EX    = 14;
    static final int CALL_AT_REC_EX     = 15;
    static final int CALL_AT_SEND_OTHER = 16;
    static final int CALL_AT_REC_OTHER  = 17;

    static void setConfig (int fwdPoint,
                           org.omg.CORBA.Object fwdObj)
    {
        fwd = fwdObj;
        fwdReqPoint = fwdPoint;
    }
}
