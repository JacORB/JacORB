package org.jacorb.util;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.*;

import java.io.*;
import java.util.*;
/*
 * CAD.java
 *
 * Created on 21. Juni 2000, 11:52
 *
 * @author  Nicolas Noffke
 * $Id$
 */
public class CAD extends javax.swing.JFrame 
{

    private javax.swing.JTable bit_table;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel int_lbl;
    private javax.swing.JLabel categories_lbl;
    private javax.swing.JLabel level_lbl;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList categories_list;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList levels_list;
    private javax.swing.JButton print_btn;
    private javax.swing.JButton exit_btn;

    private CADTableModel model = null;
    private int debug_int = 0;

    /** Creates new form CAD */
    public CAD() 
    {
        initComponents ();
        pack ();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents () 
    {
        final String[] levels = {"Quiet", "Important", 
                                 "Information", "Debug1"};

        final String[] categories = {"ORB - Connect",
                                     "ORB - Misc",
                                     "POA",
                                     "ImR",
                                     "DSI",
                                     "DII",
                                     "Interceptor",
                                     "Domain",
                                     "Proxy",
                                     "Tools",
                                     "Naming",
                                     "Trading",
                                     "Events",
                                     "Transaction",
                                     "Security"};

        final int[] category_vals = {Debug.ORB_CONNECT,
                                     Debug.ORB_MISC,
                                     Debug.POA,
                                     Debug.IMR,
                                     Debug.DSI,
                                     Debug.DII,
                                     Debug.INTERCEPTOR,
                                     Debug.DOMAIN,
                                     Debug.PROXY,
                                     Debug.TOOLS,
                                     Debug.NAMING,
                                     Debug.TRADING,
                                     Debug.EVENTS,
                                     Debug.TRANSACTION,
                                     Debug.SECURITY};
        
        final int[] category_bit_pos = {8,
                                        9,
                                        10,
                                        11,
                                        12,
                                        13, 
                                        14, 
                                        15,
                                        16, 
                                        17, 
                                        24,
                                        25,
                                        26, 
                                        27, 
                                        28};

        bit_table = new javax.swing.JTable (model = new CADTableModel());
        jPanel1 = new javax.swing.JPanel ();
        jLabel1 = new javax.swing.JLabel ();
        int_lbl = new javax.swing.JLabel ();
        categories_lbl = new javax.swing.JLabel ();
        level_lbl = new javax.swing.JLabel ();
        jSplitPane1 = new javax.swing.JSplitPane ();
        jScrollPane1 = new javax.swing.JScrollPane ();
        categories_list = new javax.swing.JList (categories);
        jScrollPane2 = new javax.swing.JScrollPane ();
        //        jScrollPane3 = new javax.swing.JScrollPane (bit_table);
        levels_list = new javax.swing.JList (levels);
        print_btn = new javax.swing.JButton ();
        exit_btn = new javax.swing.JButton ();
        setTitle ("Click-a-Debug-Level");
        addWindowListener (new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) 
                {
                exitForm (evt);
            }
        }
                           );

        debug_int = org.jacorb.util.Environment.verbosityLevel();
        
        for(int i = 0; i < 32; i++)
            model.setBit(i, (debug_int >> i) & 1);
        
        for (int i = 0; i < 8; i++)
            bit_table.getColumnModel().getColumn(i).setPreferredWidth(30);

        Vector v = new Vector();
        for (int i = 0; i < category_bit_pos.length; i++)        
            if (((debug_int >> category_bit_pos[i]) & 1) == 1)
                v.addElement(new Integer(i));

        int[] indices = new int[v.size()];
        for (int i = 0; i < v.size(); i++)
            indices[i] = ((Integer) v.elementAt(i)).intValue();

        categories_list.setSelectedIndices(indices);
        
        levels_list.setSelectedIndex(debug_int & 255);

        getContentPane().add (bit_table.getTableHeader(), java.awt.BorderLayout.NORTH);
        getContentPane().add (bit_table, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout (new java.awt.GridBagLayout ());
        java.awt.GridBagConstraints gridBagConstraints1;

        jLabel1.setText ("As int:");
  
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        jPanel1.add (jLabel1, gridBagConstraints1);
  
        int_lbl.setText ("" + debug_int);
        int_lbl.setHorizontalAlignment (javax.swing.SwingConstants.LEFT);
  
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        jPanel1.add (int_lbl, gridBagConstraints1);
  
        categories_lbl.setText ("Categories:");
        categories_lbl.setHorizontalAlignment (javax.swing.SwingConstants.LEFT);
  
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add (categories_lbl, gridBagConstraints1);
  
        level_lbl.setText ("Debug Level:");
        level_lbl.setHorizontalAlignment (javax.swing.SwingConstants.LEFT);
  
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        jPanel1.add (level_lbl, gridBagConstraints1);
  
        jScrollPane1.setViewportView (categories_list);      
        jSplitPane1.setLeftComponent (jScrollPane1);
          
        jScrollPane2.setViewportView (levels_list);      
        jSplitPane1.setRightComponent (jScrollPane2);
    
        categories_list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        //evaluate categories selection
        categories_list.addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
                {
                for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++)
                { 
                    if(categories_list.isSelectedIndex(i))
                    {
                        model.setBit(category_bit_pos[i], 1);
                        debug_int |= category_vals[i];
                        
                    }
                    else
                    {
                        model.setBit(category_bit_pos[i], 0);
                        debug_int &= category_vals[i] * -1 - 1;
                    }
                }
                
                int_lbl.setText("" + debug_int);
            }
        }
                                                 );
        
        levels_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //evaluate levels selection
        levels_list.addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
                {                
                for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++)
                {
                    if(levels_list.isSelectedIndex(i))
                    {
                        //consider lower four bit
                        for (int j = 0; j < 4; j++)
                            model.setBit(j, (i >> j) & 1);

                        debug_int &= 0xffffff00; //set level to 0
                        debug_int |= i;  //set level to new value
                    }
                }
                int_lbl.setText("" + debug_int);
            }
        }
                                             );
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 2;
        gridBagConstraints1.gridwidth = 2;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        jPanel1.add (jSplitPane1, gridBagConstraints1);
  
        print_btn.setText ("Write");
        print_btn.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                JFileChooser fc = new JFileChooser(System.getProperty("user.home"));
                fc.setDialogTitle("Select the properties file...");
                String prefix = System.getProperty("user.home") +
                    System.getProperty("file.separator");

                File f = new File(prefix + "jacorb.properties");
                if (! f.exists())
                    f = new File(prefix + ".jacorb_properties");
                
                if (f.exists())
                    fc.setSelectedFile(f);
                
                if (fc.showDialog(CAD.this, "Write") == 
                    JFileChooser.APPROVE_OPTION)
                {
                    try
                    {
                        f = fc.getSelectedFile();
                        File tmp = new File(f.getAbsolutePath() + ".tmp");
                    
                        BufferedReader f_in = new BufferedReader(new FileReader(f));
                        PrintWriter f_out = new PrintWriter(new FileWriter(tmp));
                    
                        String line = null;
                        boolean found = false;
                        while((line = f_in.readLine()) != null)
                        {
                            if (!found && 
                                line.startsWith("jacorb.verbosity="))
                            {
                                f_out.println("jacorb.verbosity=" + debug_int);
                                found = true;
                            }
                            else
                                f_out.println(line);
                        }
                    
                        if (! found)
                            f_out.println("jacorb.verbosity=" + debug_int);

                        f_in.close();
                        f_out.flush();
                        f_out.close();
                    
                        tmp.renameTo(f);
                    }catch(Exception e)
                    {
                        Debug.output(Debug.TOOLS | Debug.IMPORTANT, e);
                    }
                }
            }
        }
                                    );
  
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 3;
        jPanel1.add (print_btn, gridBagConstraints1);
  
        exit_btn.setText ("Exit");
        exit_btn.addActionListener (new java.awt.event.ActionListener () {
            public void actionPerformed (java.awt.event.ActionEvent evt) {
                doExit (evt);
            }
        }
                                    );
  
        gridBagConstraints1 = new java.awt.GridBagConstraints ();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 3;
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add (exit_btn, gridBagConstraints1);
  

        getContentPane ().add (jPanel1, java.awt.BorderLayout.SOUTH);

    }

    private void doExit (java.awt.event.ActionEvent evt) 
    {
        System.exit (0);
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) 
    {
        System.exit (0);
    }

    /**
     * @param args the command line arguments
     */
    public static void main (String args[]) 
    {
        new CAD().show ();
    }
}
