package org.jacorb.orb.miop;

import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.giop.ClientConnection;
import org.omg.MIOP.UIPMC_ProfileBody;
import org.omg.MIOP.UIPMC_ProfileBodyHelper;

/**
 * Several utility constants and static methods.
 *
 * @author Alysson Neves Bessani
 * @version 1.0
 */
public final class MulticastUtil {

    /* default values of several variables */

    /**
     * Timeout used in socket.receivePacket.
     */
    static final int SO_TIMEOUT = 100;
    /**
     * TTL of multicast packets.
     */
    static final int TIME_TO_LIVE = 5;
    /**
     * Maximum number of incomplete messages tolerated.
     *
     * @see org.jacorb.orb.miop.ServerMIOPConnection
     */
    static final int INCOMPLETE_MESSAGES_THRESHOULD = 5;
    /**
     * Timeout for a packet collection be completed.
     *
     * @see org.jacorb.orb.miop.FragmentedMessage
     */
    static final int MESSAGE_COMPLETION_TIMEOUT = 500;

    /**
     * Packet max size (data + header). Default Ethernet MTU.
     *
     * Note that while 03-01-11 states 1518 is a typical ethernet frame
     * consisting of 6+6 bytes mac address, 2 bytes length, 4 bytes CRC and 1500 bytes
     * payload, 1500 bytes is the typical amount supported by most interfaces.
     */
    static final int PACKET_MAX_SIZE = 1500;
    /**
     * This is the actual amount of data we can successfully pass inside the UMIOP
     * packet. It is calculated by:
     * 1500 -
     * 20 bytes IP Hdr less 8 bytes UDP Hdr = 28.
     * 32 bytes UMIOP hdr = 32
     * By default 1500 - (28 + 32) = 1412
     */
    static final short PACKET_DATA_MAX_SIZE = 60;


    /* UMIOP constants */

    /**
     * Our packet collection (message) id size.
     */
    public static final int ID_SIZE = 12;
    /**
     * Packet boundary length. Separates header from data.
     */
    public static final int BOUNDARY = 8;

    /**
     * Magic field of MIOP packet.
     */
    public static final char[] MAGIC = {'M','I','O','P'};
    /**
     * MIOP packet header version (1.0).
     */
    public static final byte HDR_VERSION = 0x10;
    /**
     * Big endian flag. Used in Java language.
     */
    public static final byte BIG_ENDIAN = 0x00;
    /**
     * The stop flag. Must be included in the last packet of each collection.
     */
    public static final byte STOP_FLAG = 0x02;


   /**
    * Match miop magic markers
    *
    * @param buf the buf
    *
    * @return true, if successful
    */
   public static final boolean matchMIOPMagic(char[] buf)
   {
       // The values are hard-coded to support non-ASCII platforms.
       return   (buf[0] == 0x4d   // 'M'
              && buf[1] == 0x49   // 'I'
              && buf[2] == 0x4f   // 'O'
              && buf[3] == 0x50); // 'P'
   }


   /**
    * Encapsulates the given uipmc profile and returns the bytes.
    *
    * @param orb the orb
    * @param upb the upb
    *
    * @return the encapsulated uipmc profile
    */
   public static byte[] getEncapsulatedUIPMCProfile (org.jacorb.orb.ORB orb, UIPMC_ProfileBody upb)
   {
      return getEncapsulatedUIPMCProfile (orb, null, upb);
   }

   /**
    * Encapsulates the uipmc profile in the given ClientConnection.
    *
    * @param orb the orb
    * @param connection the connection
    *
    * @return the encapsulated uipmc profile
    */
   public static byte[] getEncapsulatedUIPMCProfile (org.jacorb.orb.ORB orb, ClientConnection connection)
   {
      return getEncapsulatedUIPMCProfile (orb, connection, null);
   }

   private static byte[] getEncapsulatedUIPMCProfile (org.jacorb.orb.ORB orb, ClientConnection connection, UIPMC_ProfileBody upb)
   {
      CDROutputStream out = new CDROutputStream (orb);
      out.beginEncapsulatedArray ();
      if (connection != null)
      {
         UIPMC_ProfileBodyHelper.write (out, ((MIOPProfile)connection.getRegisteredProfile ()).getUIPMCProfile ());
      }
      else
      {
         UIPMC_ProfileBodyHelper.write (out, upb);
      }
      byte []result = out.getBufferCopy ();
      out.close();

      return result;
   }
}
