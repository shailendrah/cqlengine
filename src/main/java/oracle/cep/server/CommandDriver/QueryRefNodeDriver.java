/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandDriver/QueryRefNodeDriver.java /main/13 2011/07/27 08:41:57 udeshmuk Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/12/11 - support for alter query start time DDL
    sborah      03/17/11 - add arith_expr to ordering constraints
    udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/01/10 - add propagateHeartbeat argument while calling add
                           destination
    sborah      06/16/10 - ordering constraint
    sbishnoi    08/25/09 - support for output batching
    parujain    01/28/09 - transaction mgmt
    sborah      11/24/08 - support for altering base timeline
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/05/08 - support offset
    sbishnoi    11/05/07 - support for update semantics
    hopark      06/06/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/server/CommandDriver/QueryRefNodeDriver.java /main/10 2010/11/19 07:47:47 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPQueryRefNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class QueryRefNodeDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    CEPQueryRefNode n = (CEPQueryRefNode)node;
    String qName = n.getName();
    
    try{

    switch (n.getKind()) {
    case START: {
      ec.getQueryMgr().startNamedQuery( qName, schema);
      break;
    }

    case DROP: {
      ec.getQueryMgr().dropNamedQuery( qName, schema);
      break;
    }

    case ADDDEST: {
      ec.getQueryMgr().
        addNamedQueryDestination(qName, n.getValue(), schema,
                                 n.getIsUpdateSemantics(),
                                 n.isBatchOutputTuples(),
				 n.getPropagateHeartbeat());
      break;
    }

    case STOP: {
      ec.getQueryMgr().stopNamedQuery(qName, schema);
      break;
    }

    case ENABLE_MONITOR: {
      ec.getQueryMgr().enableNamedQueryStats(qName, schema,
                                             n.getIsBaseTimelineMillisecond());
      break;
    }
    
    case DISABLE_MONITOR: {
    ec.getQueryMgr().disableNamedQueryStats(qName, schema);
    break;
    }
    
    case ORDERING_CONSTRAINT:
    {
      ec.getQueryMgr().alterOrderingConstraint(qName, schema, 
                                               n.getOrderingConstraint(),
                                               n.getParallelPartitioningExpr());
      break;
    }
    
    case SETSTARTTIME:
    {
      ec.getQueryMgr().setQueryStartTime(qName, -1, schema,
                                         n.getStartTimeValue(), ec);
      break;
    }
    
    default:
    break;
    }
    
    }catch(CEPException e)
    {
      e.setStartOffset(n.getStartOffset());
      e.setEndOffset(n.getEndOffset());
      throw e;
    }
  }
}

