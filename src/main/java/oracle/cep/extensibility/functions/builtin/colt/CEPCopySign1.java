/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPCopySign1.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPCopySign1.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

  package oracle.cep.extensibility.functions.builtin.colt;
 
  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;

  import java.lang.Math;

  public class CEPCopySign1 implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      float retVal;
      if ((args[0] == null)||(args[1] == null)) return null;
      float val1 = ((Float)args[0]).floatValue();
      float val2 = ((Float)args[1]).floatValue();
      try {
        retVal = java.lang.Math.copySign(val1,val2);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPCopySign1");
      }
      return retVal;
    }
  }
  
