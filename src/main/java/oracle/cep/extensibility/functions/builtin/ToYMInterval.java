/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/ToYMInterval.java /main/2 2013/10/16 07:04:05 sbishnoi Exp $ */

/* Copyright (c) 2011, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/15/13 - adding parameters in the error
    sbishnoi    09/05/11 - Creation
 */
package oracle.cep.extensibility.functions.builtin;

import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.UDFError;
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.SoftUDFException;
import oracle.cep.extensibility.functions.UDFException;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/ToYMInterval.java /main/2 2013/10/16 07:04:05 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class ToYMInterval implements SingleElementFunction
{

  @Override
  public Object execute(Object[] args) throws UDFException
  {
    if(args[0] == null)
      return null;
    
    String inputData = (String) args[0];
    IntervalFormat format = null;
    Long numMonths = null;
    try
    {
      format 
        = new IntervalFormat(TimeUnit.YEAR, TimeUnit.MONTH, 9, true);
    } 
    catch (CEPException e)
    {
      // Unreachable as we are creating interval format with valid parameters
      assert false;
    }
    
    try
    {
      numMonths = IntervalConverter.parseYToMIntervalString(inputData, format);
    } 
    catch (CEPException e)
    {      
      throw new SoftUDFException(
        UDFError.ILLEGAL_ARGUMENT_FOR_FUNCTION_PARAMETRIZED,
        e,
        inputData,
        "ToYMInterval()");
    }
    
    return numMonths;
  }
  
}