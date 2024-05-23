/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/ToBigDecimal.java /main/1 2010/09/28 03:41:33 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/24/10 - Creation
 */

package oracle.cep.extensibility.functions.builtin;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/builtin/ToBigDecimal.java /main/1 2010/09/28 03:41:33 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

import oracle.cep.extensibility.functions.SingleElementFunction;
import oracle.cep.extensibility.functions.UDFException;
import java.math.BigDecimal;

public class ToBigDecimal implements SingleElementFunction
{
  public Object execute(Object[] args) throws UDFException
  {
    assert (args.length == 1) : args.length;
    
    if(args[0] == null)
      return null;
    
    if(!(args[0] instanceof BigDecimal))
      return null;
    // ?? Should we create a new BigDecimal object 
    return args[0];
  }
}
