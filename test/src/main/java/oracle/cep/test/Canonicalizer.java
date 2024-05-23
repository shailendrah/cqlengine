/* $Header: pcbpel/cep/test/src/oracle/cep/test/Canonicalizer.java /main/1 2009/02/14 09:41:51 anasrini Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates.All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    02/11/09 - Regress relation canonicalizer
    anasrini    02/11/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/oracle/cep/test/Canonicalizer.java /main/1 2009/02/14 09:41:51 anasrini Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Collection;
import java.util.Arrays;


public class Canonicalizer
{
  private static String TIME_SPLITTER    = ":";
  private static String PLUS             = "+";
  private static String MINUS            = "-";

  private static class MapValue 
  {
    String line;
    int    count;
    
    MapValue(String line, int count)
    {
      this.line  = line;
      this.count = count;
    }
  }


  public static void main(String[] args)
  {
    try 
    {

      String         inFileName;
      File           inFile;
      FileReader     inFileReader;
      BufferedReader inLineReader;

      inFileName   = args[0];
      inFile       = new File(inFileName);
      inFileReader = new FileReader(inFile);
      inLineReader = new BufferedReader(inFileReader);

      String         outFileName;
      File           outFile;
      PrintWriter    outFileWriter;

      outFileName   = args[1];
      outFile       = new File(outFileName);
      outFileWriter = new PrintWriter(outFile);

      /*
        System.out.println("Processing ... " + inFile.getAbsolutePath() + " ... "
        + "and ... " + outFile.getAbsolutePath());
      */

      processFile(inLineReader, outFileWriter);

      outFileWriter.close();
      inLineReader.close();

    }
    catch(Exception e)
    {
    }
  }


  private static void processFile(BufferedReader inLineReader,
                                  PrintWriter outFileWriter) 
    throws Exception
  {
    String prevTime = new String("-1");
    String inputLine = inLineReader.readLine();
    String time;
    String sign;
    String rest;
    int    timeLength;

    HashMap<String, MapValue> map = new HashMap<String, MapValue>();

    while (inputLine != null)
    {
      time = (inputLine.split(TIME_SPLITTER))[0];
      timeLength = time.length();

      if (timeLength > 0)
      {
        sign = inputLine.substring(timeLength+1, timeLength+2);
        rest = inputLine.substring(timeLength+2);
        
        /*
        System.out.println("time= " + time + " ; sign= " + sign + 
                           " ; rest= " + rest);
        */

        if (!(prevTime.equals(time)))
        {
          prevTime = new String(time);
          flushMap(map, outFileWriter);
        }
        updateMap(map, sign, rest, inputLine);

      }
      inputLine = inLineReader.readLine();
    }
    flushMap(map, outFileWriter);
  }


  private static void flushMap(HashMap<String, MapValue> map, 
                               PrintWriter outFileWriter) 
    throws Exception
  {
    // Get the values in the map
    Collection<MapValue> values   = map.values();
    MapValue[]           valArray = values.toArray(new MapValue[0]);
    String[]             lines    = new String[valArray.length];

    for (int i=0; i < valArray.length; i++)
      lines[i] = valArray[i].line;

    Arrays.sort(lines);
    for (int i=0; i < lines.length; i++)
    {
      //      System.out.println(lines[i]);
      outFileWriter.println(lines[i]);
    }

    map.clear();
  }

  private static void updateMap(HashMap<String, MapValue> map, String sign, 
                                String rest, String inputLine) 
    throws Exception 
  {
    int count = 0;

    if (sign.equals(PLUS))
      count = 1;
    else if (sign.equals(MINUS))
      count = -1;
    else
      throw new Exception("Invalid sign : " + inputLine);

    MapValue v = map.get(rest);
    if (v == null)
    {
      v = new MapValue(inputLine, count);
      map.put(rest, v);
    }
    else 
    {
      v.count = v.count + count;
      if (v.count == 0)
        map.remove(rest);
    }
  }

}
