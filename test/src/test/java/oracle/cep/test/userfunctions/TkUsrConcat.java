/* $Header: TkUsrConcat.java 13-may-2008.09:02:02 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    User Defined Function - String Concatenation

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
 *  @version $Header: TkUsrConcat.java 13-may-2008.09:02:02 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrConcat implements SingleElementFunction {

  public Object execute(Object[] args) throws UDFException {
    String arg1 = (String)args[0];
    String arg2 = (String)args[1];
    
    StringBuilder sb = new StringBuilder();
    sb.append(arg1);
    sb.append(arg2);

    return sb.toString();
  }
}
