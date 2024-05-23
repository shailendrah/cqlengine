/* $Header: Lpad.java 07-feb-2008.04:38:12 sbishnoi Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/23/08 - Creation
 */

package oracle.cep.extensibility.functions.builtin;

/**
 *  @version $Header: Lpad.java 07-feb-2008.04:38:12 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

import java.text.StringCharacterIterator;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class Lpad implements SingleElementFunction
{
  /*
   * LPAD(expr1, n, expr2)
   * LPAD returns expr1, left-padded to length n characters with the sequence
   * of characters in expr2    
   */
  public Object execute(Object[] args) throws UDFException
  {
    assert (args.length == 3) : args.length;
    
    if(args[0] == null || args[1] == null || args[2] == null)
      return null;
    
    String expr1 = (String)args[0];
    int    n     = (Integer)args[1];
    String expr2 = (String)args[2];
    
    StringCharacterIterator iter   = new StringCharacterIterator(expr2);
    StringBuffer            outStr = new StringBuffer(n);
    
    int len1  = expr1.length();
    int len2  = expr2.length();
    
    if(len1 == 0 || len2 == 0 || n < 1)
      return null;
    
    if(n <= len1)
      return expr1.substring(0, n);
    else
    {
      int numPadTotal  = n - len1;
      int numPadDone;
      
      int numIters = numPadTotal/len2;
      for(int i = 0; i < numIters; i++)
        outStr = outStr.append(expr2);
     
      numPadDone = outStr.length();
      char chPad = iter.first();
      
      numIters = numPadTotal - numPadDone;
      for(int i = 0; i < numIters; i++)
      {
        outStr = outStr.append(chPad);
        chPad = iter.next();
      }
      outStr = outStr.append(expr1);
    }
    return outStr.toString();
  }
}