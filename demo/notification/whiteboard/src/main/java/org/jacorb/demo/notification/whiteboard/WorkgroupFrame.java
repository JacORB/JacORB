package org.jacorb.demo.notification.whiteboard;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Alphonse Bendt
 */

public class WorkgroupFrame extends Frame implements IWorkgroupFrame {

    BrushSizeDrawCanvas drawCanvas_;
    SelectDialog dialog;
    GhostPainter ghost;
    //    PicView pic = new PicView("Daemon.jpg");
    WorkgroupController controller_;

    public BrushSizeDrawCanvas getDrawCanvas() {
        return drawCanvas_;
    }

    public Frame getFrame() {
	return this;
    }

    public List getList() {
        String[] s = controller_.getListOfWhiteboards();
        List v = new ArrayList();
        for (int n=0; n<s.length; n++) {
            v.add(s[n]);
	}
        return Collections.unmodifiableList(v);
    }

    public void setCurrentBoardText(String name) {
	currentBoard.setText(name);
    }

    public void setLeaveMenuItem(boolean value) {
	leaveMenuItem_.setEnabled(value);
    }

    /** Initializes the Form */
    public WorkgroupFrame(WorkgroupController wg, String name) {
        super(name);

        controller_ = wg;

        initComponents();
        pack ();
    }

    public WorkgroupFrame(WorkgroupController wg) {
        this(wg, "Whiteboard");
    }

    private void initComponents () {
        addWindowListener (new java.awt.event.WindowAdapter () {
		public void windowClosing (java.awt.event.WindowEvent evt) {
		    exitForm(evt);
		}
	    });
        setLayout (new java.awt.BorderLayout ());

        drawCanvas_ = new BrushSizeDrawCanvas(controller_, 400,400);
        add(drawCanvas_, BorderLayout.CENTER);

        menuBar1 = new MenuBar ();
        menu1 = new Menu ();
        menu1.setLabel ("Menu");
        select = new MenuItem ();
        select.setLabel ("Select");
        select.setName ("select");
        select.addActionListener (new ActionListener () {
		public void actionPerformed (ActionEvent evt) {
		    selectActionPerformed (evt);
		}
	    });
        menu1.add(select);

        leaveMenuItem_ = new java.awt.MenuItem ();
        leaveMenuItem_.setEnabled (false);
        leaveMenuItem_.setLabel ("Leave");
        leaveMenuItem_.setName ("leave");
        leaveMenuItem_.addActionListener (new ActionListener () {
		public void actionPerformed (ActionEvent evt) {
		    leaveActionPerformed (evt);
		}
	    });
        menu1.add(leaveMenuItem_);

        activateGhost = new java.awt.MenuItem();
        activateGhost.setLabel("Start Daemon");
        activateGhost.addActionListener (new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    activateGhostActionPerformed();
		}
	    });
	//        menu1.add(activateGhost);

        exit = new java.awt.MenuItem ();
        exit.setLabel ("Exit");
        exit.setName ("exit");
        exit.addActionListener (new java.awt.event.ActionListener () {
		public void actionPerformed (java.awt.event.ActionEvent evt) {
		    exitActionPerformed (evt);
		}
	    });
        menu1.add(exit);

        menuBar1.add(menu1);

        setMenuBar(menuBar1);
        panel1 = new java.awt.Panel ();
        panel1.setLayout (new java.awt.FlowLayout ());

	//        pic.setVisible(false);
	//        panel1.add(pic);

        draw = new java.awt.Button ();
        draw.setLabel ("Draw");
        draw.addActionListener (new java.awt.event.ActionListener () {
		public void actionPerformed (java.awt.event.ActionEvent evt) {
		    drawActionPerformed (evt);
		}
	    });
        panel1.add (draw);

        delete = new java.awt.Button ();
        delete.setLabel ("Delete");
        delete.setName ("delete");
        delete.addActionListener (new java.awt.event.ActionListener () {
		public void actionPerformed (java.awt.event.ActionEvent evt) {
		    deleteActionPerformed (evt);
		}
	    });
        panel1.add (delete);

        clear = new java.awt.Button ();
        clear.setLabel ("Clear");
        clear.setName ("clear");
        clear.addActionListener (new java.awt.event.ActionListener () {
		public void actionPerformed (java.awt.event.ActionEvent evt) {
		    clearActionPerformed (evt);
		}
	    });
        panel1.add (clear);

        currentBoard = new java.awt.TextField(12);
        currentBoard.setName("currentboard");
        currentBoard.setEditable(false);
        currentBoard.setText("No Selection");
        panel1.add(currentBoard);

        add (panel1, "South");
    }

    private void activateGhostActionPerformed() {
        if (activateGhost.getLabel() == "Start Daemon" ) {
            ghost = new GhostPainter(this,400,400);
            ghost.start();
            activateGhost.setLabel("Stop Daemon");
	    //            pic.setVisible(true);
        } else {
            ghost.shutdown();
            activateGhost.setLabel("Start Daemon");
	    //            pic.setVisible(false);
        }
    }

//     public void selectOk(String r) {
//         leave.setEnabled(true);
//         currentBoard.setText(r);
// 	//        drawCanvas_.localClearAll();
// 	//        workgroup_.joinWhiteboard(r);
//     }

    private void clearActionPerformed (java.awt.event.ActionEvent evt) {
        drawCanvas_.clearAll();
    }

    private void deleteActionPerformed (java.awt.event.ActionEvent evt) {
        drawCanvas_.setDrawColor(0,0,0);
        drawCanvas_.setBrushSize(5);
    }

    private void drawActionPerformed (java.awt.event.ActionEvent evt) {
        drawCanvas_.setDrawColor(255,255,255);
        drawCanvas_.setBrushSize(1);
    }

    private void exitActionPerformed (java.awt.event.ActionEvent evt) {
	controller_.exit();
    }

    private void leaveActionPerformed (java.awt.event.ActionEvent evt) {
        leaveMenuItem_.setEnabled(false);
        currentBoard.setText("No Selection");
        controller_.leaveWhiteboard();
    }

    private void selectActionPerformed (java.awt.event.ActionEvent evt) {
        dialog = new SelectDialog(this, controller_);
        dialog.show();
    }

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {
	controller_.exit();
    }

    private java.awt.Panel panel1;
    private java.awt.Button draw,delete,clear;
    private java.awt.MenuBar menuBar1;
    private java.awt.Menu menu1;
    private java.awt.MenuItem select,exit,activateGhost,leaveMenuItem_;
    private java.awt.TextField currentBoard;
}
