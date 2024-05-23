package oracle.cep.test.pattern;
/* $Header: PatternGenericGenTest.java 27-mar-2008.06:29:58 rkomurav Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    03/27/08 - Creation
 */

/**
 *  @version $Header: PatternGenericGenTest.java 27-mar-2008.06:29:58 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class PatternGenericGenTest
{
  
  
  public static void main(String[] args) throws FileNotFoundException
  {
    int    j;
    int    numInputs;
    int    numSymbols;
    int    maxValue;
    double interval;
    String inpFile;
    
    if(args.length != 5)
    {
      usage();
      return;
    }
    
    j = 0;
    
    numInputs  = Integer.parseInt(args[j++]);
    interval   = ((double)1000)/Integer.parseInt(args[j++]);
    inpFile    = args[j++];
    numSymbols = Integer.parseInt(args[j++]);
    maxValue   = Integer.parseInt(args[j++]);
    
    assert numInputs  > 1 : numInputs;
    assert interval   > 0 : interval;
    assert inpFile    != null;
    assert numSymbols > 0;
    assert maxValue   > 0;
    
    Random      randomSymbol = new Random();
    Random      randomValue  = new Random();
    double      prevTs       = 0;
    File        f1           = new File(inpFile);
    PrintWriter pw           = new PrintWriter(f1);
    
    long currTs = 0;
    int  value;
    int  symbol;
    
    //index count, value, symbol
    pw.println("i, i, i");
    
    for(int i = 0; i < numInputs; i++)
    {
      prevTs = prevTs + interval;
      currTs = (long)Math.ceil(prevTs);
      value = randomValue.nextInt(maxValue);
      symbol = randomSymbol.nextInt(numSymbols);
      pw.println(currTs + " " + (i+1) + "," + value + "," + symbol);
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
    System.out.println("3 - Number of symbols in the generated output");
    System.out.println("4 - Maximum value of any entry");
  }
}

