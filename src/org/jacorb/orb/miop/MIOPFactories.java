package org.jacorb.orb.miop;

import org.jacorb.config.*;
import org.omg.CORBA.BAD_PARAM;
import org.omg.ETF.Connection;
import org.omg.ETF.Listener;
import org.omg.ETF.Profile;
import org.omg.ETF._FactoriesLocalBase;
import org.omg.IOP.TAG_UIPMC;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedComponentSeqHolder;
import org.omg.IOP.TaggedProfileHolder;
import org.omg.RTCORBA.ProtocolProperties;


/**
 * Factories for MIOP ETF plugin. It is the entry point of the plugin.
 *
 * @author Alysson Neves Bessani
 * @version 1.0
 */
public class MIOPFactories extends _FactoriesLocalBase implements Configurable
{
   // the MIOP group listener for this ORB
   private MIOPListener  listener = null;

   private Configuration configuration = null;


   /**
    * Configure this object.
    *
    * @param configuration the object configuration
    * @throws ConfigurationException if any problem with the configuration is
    *         detected
    */
   public void configure (Configuration configuration) throws ConfigurationException
   {

      this.configuration = configuration;
   }


   /**
    * Creates a new client connection.
    *
    * @param protocolProperties unused in JacORB
    * @return a client connection
    */
   public Connection create_connection (ProtocolProperties protocolProperties)
   {
      ClientMIOPConnection clientConnection = new ClientMIOPConnection ();

      try
      {
         clientConnection.configure (configuration);
      }
      catch (ConfigurationException ce)
      {
         throw new org.omg.CORBA.INTERNAL ("ConfigurationException: " + ce.getMessage ());
      }

      return clientConnection;
   }


   /**
    * Creates a listener or get the existing one.
    *
    * @param protocolProperties unused in JacORB
    * @param stackSize unused in JacORB
    * @param basePriority unused in JacORB
    * @return the MIOP group listener.
    */
   public Listener create_listener (ProtocolProperties protocolProperties, int stackSize,
            short basePriority)
   {
      if (listener == null)
      {
         listener = new MIOPListener ();

         try
         {
            listener.configure (configuration);
         }
         catch (ConfigurationException ce)
         {
            throw new org.omg.CORBA.INTERNAL ("ConfigurationException: " + ce.getMessage ());
         }
      }

      return listener;
   }


   /**
    * Demarshal a requested profile.
    *
    * @param taggedProfile the holder of the marshaled profile.
    * @param taggedComponentSeq unused (can be null)
    *
    * @return the demarshaled miop profile.
    */
   public Profile demarshal_profile (TaggedProfileHolder taggedProfile,
            TaggedComponentSeqHolder taggedComponentSeq)
   {
      if (taggedProfile.value.tag != TAG_UIPMC.value)
      {
         throw new BAD_PARAM ("wrong profile for MIOP transport: tag = " + taggedProfile.value.tag);
      }
      else
      {
         MIOPProfile profile = new MIOPProfile (taggedProfile.value.profile_data);

         try
         {
            profile.configure (configuration);
         }
         catch (ConfigurationException ce)
         {
            throw new org.omg.CORBA.INTERNAL ("ConfigurationException: " + ce.getMessage ());
         }

         taggedComponentSeq.value = new TaggedComponent[0];
         return profile;
      }

   }


   /**
    * Returns the UMIOP profile tag number.
    *
    * @return TAG_UIPMC.value
    */
   public int profile_tag ()
   {
      return TAG_UIPMC.value;
   }


   /**
    * Decodes a passed corbaloc(without corbaloc: part)
    *
    * @param corbaloc the string to be decoded
    * @return the MIOP profile
    */
   public Profile decode_corbaloc (String corbaloc)
   {
      if (corbaloc.toLowerCase ().startsWith ("miop"))
      {
         MIOPProfile profile = new MIOPProfile (corbaloc);

         try
         {
            profile.configure (configuration);
         }
         catch (ConfigurationException ce)
         {
            throw new org.omg.CORBA.INTERNAL ("ConfigurationException: " + ce.getMessage ());
         }

         return profile;
      }
      else
      {
         return null;
      }
   }
}
