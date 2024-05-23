/* $Header: SubStr.java 07-feb-2008.05:30:16 sbishnoi Exp $ */

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
 *  @version $Header: SubStr.java 07-feb-2008.05:30:16 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class SubStr implements SingleElementFunction
{
  public Object execute(Object[] args) throws UDFException
  {
    assert (args.length == 3) : args.length;
    
    if(args[0] == null || args[1] == null || args[2] == null)
      return null;
    
    String inputStr = (String)args[0];
    int pos         = (Integer)args[1];
    int length      = (Integer)args[2];
    
    int inputLength = inputStr.length();
    int beginIndex;
    int endIndex;
    
    if(pos == 0)
      beginIndex = pos;
    else if(pos > 0 && pos <= inputLength)
      beginIndex = pos -1;
    else if(pos > 0 && pos > inputLength)
      return null;
    else
      beginIndex = inputLength + pos;
        
    if((beginIndex + length) > inputLength)
      endIndex = inputLength ;
    else if(length < 1)
      return null;
    else
      endIndex = length + beginIndex;
    
    return inputStr.substring(beginIndex, endIndex);
  }
}
