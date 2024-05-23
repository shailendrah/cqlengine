package oracle.cep.test.pattern;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

/* $Header: PatternGenTest.java 21-mar-2007.23:10:24 rkomurav Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    03/21/07 - Creation
 */

/**
 *  @version $Header: PatternGenTest.java 21-mar-2007.23:10:24 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

public class PatternGenTest
{
  private static int    numInputs;
  private static double interval;
  private static String inpFile;
  
  public static void main(String[] args) throws FileNotFoundException
  {
    int delay = 10;
    int j = 0;
    if(args.length != 3)
    {
      usage();
      return;
    }
    
    numInputs = Integer.parseInt(args[j++]);
    interval  = ((double)1000)/Integer.parseInt(args[j++]);
    inpFile   = args[j++];
    
    assert numInputs > 1 : numInputs;
    assert interval > 0 : interval;
    assert inpFile != null;
    
    Random random  = new Random();
    double prevTs  = 0;
    File f1        = new File(inpFile);
    PrintWriter pw = new PrintWriter(f1);
    
    long currTs = 0;
    int randDelay;
    
    pw.println("i, i");
    
    for(int i = 0; i < numInputs; i++)
    {
      prevTs = prevTs + interval;
      currTs = (long)Math.ceil(prevTs);
      randDelay = random.nextInt(delay);
      pw.println(currTs + " " + (i+1) + "," + randDelay);
    }
    pw.println("h " + 2*currTs);
    pw.close();
  }
  
  private static void usage()
  {
    System.out.println("");
    System.out.println("The arguments list is as follows:");
    System.out.println("0 - Number of inputs to be generated");
    System.out.println("1 - Number of inputs per second");
    System.out.println("2 - Input File location");
  }
}

