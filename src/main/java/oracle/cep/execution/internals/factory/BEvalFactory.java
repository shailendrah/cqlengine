/* $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/factory/BEvalFactory.java /main/5 2009/05/22 07:27:48 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      05/21/09 - remove max_instrs limit
    hopark      10/10/08 - remove statics
    hopark      05/05/08 - remove FullSpillMode
    hopark      03/08/08 - use getFullSpillMode
    hopark      09/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/factory/BEvalFactory.java /main/5 2009/05/22 07:27:48 sborah Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.internals.factory;

import oracle.cep.execution.internals.IBEval;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;

public class BEvalFactory
{
  public static IBEval create(ExecContext ec)
  {
    CEPManager cepMgr = ec.getServiceManager();
    IEvictPolicy evPolicy = cepMgr.getEvictPolicy();
    if (evPolicy == null || !evPolicy.isFullSpill())
    {
      return new oracle.cep.execution.internals.memory.BEval();
    } else{
      return new oracle.cep.execution.internals.stored.BEval();
    }
  }  
  
  public static IBEval create(ExecContext ec, int max)
  {
    CEPManager cepMgr = ec.getServiceManager();
    IEvictPolicy evPolicy = cepMgr.getEvictPolicy();
    
    // skip usage of max variable. 
    // TODO : remove in next release
    if (evPolicy == null || !evPolicy.isFullSpill())
    {
      return new oracle.cep.execution.internals.memory.BEval();
    } 
    else
    {
      return new oracle.cep.execution.internals.stored.BEval();
    }
  } 
  
}
  
