package org.jacorb.trading.test;

import java.io.*;
/**
 * This class writes the data of a processes output stream to a file.
 * This is needed since when using exec() the new process has no stdout.
 *
 * @author Nicolas Noffke
 */

public class OutputForwarder extends Thread {
    private File m_out = null;
    private Process m_process = null;
    
    public OutputForwarder (Process proc, String filename){
	m_process = proc;
	m_out = new File(filename);

	start();
    }
    
    public void run(){
	BufferedReader _in = new BufferedReader(new InputStreamReader(m_process.getInputStream()));
	String _line = null;
	PrintWriter _out = null;
	
	try{
	     _out = new PrintWriter(new FileWriter(m_out));

	    // If we get null from readLine() we assume that the process has exited.
	    // Unfortunately there is no exception thrown when trying to read from
	    // a dead processes output stream.
	    while((_line = _in.readLine()) != null)
		_out.println(_line);

	}catch (Exception _e){
	    _e.printStackTrace();
	}
	finally{
	    try{
		_in.close();
		_out.flush();
		_out.close();
	    }catch (Exception _e){
	    _e.printStackTrace();
	    }
	}
	System.out.println("A Trader has exited");
	
    }
} // OutputForwarder






