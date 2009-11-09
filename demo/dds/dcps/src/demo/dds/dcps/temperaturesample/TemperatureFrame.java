
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
package demo.dds.dcps.temperaturesample;

import java.awt.LayoutManager;


public class TemperatureFrame extends javax.swing.JFrame {
           
    public String Temperature_String;
    public TemperatureDataReaderListenerImpl parent ; 
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JMenuBar menuBar;
    
    public TemperatureFrame( TemperatureDataReaderListenerImpl parent) {
        initComponents();
        this.parent = parent ;
    }
        
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setLayout(null);        
        jPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {});        
        
        jLabel1.setBackground(new java.awt.Color(153, 51, 255));
        jLabel1.setFont(new java.awt.Font("Microsoft Sans Serif", 1, 50));        
        jLabel1.setText("jLabel1");
        jLabel1.setLocation(60,10);
        jLabel1.setSize(200,100);
        jLabel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {});         
        jPanel1.add(jLabel1);
       
        
       
        jButton1.setLabel("Click Here to product Alarm");
        jButton1.setLocation(30,100);
        jButton1.setSize(200,30);
        jButton1.setVisible(false);
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jButton1MouseReleased(evt);
            }
        });
        jPanel1.add(jButton1, null);
               
        
               
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
        
        this.setTitle("Temperature");
    }
        
    public void setjButton1Visible(boolean b) {
        jButton1.setVisible(b);
    }
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }
    
    public void SetText(double temperature ){
        this.jLabel1.setText(temperature+" C");        
    }
    
    public void SetText(String message ){
        this.jLabel1.setText(message);
    }
    
  
    public void SetColorLablel( ){
        jLabel1.setForeground(new java.awt.Color(204, 0, 0));;
    }
       
    private void jButton1MouseReleased(java.awt.event.MouseEvent evt) {
        parent.sendMessageForProducer("ALERT !");
        jButton1.setVisible(false);
    }            
}