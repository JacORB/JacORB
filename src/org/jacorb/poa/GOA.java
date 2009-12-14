package org.jacorb.poa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.jacorb.orb.Delegate;
import org.jacorb.orb.ORB;
import org.jacorb.orb.dsi.ServerRequest;
import org.jacorb.orb.giop.RequestInputStream;
import org.jacorb.orb.miop.MIOPListener;
import org.jacorb.orb.miop.MIOPProfile;
import org.jacorb.poa.AGM.TGTCWrapper;
import org.omg.CORBA.Policy;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.ETF.Factories;
import org.omg.ETF.Profile;
import org.omg.IOP.TAG_UIPMC;
import org.omg.PortableGroup.NotAGroupObject;
import org.omg.PortableGroup.TagGroupTaggedComponent;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import org.omg.RTCORBA.ProtocolProperties;


/**
 * The Group Object Adapter.
 *
 * @author Alysson Neves Bessani
 * @version 1.0
 * @see org.jacorb.poa.POA
 */
public class GOA extends org.jacorb.poa.POA implements org.omg.PortableGroup.GOA
{
   // Active Group Map
   private AGM          agm;

   // Table for OIDs to port mappings
   private Map          portTable;

   private MIOPListener listener  = null;

   private Logger logger;


   /**
    * Creates a new instance of GOA
    *
    * @param orb
    * @param name
    * @param parent
    * @param manager
    * @param policies
    */
   public GOA (ORB orb, String name, org.jacorb.poa.POA parent, POAManager manager,
            Policy[] policies)
   {
      super (orb, name, parent, manager, policies);

      Factories f =  orb.getTransportManager ().getFactories (TAG_UIPMC.value);
      logger = orb.getConfiguration ().getLogger("jacorb.poa");

      if ( f != null)
      {
         listener = (MIOPListener)f.create_listener ((ProtocolProperties)null, 0, (short)0);

         agm = new AGM();
         portTable = new HashMap ();
      }
   }


   /**
    * Removes a member servant from this group.
    *
    * @param ref the group ref
    * @param oid the member servant oid
    *
    * @throws NotAGroupObject if ref is not a group reference
    */
   public void disassociate_reference_with_id (org.omg.CORBA.Object ref, byte[] oid) throws NotAGroupObject
   {

      MIOPProfile profile = getMIOPProfile (ref);

      removeGroupMember (profile, oid);
   }


   /**
    * Add a member servant to this group
    *
    * @param ref the group ref
    * @param oid the member servant oid
    *
    * @throws NotAGroupObject if ref is not a group reference
    */
   public void associate_reference_with_id (org.omg.CORBA.Object ref, byte[] oid) throws NotAGroupObject
   {

      MIOPProfile profile = getMIOPProfile (ref);

      addGroupMember (profile, oid);
   }


   /**
    * Create an oid member for this group. It must be used to register a servant
    * as member of the group with the activate_object_with_id() method.
    *
    * @param ref the group reference
    * @return ths created oid already member of this group
    *
    * @throws NotAGroupObject if ref is not a group reference
    */
   public byte[] create_id_for_reference (org.omg.CORBA.Object ref) throws NotAGroupObject
   {

      MIOPProfile profile = getMIOPProfile (ref);

      // This method in POA is private, we must change
      byte[] oid = generateObjectId ();

      addGroupMember (profile, oid);

      return oid;
   }


   /**
    * Get the members of the group.
    *
    * @param ref the group ref
    * @return the group members oid
    *
    * @throws NotAGroupObject if ref is not a group reference
    */
   public byte[][] reference_to_ids (org.omg.CORBA.Object ref) throws NotAGroupObject
   {
      return agm.getMembersFromGroup (getMIOPProfile (ref).getTagGroup ());
   }


   /**
    * Process the group request through the POA tree.
    *
    * @param tagGroup the target group logical identification
    * @param request the request to be processed
    */
   public void processGroupRequest (TagGroupTaggedComponent tagGroup, ServerRequest request)
   {
      // deliver the request for all members in this POA
      byte[][] members = agm.getMembersFromGroup (tagGroup);

      if (members.length > 0)
      {
         // get the ORB and the request buffer
         ORB orb = getORB ();
         byte[] buffer = request.getInputStream ().getBufferCopy ();

         for (int i = 0; i < members.length; i++)
         {
            ServerRequest requestClone = new ServerRequest (orb, new RequestInputStream (orb, request.getConnection (),
                     buffer), request.getConnection ());
            try
            {
               byte[] oid = members[i];
               org.omg.CORBA.Object member = id_to_reference (oid);
               Delegate delegate = (Delegate)((ObjectImpl)member)._get_delegate ();
               requestClone.setObjectKey (delegate.getObjectKey ());
               _invoke (requestClone);
            }
            catch (WrongAdapter ex)
            {
               logger.error ("Caught exception processing group request", ex);
            }
            catch (ObjectNotActive ex)
            {
               logger.error ("Caught exception processing group request", ex);
            }
            catch (WrongPolicy ex)
            {
               logger.error ("Caught exception processing group request", ex);
            }
            catch( org.omg.CORBA.SystemException ex)
            {
               logger.error ("Caught SystemException processing group request", ex);
            }
            catch( RuntimeException ex)
            {
               logger.error ("Caught unknown exception processing group request", ex);
            }
         }
      }

      // propagates the request to the children POAs
      POA children[] = the_children ();

      for (int i = 0; i < children.length; i++)
      {
         ((GOA)children[i]).processGroupRequest (tagGroup, request);
      }
   }


   /**
    * Try to get the MIOP profile of an object reference.
    *
    * @param ref the group ref
    * @return the MIOP profile of the ref
    *
    * @throws NotAGroupObject if the ref doesn't have a MIOP profile
    */
   private MIOPProfile getMIOPProfile (org.omg.CORBA.Object ref) throws NotAGroupObject
   {
      Delegate delegate = (Delegate)((ObjectImpl)ref)._get_delegate ();

      Profile profile = delegate.getParsedIOR ().getEffectiveProfile ();

      if (profile instanceof MIOPProfile)
      {
         return (MIOPProfile)profile;
      }
      else
      {
         throw new NotAGroupObject (ref.toString ());
      }
   }


   /**
    * Add a member to this group.
    *
    * @param profile the group profile
    * @param oid the member oid
    */
   private final void addGroupMember (MIOPProfile profile, byte[] oid)
   {
      int nMembers = agm.addToGroup (profile.getTagGroup (), oid);

      if ((nMembers == 1) && !listener.haveGroupConnection (profile.getUIPMCProfile ().the_port))
      {
         // if this guy is the first member of the group and there is a multicast listener
         // port for that I will create it
         listener.addGroupConnection (profile);
      }

      portTable.put (oid, profile);
   }


   /**
    * Remove a member from this group.
    *
    * @param profile the group profile
    * @param oid the member oid
    */
   private final void removeGroupMember (MIOPProfile profile, byte[] oid)
   {
      if (agm.removeFromGroup (profile.getTagGroup (), oid) == 0)
      {
         // if this member was last in the group remove the group
         // from multicast listener
         listener.removeGroupConnection (profile.getUIPMCProfile ().the_port);
      }

      portTable.remove (oid);
   }


   /**
    * This method is used only in deactivate_object method
    * for group members deactivation
    *
    * @param oid the member oid
    */
   void clearGroupRegistration (byte[] oid)
   {
      if (agm != null)
      {
         ArrayList groupsKeys = agm.getGroupsWithMember (oid);

         for (int i = 0; i < groupsKeys.size(); i++)
         {
            if (agm.removeFromGroup (((TGTCWrapper)groupsKeys.get (i)).tcgtc, oid) == 0)
            {
               // If this member was last in the group remove the group
               // from the multicast listener
               listener.removeGroupConnection (((MIOPProfile)portTable.get (oid)).getUIPMCProfile ().the_port);
            }
         }
      }
   }
}
