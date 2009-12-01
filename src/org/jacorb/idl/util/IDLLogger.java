package org.jacorb.idl.util;

/**
 * This class is a wrapper for native Java logging tools, providing
 * logging capabilities to JaCORB idl compiler. <code>java.util.logging.Logger</code>
 * is used as delegate.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class IDLLogger
{
   
   /**
    * @author ykovalek
    * Wrapper class for java.io.Writer to provide IDL compiler compatibility
    * with native java logging output. Emulates an OutputStream for Writer.
    *
    */
   private class WriterOutputStream extends OutputStream
   {
       protected Writer writer;
       protected String encoding;
       private byte[] buf=new byte[1];
       
       public WriterOutputStream(Writer writer, String encoding)
       {
           this.writer = writer; 
           this.encoding = encoding;
       }
       
       public WriterOutputStream(Writer writer)
       {
           this.writer = writer;
       }

       public void close()
           throws IOException
       {
           writer.close();
           writer = null;
           encoding = null;
       }

       public void flush()
           throws IOException
       {
           writer.flush();
       }

       public void write(byte[] b) 
           throws IOException
       {
           if (encoding == null)
               writer.write(new String(b));
           else
               writer.write(new String(b,encoding));
       }

       public void write(byte[] b, int off, int len)
           throws IOException
       {
           if (encoding == null)
               writer.write(new String(b,off,len));
           else
               writer.write(new String(b,off,len,encoding));
       }

       public synchronized void write(int b)
           throws IOException
       {
           buf[0] = (byte)b;
           write(buf);
       }
   }
   
   public static final Level DEBUG = Level.ALL;
   public static final Level INFO = Level.FINEST;
   public static final Level WARN = Level.WARNING;
   public static final Level ERROR = Level.SEVERE;
   public static final Level FATAL_ERROR = Level.SEVERE;
   public static final Level NONE = Level.OFF;
   
   private static IDLLogger theLogger;
   
   private Logger logger;
   
   /**
    * Class constructor.
    * @param name a <code>String</code> name for logged entity
    * @param level a <code>java.util.logging.Level</code> value 
    * @param writer a <code>java.io.Writer</code> where to send the log output
    */
   private IDLLogger(String name, Level level, Writer writer)
   {
      this.logger = Logger.getLogger(name);
      this.logger.setLevel(level);
      
      this.logger.addHandler(new StreamHandler(new WriterOutputStream(writer), new SimpleFormatter()));
   }
   
   /**
    * @param name a <code>String</code> name for logged entity
    * @param level a <code>java.util.logging.Level</code> value 
    * @param writer a <code>java.io.Writer</code> where to send the log output
    * @return a <code>org.jacorb.idl.util.IDLLogger</code> logger
    */
   public static IDLLogger getLogger(String name, Level level, Writer writer)
   {
      if (IDLLogger.theLogger == null)
      {
         IDLLogger.theLogger = new IDLLogger(name, level, writer);
      }
      return IDLLogger.theLogger;
   }
   
   /**
    * Sets the logging level. Use one of the levels defined in
    * <code>org.jacorb.idl.util.IDLLogger</code> The parameter
    * is then passed to the delegate logger.
    * @param level a <code>java.util.logging.Level</code> value
    */
   public void setLevel(Level level)
   {
      this.logger.setLevel(level);
   }
   
   /**
    * Queries the delegate logger whether the debug level is enabled.
    * @return true or false
    */
   public boolean isDebugEnabled()
   {
      return this.logger.isLoggable(IDLLogger.DEBUG);
   }
   
   /**
    * Queries the delegate logger whether the info level is enabled.
    * @return true or false
    */
   public boolean isInfoEnabled()
   {
      return this.logger.isLoggable(IDLLogger.INFO);
   }
   
   /**
    * Queries the delegate logger whether the warn level is enabled.
    * @return true or false
    */
   public boolean isWarnEnabled()
   {
      return this.logger.isLoggable(IDLLogger.WARN);
   }
   
   /**
    * Queries the delegate logger whether the error level is enabled.
    * @return true or false
    */
   public boolean isErrorEnabled()
   {
      return this.logger.isLoggable(IDLLogger.ERROR);
   }
   
   /**
    * Queries the delegate logger whether the fatal error level is enabled.
    * @return true or false
    */
   public boolean isFatalErrorEnabled()
   {
      return this.logger.isLoggable(IDLLogger.FATAL_ERROR);
   }
   
   /**
    * Queries the delegate logger whether the specified logging level is enabled.
    * It's best to check against the levels defined in the wrapper class
    * <code>org.jacorb.idl.util.IDLLogger</code>
    * @return true or false
    */
   public boolean isLevelEnabled(Level level)
   {
      return this.logger.isLoggable(level);
   }
   
   /**
    * Logs a debug level message
    * @param message a <code>java.lang.String</code>
    */
   public void debug(String message)
   {
      this.logger.log(IDLLogger.DEBUG, message);
   }
   
   /**
    * Logs a debug level message
    * @param message a <code>java.lang.String</code>
    * @param th a <code>java.lang.Throwable</code>
    */
   public void debug(String message, Throwable th)
   {
      this.logger.log(IDLLogger.DEBUG, message, th);
   }

   /**
    * Logs a warn level message
    * @param message a <code>java.lang.String</code>
    */
   public void warn(String message)
   {
      this.logger.log(IDLLogger.WARN, message);
   }
   
   /**
    * Logs a warn level message
    * @param message a <code>java.lang.String</code>
    * @param th a <code>java.lang.Throwable</code>
    */
   public void warn(String message, Throwable th)
   {
      this.logger.log(IDLLogger.WARN, message, th);
   }

   /**
    * Logs an error level message
    * @param message a <code>java.lang.String</code>
    */
   public void error(String message)
   {
      this.logger.log(IDLLogger.ERROR, message);
   }
   
   /**
    * Logs an error level message
    * @param message a <code>java.lang.String</code>
    * @param th a <code>java.lang.Throwable</code>
    */
   public void error(String message, Throwable th)
   {
      this.logger.log(IDLLogger.ERROR, message, th);
   }
   
   /**
    * Logs a fatal error level message
    * @param message a <code>java.lang.String</code>
    */
   public void fatalError(String message)
   {
      this.logger.log(IDLLogger.FATAL_ERROR, message);
   }
   
   /**
    * Logs a fatal error level message
    * @param message a <code>java.lang.String</code>
    * @param th a <code>java.lang.Throwable</code>
    */
   public void fatalError(String message, Throwable th)
   {
      this.logger.log(IDLLogger.FATAL_ERROR, message, th);
   }

   /**
    * Logs an info level message
    * @param message a <code>java.lang.String</code>
    */
   public void info(String message)
   {
      this.logger.log(IDLLogger.INFO, message);
   }
   
   /**
    * Logs an info level message
    * @param message a <code>java.lang.String</code>
    * @param th a <code>java.lang.Throwable</code>
    */
   public void info(String message, Throwable th)
   {
      this.logger.log(IDLLogger.INFO, message, th);
   }
}
