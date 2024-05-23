/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptUnionFactory.java /main/3 2008/10/24 15:50:17 hopark Exp $ */

/* Copyright (c) 2006, 2008, Oracle and/or its affiliates. All rights reserved.*/

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
    dlenkov     06/12/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/factory/PhyOptUnionFactory.java /main/3 2008/10/24 15:50:17 hopark Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan.factory;

import java.util.logging.Level;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.exceptions.CEPException;
import oracle.cep.phyplan.PhyOptUnion;
import oracle.cep.service.ExecContext;

/**
 * PhyOptUnionFactory
 *
 * @author dlenkov
 */
public class PhyOptUnionFactory extends PhyOptFactory {

  /**
   * Constructor for PhyOptUnionFactory
   */
  public PhyOptUnionFactory() {
    super();
  }

  public PhyOpt newPhyOpt( Object ctx) {

    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext lpctx =
	(LogPlanInterpreterFactoryContext) ctx;
    ExecContext ec = lpctx.getExecContext();

    PhyOpt op;
    try 
    {
      op = new PhyOptUnion( ec, lpctx.getLogPlan(), lpctx.getPhyChildPlans());
    } 
    catch (CEPException ex) 
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ex);
      op = null;
    }

    return op;
  }

}
