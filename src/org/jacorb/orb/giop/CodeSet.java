package org.jacorb.orb.connection;


public class CodeSet
{
    public static final int ISO8859_1=0x00010001;   /* standard ASCII */
    public static final int UCS4 = 0x00010106;      /* 4 bytes for every character                */
    public static final int UCS2 = 0x00010102;      /*                */
    public static final int UTF16= 0x00010109;      /* extended UCS2, 2 or 4 bytes for every char */
    public static final int UTF8 = 0x05010001;      /* 1-6 bytes for every character              */
    public static final int UTF7 = 0xffff0007;      /* Internet conform ASCII7 representation     */

    public static String csName(int cs)
    {
        switch(cs)
        {
        case ISO8859_1: return "ISO8859_1";
        case UTF16: return  "UTF16";
        case UTF8: return  "UTF8";
        case UCS4: return  "UCS4";
        case UCS2: return  "UCS2";
        case 0: return  "NOTCS";
        }
        return "Unknown TCS: " + Integer.toHexString(cs);
    }

    public static int getTCSDefault()
    {
        return ISO8859_1;
    }

    public static int getTCSWDefault()
    {
        return UTF16;
    }

    public static int getConversionDefault()
    {
        return UTF8;
    }
}


