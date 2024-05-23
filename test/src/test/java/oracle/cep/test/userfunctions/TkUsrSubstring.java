/* $Header: TkUsrSubstring.java 13-may-2008.09:02:38 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    User Defined Function - substring

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/13/08 - 
    mthatte     10/16/07 - 
    parujain    04/16/07 - throw UDFException
    skmishra    12/09/06 - 
    anasrini    06/20/06 - Creation
    anasrini    06/20/06 - Creation
    anasrini    06/20/06 - Creation
 */

/**
 *  @version $Header: TkUsrSubstring.java 13-may-2008.09:02:38 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrSubstring implements SingleElementFunction {

  public Object execute(Object[] args) throws UDFException{
    String arg1  = (String)args[0];
    int    start = ((Integer)args[1]).intValue();
    int    len   = ((Integer)args[2]).intValue();
    int    end;

    if (start <= 0) 
      start = 1;
    else if (start > arg1.length())
      start = arg1.length();

    start--;
    end = start + len;

    if (end > arg1.length())
      end = arg1.length();
    
    return arg1.substring(start, end);
  }
}

