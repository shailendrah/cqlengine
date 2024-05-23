/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptRelnSrcFactory.java /main/9 2010/01/25 00:32:43 sbishnoi Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 parujain    05/09/08 - fix viewrelnsrc drop
 mthatte     10/24/07 - adding isOnDemand
 sbishnoi    07/12/07 - performance bug: remove ViewRelnSrc Operator in phyplan
 parujain    01/17/07 - fix ViewRelnSrc Operator sharing
 parujain    12/15/06 - operator sharing
 najain      07/31/06 - silent relations
 najain      05/22/06 - view support 
 najain      05/04/06 - sharing support 
 najain      04/06/06 - cleanup
 najain      04/04/06 - cleanup
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptRelnSrcFactory.java /main/9 2010/01/25 00:32:43 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptRelnSrc;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.TableManager;
import oracle.cep.metadata.View;
import oracle.cep.metadata.ViewManager;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.phyplan.PhyOptRelnSrc;
import oracle.cep.planmgr.PlanManager;
import oracle.cep.service.ExecContext;

/**
 * LogOptSrcStrmFactory
 * 
 * @author najain
 */
class PhyOptRelnSrcFactory extends PhyOptFactory
{
  PhyOpt newPhyOpt(Object ctx) throws CEPException
  {
    assert false;
    return null;
  }

  PhyOpt getNewPhyOpt(Object ctx) throws CEPException
  {
    assert ctx instanceof LogPlanInterpreterFactoryContext;

    LogPlanInterpreterFactoryContext lpctx = (LogPlanInterpreterFactoryContext) ctx;

    LogOpt logOp = lpctx.getLogPlan();
    assert logOp instanceof LogOptRelnSrc;
    LogOptRelnSrc lRelnSrc = (LogOptRelnSrc) logOp;

    PhyOpt op;

    ExecContext ec = lpctx.getExecContext();
    // Has the operator for the specified table already been initialized
    op = ec.getPlanMgr().getSourceOpt(lRelnSrc.getRelationId());
    if (op != null)
      return op;

    // Special processing for a view
    int tableId = lRelnSrc.getRelationId();
    boolean isView = false;
    PhyOpt input = null;

    try
    {
      View view = ec.getViewMgr().getView(tableId);
      int qryId = view.getQueryId();
      input = ec.getPlanMgr().getQueryRootOpt(qryId);
      isView = true;
    }
    catch (MetadataException e)
    {
      if (e.getErrorCode() != MetadataError.INVALID_VIEW_IDENTIFIER)
        throw e;
    }

    if (isView)
    {
      input.addQryId(new Integer(lpctx.getQuery().getId()), true);

      op = input;
      op.setIsView(true);
      op.setIsSource(true);
    }
    else
    {
      op = new PhyOptRelnSrc(ec, lRelnSrc, lpctx.getPhyChildPlans());
      op.setIsSource(true);

      
      //TODO: Silent relation cleanup will remove this code
      if (ec.getTableMgr().getTable(tableId).getIsSilent())
        op.setSilentRelnDep();
      op.addSilentRelnDep(op);
      
    }

    // Add the operator
    ec.getPlanMgr().setSourceOpt(lRelnSrc.getRelationId(), op);
    op.addSourceId(tableId);

    return op;
  }
}
