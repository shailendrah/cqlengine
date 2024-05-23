/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPLog1.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPLog1.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.functions.builtin.colt;

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.SoftUDFException;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;


/**
 * This function implements the logic for calculating the natural logarithm of a
 * number.
 * 
 * Underlying implementation uses {@code java.lang.Math.log}. Some minor changes
 * have been incorporated to take into account the difference between Oracle's
 * and Java's implementation for corner cases(e.g null and non-positive inputs).
 * 
 * @author subhrcho
 *
 */
public class CEPLog1 implements SingleElementFunction
{
  public static final String FUNC_NAME = "ln";

  public Object execute(Object[] args) throws UDFException
  {
    double retVal;
    if ((args[0] == null))
      return null;
    double input = ((Double) args[0]).doubleValue();
    // For non positive inputs Java returns NaN, but Oracle throws Exception
    if (Math.signum(input) != 1.0)
      throw new SoftUDFException(UDFError.ILLEGAL_ARGUMENT_FUNCTION_LN, input,
          FUNC_NAME);
    try
    {
      retVal = java.lang.Math.log(input);
    } catch (Exception e)
    {
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR,
          "CEPLog1");
    }
    return retVal;
  }
}
