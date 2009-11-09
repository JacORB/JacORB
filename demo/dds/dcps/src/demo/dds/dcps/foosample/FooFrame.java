/*
*  DDS (Data Distribution Service) for JacORB
*
* Copyright (C) 2005  , Ahmed yehdih <ahmed.yehdih@gmail.com>, fouad
* allaoui <fouad.allaoui@gmail.com>, Didier Donsez (didier.donsez@ieee.org)
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Library General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Library General Public License for more details.
*
* You should have received a copy of the GNU Library General Public 
* License along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
* 02111-1307, USA.
*
* Coontact: Ahmed yehdih <ahmed.yehdih@gmail.com>, fouad allaoui
* <fouad.allaoui@gmail.com>, Didier Donsez (didier.donsez@ieee.org)
* Contributor(s)
*
**/
package demo.dds.dcps.foosample;

public class FooFrame extends javax.swing.JFrame {
           
    public String Temperature_String;
    
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JMenuBar menuBar;
    
    public FooFrame() {
        initComponents();
    }
        
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(new java.awt.BorderLayout());        
        jPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {});        
        jLabel1.setBackground(new java.awt.Color(153, 51, 255));
        jLabel1.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 30));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("jLabel1");
        jLabel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});       
        jPanel1.add(jLabel1, java.awt.BorderLayout.CENTER);
        
        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        
        fileMenu.setText("File");
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        
        fileMenu.add(exitMenuItem);        
        menuBar.add(fileMenu);        
        setJMenuBar(menuBar);        
        setBounds(500, 350, 300, 200);
        
        this.setTitle("Foo");
    }
        
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }
    
    public void SetText(double value ){
        this.jLabel1.setText(""+value);
    }
    
    public void SetText(String message ){
        this.jLabel1.setText(message);
    }
    
    public void SetColorLablel( ){
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));;
    }
       
    public static void main(String args[]) {
        FooFrame Test =   new FooFrame() ;
        Test.SetText(30);
        Test.setVisible(true);
    }               
}