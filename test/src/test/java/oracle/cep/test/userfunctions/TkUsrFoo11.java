/* $Header: TkUsrFoo11.java 13-may-2008.09:02:13 hopark Exp $ */

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
 *  @version $Header: TkUsrFoo11.java 13-may-2008.09:02:13 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrFoo11 implements SingleElementFunction {

  public Object execute( Object[] args) throws UDFException {
    int i1 = ((Integer)args[0]).intValue();
    int i2 = ((Integer)args[1]).intValue();
    float f3 = ((Float)args[2]).floatValue();
    float sum = i1 + i2 + f3;

    System.out.println( "Foo11: sum = " + sum);

    return new Float( sum);
  }
}
