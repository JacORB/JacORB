package org.jacorb.poa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import org.omg.PortableGroup.TagGroupTaggedComponent;


/**
 * Active Group Map Map groups with its members oids.
 *
 * @author Alysson Neves Bessani
 * @author Nick Cross
 * @version 1.0
 * @see GOA
 */
public class AGM
{
   private static final byte [][]EMPTYBYTE = new byte[0][];

   private HashMap groupTable = new HashMap ();


   /**
    * Adds an oid to a group represented by the TagGroupTaggedComponent
    *
    * @param tagGroupTaggedComponent
    * @param oid
    * @return the size of the group
    */
   synchronized int addToGroup (TagGroupTaggedComponent tagGroupTaggedComponent, byte[] oid)
   {
      TGTCWrapper t = new TGTCWrapper (tagGroupTaggedComponent);
      ArrayList members = (ArrayList)groupTable.get (t);

      if (members == null)
      {
         members = new ArrayList ();
         groupTable.put (t, members);
      }

      // If it does not contain the OID insert it
      if (!members.contains (oid))
      {
         members.add (oid);
      }
      return members.size ();
   }


   /**
    * Removes a stringfied oid to a group (represented by the stringfied group
    * key)
    *
    * @param tagGroupTaggedComponent
    * @param oid
    * @return the size of the group
    */
   synchronized int removeFromGroup (TagGroupTaggedComponent tagGroupTaggedComponent, byte[] oid)
   {
      TGTCWrapper t = new TGTCWrapper (tagGroupTaggedComponent);
      ArrayList members = (ArrayList)groupTable.get (t);

      if (members == null)
      {
         // no group exists in the table so return 0 for no members.
         return 0;
      }

      members.remove (oid);

      int nMembers = members.size ();

      // check if there members in this group (group empty), if empty
      // remove the entry from the group table.
      if (nMembers == 0)
      {
         groupTable.remove (t);
      }

      return nMembers;
   }


   /**
    * Returns the group members
    *
    * @param tagGroup
    * @return the group members oids as strings
    */
   final byte[][] getMembersFromGroup (TagGroupTaggedComponent tagGroup)
   {
      TGTCWrapper t = new TGTCWrapper (tagGroup);
      ArrayList members = (ArrayList)groupTable.get (t);

      return
      (
         members == null ?
         // no group in the table, has no members
         EMPTYBYTE
         :
         // group exists, return the array of members
         (byte[][])members.toArray (EMPTYBYTE)
      );
   }


   /**
    * Returns the groups in which the object is registered.
    *
    * @param oid
    * @return the groups that have this object as a member
    */
   final ArrayList getGroupsWithMember (byte[] oid)
   {
      ArrayList keyList = new ArrayList ();

      for (Iterator iterator = groupTable.keySet ().iterator (); iterator.hasNext ();)
      {
         TGTCWrapper t = (TGTCWrapper)iterator.next ();
         byte [][]oids = getMembersFromGroup (t.tcgtc);

         for (int i = 0; i < oids.length; i++)
         {
            if (Arrays.equals (oids[i], oid))
            {
               keyList.add (t);
               continue;
            }
         }
      }

      return keyList;
   }


   /**
    * TGTCWrapper is a simple wrapper around a TagGroupTaggedComponent. It
    * allows a TagGroupTaggedComponent to be used as a key in a hashmap as its
    * implementation of hashCode and equals actually is an implementation for
    * the component not the wrapper.
    */
   static class TGTCWrapper
   {
      TagGroupTaggedComponent tcgtc;


      public TGTCWrapper (TagGroupTaggedComponent tagGroupTaggedComponent)
      {
         tcgtc = tagGroupTaggedComponent;
      }


      /**
       * This implementation manually generates hashcode on the contained component
       * rather than delegating to its implementation (as its generated code it doesn't have one).
       *
       * @see java.lang.Object#hashCode()
       */
      public int hashCode ()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result +
                  ((tcgtc.group_domain_id == null) ? 0 : tcgtc.group_domain_id.hashCode ());
         result = prime * result + ((tcgtc.group_version == null) ? 0 : tcgtc.group_version.major);
         result = prime * result + ((tcgtc.group_version == null) ? 0 : tcgtc.group_version.minor);
         result = prime * result + (int)(tcgtc.object_group_id ^ (tcgtc.object_group_id >>> 32));
         result = prime * result + tcgtc.object_group_ref_version;
         return result;
      }


      /**
       * This implementation manually checks for equality on the contained component
       * rather than delegating to its implementation (as its generated code it doesn't have one).
       *
       * @see java.lang.Object#equals(java.lang.Object)
       */
      public boolean equals (Object obj)
      {
         if (this == obj)
         {
            return true;
         }
         if (obj == null)
         {
            return false;
         }
         if (!(obj instanceof TGTCWrapper))
         {
            return false;
         }
         TGTCWrapper tother = (TGTCWrapper)obj;
         if (tcgtc == null)
         {
            if (tother.tcgtc != null)
            {
               return false;
            }
         }

         TagGroupTaggedComponent other = ((TGTCWrapper)obj).tcgtc;
         if (tcgtc.group_domain_id == null)
         {
            if (other.group_domain_id != null)
            {
               return false;
            }
         }
         else if (!tcgtc.group_domain_id.equals (other.group_domain_id))
         {
            return false;
         }
         if (tcgtc.group_version == null)
         {
            if (other.group_version != null)
            {
               return false;
            }
         }
         else if (!(tcgtc.group_version.major == other.group_version.major && tcgtc.group_version.minor == other.group_version.minor))
         {
            return false;
         }
         if (tcgtc.object_group_id != other.object_group_id)
         {
            return false;
         }
         if (tcgtc.object_group_ref_version != other.object_group_ref_version)
         {
            return false;
         }
         return true;
      }
   }
}
