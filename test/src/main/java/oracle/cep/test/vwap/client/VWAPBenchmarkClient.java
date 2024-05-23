/* $Header: VWAPBenchmarkClient.java 19-may-2008.02:31:13 udeshmuk Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/19/08 - 
    sbishnoi    05/16/08 - testing
    sbishnoi    05/12/08 - Creation
 */

/**
 *  @version $Header: VWAPBenchmarkClient.java 19-may-2008.02:31:13 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.vwap.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import net.esper.example.benchmark.Symbols;
import net.esper.example.benchmark.MarketData;

public class VWAPBenchmarkClient {

  private static String              SERVER_NAME;
  private static int                 SERVER_PORT;
  private static int                 RATE;
  private static int                 RUN_TIME_IN_MILLIS;

  private static Socket              vwapSocket;
  private static ObjectOutputStream  out;
  private static MarketData          market[];

  static {
    SERVER_NAME = "stbda16.us.oracle.com";
    SERVER_PORT = 4444;
    RATE        = 100000;
    RUN_TIME_IN_MILLIS = 5000;
  }
 
  public static void main(String args[]) throws IOException
  {

    // Set Parameter Values from Command line arguments OR set them default
    try
    {
      for(int i =0; i < args.length ; i++)
      {
        if("-rate".equalsIgnoreCase(args[i])){
          i++;
          RATE = Integer.parseInt(args[i]);
        }
        else if("-runTime".equalsIgnoreCase(args[i])){
          i++;
          RUN_TIME_IN_MILLIS = Integer.parseInt(args[i]);
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }


    // Establish Connection
    establishConnection();
    System.out.println("Progress: Client's Connection Status: Conneciton Established with Server "
                        + SERVER_NAME + " at port # " + SERVER_PORT);

    // Initialize Market Data
    initializeMarketData();
    System.out.println("Progress: Market Data Initialized");

    // Generate Events at a rate %RATE% for time %RUN_TIME_IN_MILLIS%
    long startTs    = System.currentTimeMillis();
    long currentTs  = System.currentTimeMillis();
    int  iterCount = 1;
    long sleepTime = 0l;
    long tupleCount = 0l;
    int eventPer50ms = RATE / 20;
    System.out.println("Progress: Data Injection Started..");
    try
    { 
      while(System.currentTimeMillis() - startTs < RUN_TIME_IN_MILLIS)
      {
        for(int i = 0; i < eventPer50ms; i++)
        {
          out.write(getTuple().array());
          tupleCount++;
        }
        currentTs = System.currentTimeMillis();
        sleepTime = startTs + (50*iterCount) - currentTs;
        if(sleepTime > 0)
          Thread.sleep(sleepTime);
        iterCount++;
      } 
    }
    catch(InterruptedException ex){
      System.out.println("Thread Interrupted");
      ex.printStackTrace();
    } 
    finally{
      out.close();
      vwapSocket.close();
    }
    System.out.println("Progress: Data Injection Completed.\n"+ "Progress: " +
                      tupleCount  + " tuples sent to CEP Server " +" in time "+
                        RUN_TIME_IN_MILLIS + " with rate " + RATE);

    System.out.println("Data Injection Client Program Ended");
  }

  private static void establishConnection()
  {
    try 
    {
      vwapSocket = new Socket(SERVER_NAME, SERVER_PORT);
      out = new ObjectOutputStream(vwapSocket.getOutputStream());
    }
    catch (UnknownHostException e) {
      System.err.println("Don't know about host:" + SERVER_NAME);
      System.exit(1);
    } 
    catch (IOException e) {
      System.err.println("Couldn't get I/O for "
                    + "the connection to:" + SERVER_NAME);
      System.exit(1);
    }
  }

  private static void initializeMarketData()
  {
    market = new MarketData[Symbols.SYMBOLS.length];

    for (int i = 0; i < market.length; i++)
      market[i] = new MarketData(Symbols.SYMBOLS[i], Symbols.nextPrice(10), Symbols.nextVolume(10));

    // set market data
    for (int i = 0; i < market.length; i++)
    {
      MarketData md = market[i];
      md.setPrice(Symbols.nextPrice(md.getPrice()));
      md.setVolume(Symbols.nextVolume(10));
    }  
  } 

  private static ByteBuffer getTuple()
  {
    int tickerIndex = Symbols.nextVolume(100000) % market.length;
    MarketData md = market[tickerIndex];
    md.setPrice(Symbols.nextPrice(md.getPrice()));
    md.setVolume(Symbols.nextVolume(10));
    ByteBuffer b = ByteBuffer.allocate(28);

    /*if(tupleCount == 0)
     System.out.println("Inp Data:" + md.getTicker() + "; " + md.getPrice() + "; "+ md.getVolume());*/

    md.toByteBuffer(b);
    return b;
  }

/*  static class Market implements Serializable {
    private static final long serialVersionUID = 123123L;
    char[] ticker;
    int     volume;
    float   price;
    
    public Market() {
     ticker = new char[]{'t', 'e', 's', 't'};
     volume = 5;
     price = 5.9f;
    }
  }*/
}
