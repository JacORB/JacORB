package org.jacorb.test.bugs.bug969;

import static org.junit.Assert.assertTrue;
import java.util.Properties;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.test.harness.ORBTestCase;
import org.junit.Assert;
import org.junit.Test;

public class CodesetByteTest extends ORBTestCase
{
    @Override
    protected void patchORBProperties(Properties props) throws Exception
    {
        props.setProperty("jacorb.codeset", "on");
        props.setProperty("jacorb.native_char_codeset", "UTF8");
    }

    @Test
    public void testCDRWith4Byte() throws Exception
    {
        CDROutputStream cdrOutputStream = new CDROutputStream(orb);

        // Using DESERET SMALL LETTER ER
        // http://www.charbase.com/10449-unicode-deseret-small-letter-er
        String source = "\ud801\udc49";
        byte[] sourceMatch = { (byte) 0xf0, (byte) 0x90, (byte) 0x91, (byte) 0x89 };
        assertTrue(length(source) == sourceMatch.length);
        assertTrue(length(source) == source.getBytes(getORB().getTCSDefault().getName()).length);

        cdrOutputStream.write_string(source);

        byte[] bufferCopy = cdrOutputStream.getBufferCopy();
        byte[] toMatch = new byte[sourceMatch.length];
        System.arraycopy(bufferCopy, 4, toMatch, 0, sourceMatch.length);

        assertTrue("Buffer size should be 9",
                bufferCopy.length == (4 + 1 + length(source)));

        CDRInputStream cdrin = (CDRInputStream) cdrOutputStream.create_input_stream();
        String result = cdrin.read_string();
        assertTrue("Strings should match", source.equals(result));

        Assert.assertArrayEquals(sourceMatch, result.getBytes("utf-8"));
        Assert.assertArrayEquals(sourceMatch, toMatch);

        cdrOutputStream.close();
    }

    /**
     * Return length of bytes in the given string
     *
     * http://stackoverflow.com/questions/8511490/calculating-length-in-utf-8-of
     * -java-string-without-actually-encoding-it
     *
     * @param sequence
     * @return
     */
    public static int length(CharSequence sequence)
    {
        int count = 0;
        for (int i = 0, len = sequence.length(); i < len; i++)
        {
            char ch = sequence.charAt(i);
            if (ch <= 0x7F)
            {
                count++;
            }
            else if (ch <= 0x7FF)
            {
                count += 2;
            }
            else if (Character.isHighSurrogate(ch))
            {
                count += 4;
                ++i;
            }
            else
            {
                count += 3;
            }
        }
        return count;
    }
}
