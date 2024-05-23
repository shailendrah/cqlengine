/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptStrmSrcFactory.java /main/13 2011/07/20 13:46:36 alealves Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sborah      03/19/09 - siggen optimization: removing viewstrmsrc
 hopark      10/09/08 - remove statics
 hopark      10/07/08 - use execContext to remove statics
 parujain    05/09/08 - viewrelnsrc drop
 mthatte     04/10/08 - 
 parujain    03/11/08 - derived timestamp
 anasrini    09/03/07 - support for ELEMENT_TIME, reintro ViewStrmSrc op
 sbishnoi    07/12/07 - Performance bug: remove ViewStrmSrc operator in phyplan
 parujain    01/17/07 - fix ViewStrmSrc Operator sharing
 parujain    12/15/06 - operator sharing
 najain      07/31/06 - silent relations
 najain      05/17/06 - add view source 
 najain      05/04/06 - sharing support 
 najain      04/06/06 - cleanup
 najain      04/04/06 - cleanup
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/PhyOptStrmSrcFactory.java st_pcbpel_alealves_9261513/3 2010/07/09 11:50:27 alealves Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import oracle.cep.common.OrderingKind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptStrmSrc;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.View;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyOptStrmSrc;
import oracle.cep.phyplan.PhyOptViewStrmSrc;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.service.ExecContext;

/**
 * LogOptSrcStrmFactory
 * 
 * @author najain
 */
class PhyOptStrmSrcFactory extends PhyOptFactory
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
    assert logOp instanceof LogOptStrmSrc;
    LogOptStrmSrc lStrmSrc = (LogOptStrmSrc) logOp;

    PhyOpt op;
    ExecContext ec = lpctx.getExecContext();
    
    // Has the operator for the specified table already been initialized
    // We keep the ordered and ordered sources separately, as they have different execute operators.
    // Note that the partition ordered sources are to be set as an unordered.
    if (logOp.getOrderingConstraint() == OrderingKind.TOTAL_ORDER)
      op = ec.getPlanMgr().getSourceOpt(lStrmSrc.getStreamId());
    else
      op = ec.getPlanMgr().getUnorderedSourceOpt(lStrmSrc.getStreamId());
    
    if (op != null)
      return op;
    
    // Special processing for a view
    int tableId = lStrmSrc.getStreamId();
    boolean isView = false;
    PhyOpt input = null;
    
    try
    {
      View view = ec.getViewMgr().getView(tableId);
      int  qryId = view.getQueryId();
      input = ec.getPlanMgr().getQueryRootOpt(qryId);
      isView = true;
    } catch (MetadataException e)
    {
      if (e.getErrorCode() != MetadataError.INVALID_VIEW_IDENTIFIER)
        throw e;
    }

    if (isView)
    {
      input.addQryId(new Integer(lpctx.getQuery().getId()), true);
      
      // Removing ViewStreamSource if view source input operator is Stream Join,
      // in which case we know that no 'windows' are being used.     
      if(input.getOperatorKind() == PhyOptKind.PO_STR_JOIN || 
         input.getOperatorKind() == PhyOptKind.PO_STR_JOIN_PROJECT 
         /* || input.getOrderingConstraint() == OrderingKind.UNORDERED */ 
         /* REVIEW See bug 12685685 - Optimization is causing ArrayOutOfBounds exception */
         )
      {
        op = input;
      }
      else
        op = new PhyOptViewStrmSrc(ec, lStrmSrc, input);
      
      op.setIsSource(true);
      op.setIsView(true);

      if (input.isSilentRelnDep())
      {
        op.setSilentRelnDep();
        op.addSilentRelnDep(input.getSilentRelnDep());
      }
    }
    else
    {
      op = new PhyOptStrmSrc(ec, lStrmSrc, lpctx.getPhyChildPlans());
      op.setIsSource(true);
      if(lStrmSrc.getDerivedTSExpr() != null)
      {
        Expr dExpr = LogPlanExprFactory.getInterpreter(lStrmSrc.getDerivedTSExpr(),
                 new LogPlanExprFactoryContext(lStrmSrc.getDerivedTSExpr(), lStrmSrc));
        ((PhyOptStrmSrc)op).setDerivedTs(dExpr);
      }
    }

    // Add the operator.
    // Partition ordered is to be set as unordered source.
    if (logOp.getOrderingConstraint() == OrderingKind.TOTAL_ORDER)
      ec.getPlanMgr().setSourceOpt(lStrmSrc.getStreamId(), op);
    else
      ec.getPlanMgr().setUnorderedSourceOpt(lStrmSrc.getStreamId(), op);
    
    op.addSourceId(tableId);
    
    return op;
  }

}
