/* $Header: TkUsrToInt.java 13-may-2008.09:02:40 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/13/08 - 
    mthatte     10/16/07 - 
    anasrini    07/15/07 - 
    najain      06/12/07 - Creation
 */

/**
 *  @version $Header: TkUsrToInt.java 13-may-2008.09:02:40 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;

public class TkUsrToInt implements SingleElementFunction {

  public Object execute(Object[] args) {
    int res = 0;
    try {
      res = (int)(((Float)args[0]).floatValue());
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return new Integer(res);    
  }
}
