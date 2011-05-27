package test.nio.timeout;

import java.lang.*;
import java.util.concurrent.atomic.*;

public class GoodDayImpl
  extends GoodDayPOA
{
  private String location;
  private AtomicInteger requestCount = new AtomicInteger (1);

  public GoodDayImpl( String location )
  {
    this.location = location;
  }

  public String hello_simple (int id, String data)
  {
    System.out.print (" <" + requestCount.getAndIncrement() + "," + id + "," + data.length() + ">");

    try {
      Thread.currentThread().sleep (500);
    } catch (InterruptedException e) {
      System.out.println (e);
    }

    return "Hello World, from " + location;
  }
}
