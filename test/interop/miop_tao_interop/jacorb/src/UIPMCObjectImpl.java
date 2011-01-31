package miop_tao_interop;

import java.util.Arrays;
import java.util.HashMap;

public class UIPMCObjectImpl extends UIPMC_ObjectPOA
{
    private int payload;
    private int clients;
    private Integer calls;
    private HashMap received;

    public UIPMCObjectImpl (int payload, int clients, int calls)
    {
        this.payload = payload;
        this.clients = clients;
        this.calls = calls;
        this.received = new HashMap();
    }

    public void final_check()
    {
        if (this.received.size() == 0)
        {
            System.err.println("ERROR: expected " + this.clients
                    + " clients but only " + this.received.size()
                    + " encountered");
            return;
        }

        for (int i = 0; i < this.clients; ++i)
        {
            Integer count = (Integer) this.received.get((byte) ClientIDs.value.charAt(i));
            if (count == null) count = 0;

            if (count != this.calls)
            // This perfectly ok for MIOP to lose messages.
            // So, this is not an error.
                System.out.println("DEBUG: expected " + this.calls
                        + " messages from '" + ClientIDs.value.charAt(i)
                        + "' client but only " + count + " encountered");
        }
    }

    public void process(byte[] payload)
    {
        if (this.payload != payload.length)
        {
            System.err.println("ERROR: expected " + this.payload
                    + " but received " + payload.length + " sequence length");
            return;
        }

        byte c = payload[0];
        Integer count = (Integer) this.received.get(c);
        if (count == null) count = 0;

        byte[] seq = new byte[this.payload];
        Arrays.fill(seq, c);
        if (!Arrays.equals(seq, payload))
        {
            System.err.println("ERROR: received malformed message from client '"
                    + c + "'");
            return;
        }

        if (!ClientIDs.value.contains(String.valueOf((char) c)))
        {
            System.err.println("ERROR: client id '" + c
                    + "' doesn't match any known value");
            return;
        }

        ++count;
        this.received.put(c, count);
    }
}
