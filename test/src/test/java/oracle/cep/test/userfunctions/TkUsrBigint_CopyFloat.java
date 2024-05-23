/* $Header: TkUsrBigint_CopyFloat.java 13-may-2008.09:01:46 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    User Defined Aggregation for Bigint datatype testing

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      05/13/08 - 
    udeshmuk    10/18/07 - Rewrite to make use of generic handlePlus and
                           handleMinus functions.
    mthatte     10/16/07 - 
    parujain    04/16/07 - throw UDFException
    rkomurav    01/05/07 - null uDA
    hopark      11/27/06 - Creation
 */

/**
 *  @version $Header: TkUsrBigint_CopyFloat.java 13-may-2008.09:01:46 hopark Exp $
 *  @author  anasrini
 *  @since   1.0
 */
package oracle.cep.test.userfunctions;

import oracle.cep.extensibility.functions.AggrBigInt;
import oracle.cep.extensibility.functions.AggrFloat;
import oracle.cep.extensibility.functions.AggrInteger;
import oracle.cep.extensibility.functions.AggrValue;
import oracle.cep.extensibility.functions.IAggrFnFactory;
import oracle.cep.extensibility.functions.IAggrFunction;
import oracle.cep.extensibility.functions.AggrFunctionImpl;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDAException;
import oracle.cep.extensibility.functions.UDFException;

public class TkUsrBigint_CopyFloat implements SingleElementFunction {
  public Object execute( Object[] args) throws UDFException{
    float fval = ((Float)args[0]).floatValue();
    return new Float(fval);
  }
}

