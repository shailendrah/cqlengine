/* $Header: TkUsrFoo32.java 13-may-2008.09:02:22 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

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
    parujain    04/16/07 - throw UDFException
    skmishra    12/09/06 - 
    dlenkov     11/14/06 - Creation
 */

/**
 *  @version $Header: TkUsrFoo32.java 13-may-2008.09:02:22 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrFoo32 implements SingleElementFunction {

  public Object execute( Object[] args)throws UDFException {
    float f1 = ((Float)args[0]).floatValue();
    float f2 = ((Float)args[1]).floatValue();
    float f3 = ((Float)args[2]).floatValue();
    float sum = f1 + f2 + f3;

    System.out.println( "Foo32: sum = " + sum);

    return new Float( sum);
  }
}
