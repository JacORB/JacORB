package demo.benchmark;

public class octetBenchImpl 
    extends octetBenchPOA
{
    public void opOctetSeqIn(byte[] argin)
    {
        //
    }

    public void opOctetSeqOut(demo.benchmark.OctetSeqHolder argout)
    {
        argout.value = new byte[1];
    }

    public void opOctetSeqInOut(demo.benchmark.OctetSeqHolder arginout)
    {
        arginout.value = arginout.value; // :-)
    }

}


