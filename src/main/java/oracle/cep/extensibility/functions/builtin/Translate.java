/* $Header: Translate.java 07-feb-2008.05:29:03 sbishnoi Exp $ */

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
 *  @version $Header: Translate.java 07-feb-2008.05:29:03 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
import java.util.LinkedHashMap;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class Translate implements SingleElementFunction
{
  public Object execute(Object[] args) throws UDFException
  {
    assert (args.length == 3) : args.length;
    
    if(args[0] == null || args[1] == null || args[2] == null)
      return null;
    
    String expr       = (String)args[0];
    String fromString = (String)args[1];
    String toString   = (String)args[2];
    
    StringBuffer buffer = new StringBuffer();
   
    int exprLength       = expr.length();
    int fromStringLength = fromString.length();
    int toStringLength   = toString.length();
   
    if(exprLength == 0 || fromStringLength == 0 || toStringLength ==0)
      return null;
    
    // Creates a Hash Map
    LinkedHashMap<Character, Character> map = 
      new LinkedHashMap<Character,Character>();
    
    for(int i = fromStringLength; i > 0; i--)
    {
      if(i <= toStringLength)
        map.put(fromString.charAt(i-1), toString.charAt(i-1));
      else
        map.put(fromString.charAt(i-1), null);
    }
    
    char currentChar;
    Character newChar;
    for(int i =0 ; i < exprLength; i++)
    {
      currentChar = expr.charAt(i);
      if(map.containsKey(currentChar))
      {
        if((newChar = map.get(currentChar))!= null)
          buffer.append(newChar);
      }
      else
        buffer.append(currentChar);
    }
    return buffer.toString();
  }
}