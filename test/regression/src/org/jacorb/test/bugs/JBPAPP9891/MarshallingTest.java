/*
 * JBoss, Home of Professional Open Source
 * Copyright (c) 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jacorb.test.bugs.JBPAPP9891;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.test.common.ORBTestCase;
import org.jacorb.test.common.TestUtils;
import org.omg.CORBA.ORB;
import org.omg.CORBA_2_3.portable.OutputStream;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @author
 */
public class MarshallingTest extends ORBTestCase
{
   private static Object invoke(String methodClass, Object obj, String methodName, Class<?> parameterTypes[], Object args[])
   {
      try
      {
         Method method = Class.forName(methodClass).getDeclaredMethod(methodName, parameterTypes);
         method.setAccessible(true);
         return method.invoke(obj, args);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException(e.getTargetException());
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);
      }
      catch ( SecurityException e )
      {
          throw new RuntimeException(e);
      }
      catch ( ClassNotFoundException e )
      {
          throw new RuntimeException(e);
      }
   }


   public void testSomething() throws Exception
   {
      Something value = new Something();
      value.value = "Hello world";
      value.number = -10;

      ORB foreignorb = ORB.init(new String[]{}, TestUtils.newForeignORBProperties());
      OutputStream out = (OutputStream) foreignorb.create_output_stream();
      out.write_value(value);

      byte []result;
      if (TestUtils.isIBM)
      {
          result = (byte[])invoke("com.ibm.rmi.iiop.CDROutputStream", out, "toByteArray", new Class<?>[] {}, new Object[] {});
      }
      else
      {
          result = (byte[])invoke("com.sun.corba.se.impl.encoding.CDROutputStream", out, "toByteArray", new Class<?>[] {}, new Object[] {});

      }

      Properties properties = new Properties();
      properties.put("jacorb.interop.sun", "on");
      ORB jacorborb = org.omg.CORBA.ORB.init (new String[]{}, properties);
      CDRInputStream in = new CDRInputStream(jacorborb, result);

      Something s = (Something)in.read_value();

      System.out.println("### testSomething::value is : " + s.value + " and the number is " + s.number);

      foreignorb.shutdown(true);
      jacorborb.shutdown(true);
      assertTrue (s.number == -10);
   }


   public void testException() throws Exception
   {
      NegativeArgumentException value = new NegativeArgumentException(-10);

      ORB foreignorb = ORB.init(new String[]{}, TestUtils.newForeignORBProperties());
      OutputStream out = (OutputStream) foreignorb.create_output_stream();
      out.write_value(value);

      byte []result;
      if (TestUtils.isIBM)
      {
          result = (byte[])invoke("com.ibm.rmi.iiop.CDROutputStream", out, "toByteArray", new Class<?>[] {}, new Object[] {});
      }
      else
      {
          result = (byte[])invoke("com.sun.corba.se.impl.encoding.CDROutputStream", out, "toByteArray", new Class<?>[] {}, new Object[] {});

      }

      Properties properties = new Properties();
      properties.put("jacorb.interop.sun", "on");
      ORB jacorborb = org.omg.CORBA.ORB.init (new String[]{}, properties);
      CDRInputStream in = new CDRInputStream(jacorborb, result);


      NegativeArgumentException n = (NegativeArgumentException)in.read_value();

      System.out.println("### testException::value i is : " + n.i + " and the static value j is : " + n.j+" and message " + n.getMessage());

      foreignorb.shutdown(true);
      jacorborb.shutdown(true);

      assertTrue (n.i == -10);
   }
}
