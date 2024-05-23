/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/CEPCeil.java /main/1 2013/03/04 00:54:59 sbishnoi Exp $ */

/* Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/25/13 - Creation
 */
package oracle.cep.extensibility.functions.builtin;
  
import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import oracle.cep.exceptions.UDFError;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/CEPCeil.java /main/1 2013/03/04 00:54:59 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPCeil implements SingleElementFunction 
{
    
  public Object execute(Object[] args) throws UDFException 
  {
    double retVal;
    if ((args[0] == null)) 
      return null;
  
    double val1 = ((Double)args[0]).doubleValue();
    
    try 
    {
      retVal = java.lang.Math.ceil(val1);
    }
    catch(Exception e) 
    {
      throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, 
        "ceil1");
    }
    return retVal;
  }
}  
