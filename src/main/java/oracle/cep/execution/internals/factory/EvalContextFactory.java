/* $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/factory/EvalContextFactory.java /main/4 2008/10/24 15:50:19 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      05/05/08 - remove FullSpillMode
    hopark      03/08/08 - use getFullSpillMode
    hopark      09/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/factory/EvalContextFactory.java /main/4 2008/10/24 15:50:19 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals.factory;

import oracle.cep.execution.internals.IEvalContext;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.service.ExecContext;

public class EvalContextFactory
{
  public static IEvalContext create(ExecContext ec)
  {
    IEvictPolicy evPolicy = ec.getServiceManager().getEvictPolicy();
    if (evPolicy == null || !evPolicy.isFullSpill())
    {
      return new oracle.cep.execution.internals.memory.EvalContext();
    } else{
      return new oracle.cep.execution.internals.stored.EvalContext();
    }
  }  
}
