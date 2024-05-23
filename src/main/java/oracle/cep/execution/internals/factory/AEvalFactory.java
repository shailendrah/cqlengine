/* $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/factory/AEvalFactory.java /main/4 2008/10/24 15:50:15 hopark Exp $ */

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
    hopark      10/07/08 - use execContext to remove statics
    hopark      05/05/08 - remove FullSpillMode
    hopark      03/26/08 - server reorg
    hopark      03/08/08 - use getFullSpillMode
    hopark      09/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/factory/AEvalFactory.java /main/4 2008/10/24 15:50:15 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals.factory;

import oracle.cep.execution.internals.memory.AEval;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;

public class AEvalFactory
{
  public static AEval create(ExecContext ec)
  {
    CEPManager cepMgr = ec.getServiceManager();
    IEvictPolicy evPolicy = cepMgr.getEvictPolicy();
    if (evPolicy == null || !evPolicy.isFullSpill())
    {
      return new oracle.cep.execution.internals.memory.AEval(ec);
    } else{
      return new oracle.cep.execution.internals.stored.AEval(ec);
    }
  }
}
  
