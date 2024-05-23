/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptDistinctFactory.java /main/3 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/09/08 - remove statics
    hopark      07/13/07 - dump stack trace on exception
    sbishnoi    05/11/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptDistinctFactory.java /main/3 2008/10/24 15:50:17 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import java.util.logging.Level;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.exceptions.CEPException;
import oracle.cep.phyplan.PhyOptDistinct;
import oracle.cep.service.ExecContext;


public class PhyOptDistinctFactory extends PhyOptFactory {
  
  public PhyOptDistinctFactory(){
    super();
  }
  
  public PhyOpt newPhyOpt(Object ctx){
    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext lpctx =
  (LogPlanInterpreterFactoryContext) ctx;

    PhyOpt   op;
    PhyOpt[] phyChildren;
    PhyOpt   phyChild;
    
    ExecContext ec = lpctx.getExecContext();
    phyChildren = lpctx.getPhyChildPlans();
    phyChild    = phyChildren[0];
    try {
      op = new PhyOptDistinct(ec, phyChild);
    } catch (CEPException ex) {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      op = null;
    }

    return op;
  }
}