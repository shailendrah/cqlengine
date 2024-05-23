/* $Header: TkUsrBinomial.java 13-may-2008.09:01:58 hopark Exp $ */

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
    sbishnoi    02/11/08 - error parameterization
    sbishnoi    02/05/08 - adding parameters to UDFError
    mthatte     10/16/07 - 
    anasrini    06/19/07 - 
    sbishnoi    06/14/07 - Creation
 */

/**
 *  @version $Header: TkUsrBinomial.java 13-may-2008.09:01:58 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.test.userfunctions;

import cern.jet.math.Arithmetic;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

public class TkUsrBinomial implements SingleElementFunction {
  
  public Object execute(Object[] args) throws UDFException {
    double dval = 0.0d;
    long n      = ((Long)args[0]).longValue();
    long k      = ((Long)args[1]).longValue();

    try {
      dval = Arithmetic.binomial(n, k);
    }
    catch(Exception e) {
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
                             "TkUsrBinomial");
    }

    Double dtemp = new Double(dval);
    return new Float(dtemp.floatValue());
  }
}
