/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPBinomial2.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $ */

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
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/colt/CEPBinomial2.java /main/1 2014/02/24 18:16:19 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

  package oracle.cep.extensibility.functions.builtin.colt;
  
  import oracle.cep.extensibility.functions.SingleElementFunction;
  import oracle.cep.extensibility.functions.UDFException;
  import oracle.cep.exceptions.UDFError;

  import cern.jet.stat.Probability;

  public class CEPBinomial2 implements SingleElementFunction {
    
    public Object execute(Object[] args) throws UDFException {
      double retVal;
      if ((args[0] == null)||(args[1] == null)||(args[2] == null)) return null;
      int val1 = ((Integer)args[0]).intValue();
      int val2 = ((Integer)args[1]).intValue();
      double val3 = ((Double)args[2]).doubleValue();
      try {
        retVal = cern.jet.stat.Probability.binomial(val1,val2,val3);
      }
      catch(Exception e) {
        throw new UDFException(UDFError.USERDEFINED_FUNCTION_RUNTIME_ERROR, "CEPBinomial2");
      }
      return retVal;
    }
  }
  