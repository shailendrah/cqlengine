/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptTableFunctionRelSrcFactory.java /main/2 2010/03/20 08:53:21 sbishnoi Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    12/26/09 - Creation
 */

package oracle.cep.phyplan.factory;

import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.LogOptTableFunctionRelSource;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptTableFunctionRelnSrc;
import oracle.cep.service.ExecContext;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptTableFunctionRelSrcFactory.java /main/2 2010/03/20 08:53:21 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class PhyOptTableFunctionRelSrcFactory extends PhyOptFactory
{
  
  PhyOpt newPhyOpt(Object ctx) throws CEPException
  {
    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext lpctx =
      (LogPlanInterpreterFactoryContext) ctx;
    
    ExecContext ec = lpctx.getExecContext();
    
    PhyOptTableFunctionRelnSrc op = null;
    
    try 
    {
      op = new PhyOptTableFunctionRelnSrc(ec, lpctx.getLogPlan());
    } 
    catch (CEPException ex) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      op = null;
    }

    return op;
  }
  
  PhyOpt getNewPhyOpt(Object ctx) throws CEPException
  {
    PhyOpt op = newPhyOpt(ctx);
    return op;
  }

}