package org.jacorb.orb.miop;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.etf.FactoriesBase;
import org.jacorb.orb.etf.ProtocolAddressBase;
import org.jacorb.orb.iiop.IIOPAddress;
import org.omg.CORBA.BAD_PARAM;
import org.omg.ETF.Connection;
import org.omg.ETF.Listener;
import org.omg.ETF.Profile;
import org.omg.IOP.TAG_UIPMC;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedComponentSeqHolder;
import org.omg.IOP.TaggedProfileHolder;


/**
 * Factories for MIOP ETF plugin. It is the entry point of the plugin.
 *
 * @author Alysson Neves Bessani
 * @version 1.0
 */
public class MIOPFactories extends FactoriesBase implements Configurable
{
   // the MIOP group listener for this ORB
   private MIOPListener listener = null;


   /**
    * Return the correct type of connection
    */
   protected Connection create_connection_internal ()
   {
      return new ClientMIOPConnection();
   }

   /**
    * Return the correct type of address
    */
   protected ProtocolAddressBase create_address_internal ()
   {
      return new IIOPAddress();
   }

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
    * Return the correct type of listener
    */
   public Listener create_listener_internal ()
   {
      if (listener == null)
      {
         listener = new MIOPListener();
         configureResult (listener);
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

      MIOPProfile profile = new MIOPProfile (taggedProfile.value.profile_data);

      configureResult (profile);

      taggedComponentSeq.value = new TaggedComponent[0];

      return profile;
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
