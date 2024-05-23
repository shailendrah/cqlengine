/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPSignum.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $ */

/* Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    02/14/14 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPSignum.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.functions.builtin.math;

import java.math.BigDecimal;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

/**
 * This function is an implementation class for the Oracle CQL function "SIGNUM":
 * SIGNUM(n) returns the sign of n. 
 * This function takes as an argument any numeric data type and returns DOUBLE.
 *
 * For value of any NUMERIC type, the sign is:
 * -1 if n<0
 * 0 if n=0
 * 1 if n>0
 * 
 * Following are the valid signatures for signum function:
 * signum(int)
 * signum(float)
 * signum(bigint)
 * signum(double)
 * signum(number)
 * 
 * @author sbishnoi
 *
 */
public class Signum implements SingleElementFunction
{

  public Object execute(Object[] args) throws UDFException
  {
    if ((args[0] == null))
      return null;
    
    if(args[0] instanceof Integer)
    {
      int val1 = ((Integer) args[0]).intValue();
      return Integer.signum(val1);
    }
    else if (args[0] instanceof Float)
    {
      float val1 = ((Float) args[0]).floatValue();
      return new Float(java.lang.Math.signum(val1)).intValue();
    }
    else if(args[0] instanceof Long)
    {
      Long val1 = ((Long) args[0]).longValue();
      return Long.signum(val1);
    }
    else if(args[0] instanceof Double)
    {
      double val1 = ((Double) args[0]).doubleValue();
      return java.lang.Math.signum(val1);
    }
    else if(args[0] instanceof BigDecimal)
    {
      BigDecimal val1 = (BigDecimal)args[0];
      return val1.signum();
    }
    else
    {
      throw new UDFException(UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION_PARAMETRIZED,
          args[0], "signum");
    }
  }
}
