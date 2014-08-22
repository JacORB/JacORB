/*
 * JacORB - a free Java ORB
 *
 * Copyright (C) 1997-2012 Gerald Brose / The JacORB Team.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.naming.namemanager;


import java.awt.Dimension;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jacorb.naming.Name;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;


/**
 * @author Gerald Brose, FU Berlin/XTRADYNE Technologies AG
 */

public class NSTree extends JTree
{
   private NamingContext     rootContext;

   private Dimension         size;

   private boolean           created;

   private org.omg.CORBA.ORB orb;

   public static NSTable     nsTable;


   public NSTree (int width, int height, NSTable theTable, NamingContext rootCntxt,
            org.omg.CORBA.ORB orb)
   {
      this.orb = orb;
      DefaultMutableTreeNode root = new DefaultMutableTreeNode ("RootContext");
      root.setAllowsChildren (true);
      setModel (new DefaultTreeModel (root, true));
      created = false;
      size = new Dimension (width, height);
      nsTable = theTable;
      rootContext = rootCntxt;
      ContextNode cn = new ContextNode (orb, rootContext, (DefaultTreeModel)getModel ());
      cn.setNode (root);
      root.setUserObject (cn);
   }


   /**
    * Bind a new name context and insert it
    */

   public void bind (String name) throws NotFound, CannotProceed, InvalidName, AlreadyBound
   {
      TreePath path = null;
      int length = 0;
      try
      {
         path = getSelectionPath ();
         length = path.getPathCount ();
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog (this, "Nothing selected", "Selection error",
                                        JOptionPane.ERROR_MESSAGE);
         return;
      }

      DefaultMutableTreeNode node = (DefaultMutableTreeNode)getModel ().getRoot ();
      NamingContext context = rootContext;

      if (length > 1)
      {
         for (int i = 1; i < length; i++)
         {
            node = (DefaultMutableTreeNode)path.getPathComponent (i);
            ContextNode bind = (ContextNode)node.getUserObject ();
            context = NamingContextHelper.narrow (context.resolve (bind.getName ()));
            if (context == null)
            {
               System.err.println ("Naming context narrow failed!");
               System.exit (1);
            }
         }
      }
      if (node.getAllowsChildren ())
      {
         Name bindname = new Name (name);
         if (context == null)
            System.err.println ("context null ");

         if (bindname.components () == null)
            System.err.println ("name is null ");

         context.bind_new_context (bindname.components ());
         update ();
      }
      else
      {
         JOptionPane.showMessageDialog (this, "Please select a naming context", "Selection error",
                                        JOptionPane.ERROR_MESSAGE);
      }
   }


   public void bindObject (String name, String ior, boolean isRebind) throws NotFound,
            CannotProceed, InvalidName, AlreadyBound
   {
      TreePath path = null;
      int length = 0;
      try
      {
         path = getSelectionPath ();
         length = path.getPathCount ();
      }
      catch (Exception e)
      {
         JOptionPane.showMessageDialog (this, "Nothing selected", "Selection error",
                                        JOptionPane.ERROR_MESSAGE);
         return;
      }

      DefaultMutableTreeNode node = (DefaultMutableTreeNode)getModel ().getRoot ();
      NamingContext context = rootContext;

      if (length > 1)
      {
         for (int i = 1; i < length; i++)
         {
            node = (DefaultMutableTreeNode)path.getPathComponent (i);
            ContextNode bind = (ContextNode)node.getUserObject ();
            context = NamingContextHelper.narrow (context.resolve (bind.getName ()));
            if (context == null)
            {
               System.err.println ("Naming context narrow failed!");
               System.exit (1);
            }
         }
      }
      if (node.getAllowsChildren ())
      {
         Name bindname = new Name (name);
         if (context == null)
            System.err.println ("context null ");

         if (bindname.components () == null)
            System.err.println ("name is null ");

         try
         {
            context.bind (bindname.components (), orb.string_to_object (ior));
         }
         catch (AlreadyBound ab)
         {
            if (isRebind)
               context.rebind (bindname.components (), orb.string_to_object (ior));
            else
               throw ab;
         }
         update ();
      }
      else
      {
         JOptionPane.showMessageDialog (this, "Please select a naming context", "Selection error",
                                        JOptionPane.ERROR_MESSAGE);
      }
   }


   public Dimension getPreferredSize ()
   {
      if (!created)
      {
         created = true;
         return size;
      }
      else
         return super.getPreferredSize ();
   }


   /**
    * unbind a context and remove it from this tree
    */

   public void unbind ()
   {
      DefaultMutableTreeNode node;
      NamingContext context = rootContext;
      TreePath path = null;
      int length = 0;
      try
      {
         path = getSelectionPath ();
         length = path.getPathCount ();
         if (length > 1)
         {
            for (int i = 1; i < length - 1; i++)
            {
               node = (DefaultMutableTreeNode)path.getPathComponent (i);
               ContextNode bind = (ContextNode)node.getUserObject ();
               context = NamingContextHelper.narrow (context.resolve (bind.getName ()));
            }
         }

         if (length > 0)
         {
            node = (DefaultMutableTreeNode)path.getPathComponent (length - 1);
            ContextNode binding = (ContextNode)node.getUserObject ();
            context.unbind (binding.getName ());
            DefaultTreeModel model = (DefaultTreeModel)getModel ();
            model.removeNodeFromParent (node);

            // select the parent node and display its content
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)path.getPathComponent (length - 2);
            setSelectionPath (new TreePath (parent.getPath ()));
            ((ContextNode)parent.getUserObject ()).display ();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace ();

         JOptionPane.showMessageDialog (this, "Nothing selected or invalid selection",
                                        "Selection error", JOptionPane.ERROR_MESSAGE);
      }
   }


   /**
    * update the entire tree of contexts
    */

   public synchronized void update ()
   {
      DefaultTreeModel model = (DefaultTreeModel)getModel ();
      ((ContextNode)((DefaultMutableTreeNode)model.getRoot ()).getUserObject ()).display();
      nsTable.update ();
   }
}
