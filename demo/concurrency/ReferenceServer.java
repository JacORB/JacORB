package demo.concurrency;

import java.io.*;
import java.net.Socket.*;
import java.net.*;

public class ReferenceServer implements Runnable{

   private org.omg.CORBA.Object ref;
   private int port_num;

   public ReferenceServer(org.omg.CORBA.Object _ref, int port){
      ref      = _ref;
      port_num = port;
   }

   public void run(){
      Socket conn = null;
      PrintWriter quoteSend = null;
      try {
         ServerSocket server = new ServerSocket(port_num);
         for (;;){
            conn = server.accept();
            quoteSend = new PrintWriter(conn.getOutputStream());
            quoteSend.print(ref);
            quoteSend.flush();

            try {
                Thread.currentThread().sleep(3000);
            } catch(InterruptedException e) {
            }

            quoteSend.close();
            conn.close();
         }
      } catch (UnknownHostException e){
         e.printStackTrace(); 
      } catch (IOException ioe){
         ioe.printStackTrace(); 
      } finally {
         if (quoteSend != null){
            quoteSend.close();
         }
         if (conn != null){
            try {
               conn.close();
            } catch (IOException ioe){
               ioe.printStackTrace(); 
            }
         }
      }
   }
}
