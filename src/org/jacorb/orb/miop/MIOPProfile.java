package org.jacorb.orb.miop;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.CDROutputStream;
import org.jacorb.orb.ORB;
import org.jacorb.orb.ParsedIOR;
import org.jacorb.orb.TaggedComponentList;
import org.jacorb.orb.etf.ProfileBase;
import org.jacorb.orb.iiop.IIOPProfile;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.ETF.Profile;
import org.omg.GIOP.Version;
import org.omg.IOP.IOR;
import org.omg.IOP.TAG_GROUP;
import org.omg.IOP.TAG_INTERNET_IOP;
import org.omg.IOP.TAG_UIPMC;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedComponentSeqHolder;
import org.omg.IOP.TaggedProfile;
import org.omg.IOP.TaggedProfileHolder;
import org.omg.MIOP.UIPMC_ProfileBody;
import org.omg.MIOP.UIPMC_ProfileBodyHelper;
import org.omg.PortableGroup.TagGroupTaggedComponent;
import org.omg.PortableGroup.TagGroupTaggedComponentHelper;


/**
 *
 * @author Alysson Neves Bessani
 * @author Nick Cross
 * @version 1.0
 * @see MIOPFactories
 */
public class MIOPProfile extends ProfileBase
{
   private InetAddress             inetAddress      = null;
   private byte[]                  data             = null;
   private UIPMC_ProfileBody       uipmc            = null;
   private TagGroupTaggedComponent tagGroup         = null;
   private IIOPProfile             groupIIOPProfile = null;


   /**
    * Creates a MIOP profile from a marshaled profile.
    *
    * @param data the byte array
    */
   public MIOPProfile (byte[] data)
   {
      this.data = data;

      CDRInputStream in = new CDRInputStream (data);
      in.openEncapsulatedArray ();
      uipmc = UIPMC_ProfileBodyHelper.read (in);
      in.close ();

      version = uipmc.miop_version;
   }


   /**
    * Creates a MIOP profile based on a complete information set.
    *
    * @param address group address.
    * @param port group port.
    * @param domainId group domain id.
    * @param groupId group id.
    * @param groupVersion group version.
    * @param groupRefVersion group reference version.
    * @param iiop the IIOP group profile..
    */
   public MIOPProfile (String address, short port, String domainId, long groupId,
            Version groupVersion, int groupRefVersion, IIOPProfile iiop)
   {

      this.groupIIOPProfile = iiop;
      this.tagGroup = new TagGroupTaggedComponent(groupVersion, domainId, groupId, groupRefVersion);

      TaggedComponentList list = new TaggedComponentList ();
      list.addComponent (TAG_GROUP.value, tagGroup, TagGroupTaggedComponentHelper.class);

      if (iiop != null)
      {
         list.addAll (iiop.getComponents ());
      }

      this.uipmc = new UIPMC_ProfileBody (new Version ((byte)1, (byte)0), address, port,
               list.asArray ());

      try
      {
         inetAddress = InetAddress.getByName (uipmc.the_address);
      }
      catch (UnknownHostException uke)
      {
         throw new RuntimeException ("Unable to create profile to unknown group address: " +
                  uipmc.the_address);
      }
   }


   /**
    * Creates a MIOP profile from a corbaloc URL.
    *
    * @param corbaloc group corbaloc URL
    */
   public MIOPProfile (String corbaloc)
   {
      this.corbalocStr = corbaloc;
   }


   /**
    * Configure this object. Must be called after the creation for full profile
    * configuration.
    *
    * @param configuration the object configuration
    * @throws ConfigurationException if any problem with the configuration is
    *         detected
    */
   public void configure (Configuration config) throws ConfigurationException
   {
      super.configure(configuration);

      logger = configuration.getLogger ("jacorb.miop");

      ORB orb = ((org.jacorb.config.Configuration)config).getORB ();

      if (corbalocStr != null)
      {
         if (logger.isDebugEnabled ())
         {
            logger.debug ("MIOPProfile parsing corbaloc.");
         }

         // I create the profile from the corbaloc
         if (!corbalocStr.startsWith ("miop:"))
         {
            throw new IllegalArgumentException ("URL must start with \'miop:\'");
         }

         int sub = corbalocStr.indexOf (";");
         if (sub == -1)
         {
            parseMIOPCorbaloc (corbalocStr.substring (0));
         }
         else
         {
            parseMIOPCorbaloc (corbalocStr.substring (0, sub));
         }


         components = new TaggedComponentList ();

         CDROutputStream out = new CDROutputStream ();
         out.beginEncapsulatedArray ();
         TagGroupTaggedComponentHelper.write (out, tagGroup);
         components.addComponent (TAG_GROUP.value, out.getBufferCopy ());
         out.close ();

         if (sub != -1)
         {
            groupIIOPProfile = (IIOPProfile)new ParsedIOR (orb, "corbaloc:" +
                     corbalocStr.substring (sub + 1)).getEffectiveProfile ();

            objectKey = groupIIOPProfile.get_object_key ();
            TaggedProfile taggedProfile = groupIIOPProfile.asTaggedProfile ();
            components.addComponent (taggedProfile.tag, taggedProfile.profile_data);
         }

         uipmc.components = components.asArray ();
      }

      if (tagGroup == null)
      {
         if (logger.isDebugEnabled ())
         {
            logger.debug ("MIOPProfile inspecting uipmc components.");
         }

         components = new TaggedComponentList ();
         for (int i = 0; i < uipmc.components.length; i++)
         {
            TaggedComponent component = uipmc.components[i];
            components.addComponent (component);
            switch (component.tag)
            {
               case TAG_GROUP.value:
               {
                  if (logger.isDebugEnabled ())
                  {
                     logger.debug ("MIOPProfile inspecting tagGroup.");
                  }

                  CDRInputStream in2 = new CDRInputStream (orb, component.component_data);
                  in2.openEncapsulatedArray();
                  tagGroup = TagGroupTaggedComponentHelper.read (in2);
                  in2.close();
                  break;
               }
               case TAG_INTERNET_IOP.value:
               {
                  if (logger.isDebugEnabled ())
                  {
                     logger.debug ("MIOPProfile inspecting group iiop profile.");
                  }
                  IOR ior = new IOR ("IDL:omg.org/CORBA/Object:1.0",
                           new TaggedProfile[] { new TaggedProfile (component.tag,
                                    component.component_data) });

                  groupIIOPProfile = (IIOPProfile)new ParsedIOR (orb, ior).getEffectiveProfile ();
               }
            }
         }
      }

      try
      {
         inetAddress = InetAddress.getByName (uipmc.the_address);
      }
      catch (UnknownHostException uke)
      {
         throw new RuntimeException ("Unable to create profile to unknow group address: " +
                  uipmc.the_address);
      }
   }


   /**
    * The group object key, or the tagGroup as a byte array.
    *
    * @return tagGroup as a byte array.
    */
   public byte[] get_object_key ()
   {
      if (objectKey == null)
      {
         CDROutputStream out = new CDROutputStream ();
         out.beginEncapsulatedArray ();
         TagGroupTaggedComponentHelper.write (out, tagGroup);
         objectKey = out.getBufferCopy ();
         out.close();
      }

      return objectKey;
   }


   /**
    * Hash code of this profile.
    *
    * @return the hashcode of this object.
    */
   public int hash ()
   {
      return hashCode ();
   }


   /**
    * Test if the passed profile is equal this.
    *
    * @param profile other profile.
    * @return true if it's equal, false otherwise.
    */
   public boolean is_match (Profile profile)
   {
      return equals (profile);
   }


   /**
    * Encode this profile as a TaggedProfile (marshaled).
    *
    * @param taggedProfile a tagged profile holder.
    * @param taggedComponentSeq unused.
    */
   public void marshal (TaggedProfileHolder taggedProfile,
            TaggedComponentSeqHolder taggedComponentSeq)
   {
      if (data == null)
      {
         CDROutputStream out = new CDROutputStream ();
         out.beginEncapsulatedArray ();
         UIPMC_ProfileBodyHelper.write (out, uipmc);
         data = out.getBufferCopy ();
         out.close ();
      }

      taggedProfile.value = new TaggedProfile (TAG_UIPMC.value, data);
   }


   /**
    * Set this object key. Does nothing because it is a group profile. Objectkey
    * equals serialized tagGroup
    *
    * @param objectKey unused.
    */
   public void set_object_key (byte[] objectKey)
   {
   }


   /**
    * Return the profile tag.
    *
    * @return TAG_UIPMC.value.
    */
   public int tag ()
   {
      return TAG_UIPMC.value;
   }


   /**
    * This profile MIOP version.
    *
    * @return miop version.
    */
   public Version version ()
   {
      return uipmc.miop_version;
   }


   /*
    * Other public methods
    */

   /**
    * Returns the group InetAddress.
    *
    * @return group InetAddress.
    */
   public final InetAddress getGroupInetAddress ()
   {
      return inetAddress;
   }


   /**
    * Returns the UIPMC profile.
    *
    * @return the UIPMC_ProfileBody object.
    */
   public final UIPMC_ProfileBody getUIPMCProfile ()
   {
      return uipmc;
   }


   /**
    * Returns tagGroup, the group logical information struct.
    *
    * @return the GroupInfo object.
    */
   public final TagGroupTaggedComponent getTagGroup ()
   {
      return tagGroup;
   }


   /**
    * the IIOP group profile (for two way requests).
    *
    * @return IIOP profile
    */
   public final IIOPProfile getGroupIIOPProfile ()
   {
      return groupIIOPProfile;
   }


   /**
    * The string address of this profile.
    *
    * @return <group address>:<group port>
    */
   public final String toString ()
   {
      return uipmc.the_address + ":" + uipmc.the_port;
   }


   /**
    * Parse a miop corbaloc url. Starting with 'miop:'.
    *
    * @param corbaloc
    */
   private void parseMIOPCorbaloc (String corbaloc)
   {
      // removes the "miop:" header
      corbaloc = corbaloc.substring (corbaloc.indexOf (':') + 1);

      // parse version (optional)
      int sep = corbaloc.indexOf ('@');
      Version version = parseVersion (sep == -1 ? "" : corbaloc.substring (0, sep));
      corbaloc = corbaloc.substring (sep + 1);

      // parse tag group
      sep = corbaloc.indexOf ('/');
      parseTagGroup (corbaloc.substring (0, sep));
      corbaloc = corbaloc.substring (sep + 1);

      // parse group transport address
      parseGroupAddress (corbaloc);

      uipmc.miop_version = version;
   }


   /**
    * Parses a version string into a org.omg.GIOP.Version object.
    *
    * @param ver_str.
    * @return a version object.
    */
   private Version parseVersion (String ver_str)
   {
      int major = 1;
      int minor = 0;
      int sep = ver_str.indexOf ('.');
      if (sep != -1)
      {
         try
         {
            major = Integer.parseInt (ver_str.substring (0, sep));
            minor = Integer.parseInt (ver_str.substring (sep + 1));
         }
         catch (NumberFormatException nfe)
         {
            throw new IllegalArgumentException ("Invalid version :" + ver_str);
         }
      }
      return new Version ((byte)major, (byte)minor);
   }


   /**
    * Parses a Tag Group from a String. Example: "1.0-MyDomain-1" Where: <group
    * component version>-<group_domain_id>-<object_group_id>[-<object group
    * reference version>]
    *
    * @param s
    */
   public void parseTagGroup (String s)
   {
      tagGroup = new TagGroupTaggedComponent();
      if (s.indexOf ('-') != -1)
      {
         try
         {
            StringTokenizer st = new StringTokenizer (s, "-");
            tagGroup.group_version = parseVersion (st.nextToken ());
            tagGroup.group_domain_id = st.nextToken ();
            tagGroup.object_group_id = Long.parseLong (st.nextToken ());
            if (st.hasMoreTokens ())
            {
               tagGroup.object_group_ref_version = Integer.parseInt (st.nextToken ());
            }
         }
         catch (NumberFormatException nfe)
         {
            throw new IllegalArgumentException ("Illegal group information format: " + s);
         }
      }
      else
      {
         throw new IllegalArgumentException ("Illegal group information format: " + s);
      }
   }


   /**
    * Parse the group address. Example: "225.6.7.8:12345" Where: <group IP
    * address>:<group port>
    *
    * @param s
    */
   private void parseGroupAddress (String s)
   {
      uipmc = new UIPMC_ProfileBody ();

      int sep = s.indexOf (':');
      if (sep != -1)
      {
         try
         {
            uipmc.the_port = (short)Integer.parseInt (s.substring (sep + 1));
            uipmc.the_address = s.substring (0, sep);
         }
         catch (NumberFormatException ill)
         {
            throw new IllegalArgumentException (
                     "Illegal port number in MIOP object address format: " + s);
         }
      }
   }


   /*
    * (non-Javadoc)
    *
    * @seeorg.jacorb.orb.etf.ProfileBase#readAddressProfile(org.jacorb.orb.
    * CDRInputStream)
    */
   public void readAddressProfile (CDRInputStream stream)
   {
      throw new NO_IMPLEMENT ("Not yet implemented");
   }


   /*
    * (non-Javadoc)
    *
    * @seeorg.jacorb.orb.etf.ProfileBase#writeAddressProfile(org.jacorb.orb.
    * CDROutputStream)
    */
   public void writeAddressProfile (CDROutputStream stream)
   {
      throw new NO_IMPLEMENT ("Not yet implemented");
   }
}
