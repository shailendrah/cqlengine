/* $Header: TkUsrDivInt.java 13-may-2008.09:02:03 hopark Exp $ */

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
    najain      05/18/07 - 
    dlenkov     04/02/07 - Creation
 */

/**
 *  @version $Header: TkUsrDivInt.java 13-may-2008.09:02:03 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;

public class TkUsrDivInt implements SingleElementFunction {

  public Object execute(Object[] args) {
    int res = 0;
    try {
      int arg0 = ((Integer)args[0]).intValue();
      int arg1 = ((Integer)args[1]).intValue();
      res = arg0/arg1;
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return new Integer(res);    
  }
}
