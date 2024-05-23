/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/ISimpleFunction.java /main/1 2009/12/02 02:35:19 alealves Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      alealves  11/27/09 - Data cartridge context, default package support
    alealves    Nov 22, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/extensibility/functions/ISimpleFunction.java /main/1 2009/12/02 02:35:19 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.extensibility.functions;

import oracle.cep.extensibility.cartridge.ICartridgeContext;
import oracle.cep.extensibility.cartridge.IRuntimeInvocable;
import oracle.cep.extensibility.cartridge.RuntimeInvocationException;

public interface ISimpleFunction extends IRuntimeInvocable, UserDefinedFunction
{
  /**
   * Execute runtime function implementation for simple functions (e.g. non aggregate functions).
   * 
   * @param args
   * @param context
   * @return
   * @throws RuntimeInvocationException
   */
  public Object execute(Object[] args, ICartridgeContext context) 
    throws RuntimeInvocationException;
}
