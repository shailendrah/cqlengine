/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPNextAfter.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPNextAfter.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
  package oracle.cep.extensibility.functions.builtin.colt;

  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;

  import java.lang.Math;

  public class CEPNextAfter implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      double retVal;
      if ((args[0] == null)||(args[1] == null)) return null;
      double val1 = ((Double)args[0]).doubleValue();
      double val2 = ((Double)args[1]).doubleValue();
      try {
        retVal = java.lang.Math.nextAfter(val1,val2);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPNextAfter");
      }
      return retVal;
    }
  }
  
