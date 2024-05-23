/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanParallelismHelper.java /main/17 2015/02/06 15:09:31 sbishnoi Exp $ */

/* Copyright (c) 2009, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      udeshmuk  11/24/14 - bug 19305663 - check destination properties like
                           batching when creating alter query add dest
      sbishnoi  09/23/14 - support for partitioned stream
      udeshmuk  09/11/14 - use isPartitioned flag to bypass the check of
                           whether partn ordered execution is possible
      anasrini  08/22/11 - XbranchMerge anasrini_bug-12585148_ps5 from
                           st_pcbpel_11.1.1.4.0
      anasrini  07/25/11 - XbranchMerge anasrini_bug-12640350_ps5 from
                           st_pcbpel_11.1.1.4.0
      anasrini  07/25/11 - partition ordered query over ordered view
      alealves  07/21/11 - XbranchMerge alealves_bug-12685685_cep_main from
                           main
      anasrini  07/19/11 - XbranchMerge anasrini_bug-12752107_ps5 from
                           st_pcbpel_11.1.1.4.0
      anasrini  07/07/11 - XbranchMerge anasrini_bug-12640265_ps5 from
                           st_pcbpel_11.1.1.4.0
      anasrini  07/06/11 - bug 12640265
      alealves  06/21/11 - Support for concurrent views
      alealves  06/21/11 - XbranchMerge alealves_bug-12584321_cep from main
      anasrini  05/17/11 - XbranchMerge anasrini_bug-12560613_ps5 from main
      anasrini  04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
      anasrini  03/29/11 - partition parallelism for views
      anasrini  03/16/11 - partition parallelism
      alealves  02/24/11 - Concurrency infra and initial operators
      alealves  02/24/11 - Concurrency infra and initial operators     
      alealves  06/30/10 - Creation

 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogPlanParallelismHelper.java /main/17 2015/02/06 15:09:31 sbishnoi Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.cep.common.OrderingConstraint;
import oracle.cep.common.OrderingKind;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.metadata.Destination;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.UserFunction;
import oracle.cep.metadata.DependencyType;
import oracle.cep.service.ExecContext;


public class LogPlanParallelismHelper
{
  /**
   * Derive Parallelism Constraints For Given Query.
   * @param q Input Query
   * @param queryPlan Logical Query Plan
   * @return ordering constraints of query
   * @throws CEPException
   */
  public static Map<String,OrderingConstraint> deriveParallelism(Query q, LogOpt queryPlan)
      throws CEPException
  {
    queryPlan.setSourceLineages();
    
    // Initializ an empty map having entry for each source
    Map<String,OrderingConstraint> orderingConstraints = new HashMap<String,OrderingConstraint>();
    ArrayList<LogOpt> sourceLineage = queryPlan.getSourceLineage();
    assert sourceLineage != null && !sourceLineage.isEmpty();
    
    // By Default, All Input Sources are marked as UNORDERED
    for(LogOpt sourceOpt: sourceLineage)
    {
      orderingConstraints.put(((LogOptSource)sourceOpt).getEntityName(), new OrderingConstraint(OrderingKind.UNORDERED));
    }
    
    setParallelism(queryPlan, orderingConstraints);
    return orderingConstraints;
  }
  
  /**
   * Apply new ordering constraints to list of sources.
   * @param existingOrderingConstraintsMap Existing Ordering Constraints for various sources
   * @param source                         Source whose ordering constraints need to be modified
   * @param newOrderingConstraint          New ordering constraints
   */
  public static void setOrderingConstraint(Map<String,OrderingConstraint> existingOrderingConstraintsMap,
                                           LogOpt source,
                                           OrderingConstraint newOrderingConstraint)
  {
    OrderingConstraint existingOrderingConstraints 
      = existingOrderingConstraintsMap.get(((LogOptSource)source).getEntityName());
    
    switch(existingOrderingConstraints.getKind())
    {
    case UNORDERED:
    {
      switch(newOrderingConstraint.getKind())
      {
      case UNORDERED:
        // Do Nothing. Ordering Constraint is already UNORDERED.
        break;
      case PARTITION_ORDERED:
      case TOTAL_ORDER:
        existingOrderingConstraintsMap.put(((LogOptSource)source).getEntityName(), newOrderingConstraint);
        break;
      }
    }
    break;
    case PARTITION_ORDERED:
      switch(newOrderingConstraint.getKind())
      {
      case UNORDERED:
        // Do Nothing. PARTITION_ORDERED source can't be set as UNORDERED.
        break;
      case PARTITION_ORDERED:
        // Compare partitioning attributes of new ordering constraint requirement
        // with existing constraint's ordering attributes
        List<String> orderingAttrs = newOrderingConstraint.getOrderingAttributes();
        List<String> existingOrderingAttrs = existingOrderingConstraints.getOrderingAttributes();
        
        assert orderingAttrs != null && orderingAttrs.size() > 0;
        
        if(existingOrderingAttrs == null || existingOrderingAttrs.size() == 0)
        {
          existingOrderingConstraintsMap.put(((LogOptSource)source).getEntityName(), newOrderingConstraint);
        }
        else if(orderingAttrs.size() == existingOrderingAttrs.size())
        {
          boolean allmatches = true;
          for(String attr: orderingAttrs)
            allmatches = allmatches && existingOrderingAttrs.contains(attr);
          
          // If new ordering requirement specifies a different set of partitioning attributes,
          // then we will set the ordering constraint to TOTAL ORDER.
          // We can't have two operators running with different partitioning criteria
          // in same query plan.
          if(!allmatches)
            existingOrderingConstraintsMap.put(((LogOptSource)source).getEntityName(), 
                                                 new OrderingConstraint(OrderingKind.TOTAL_ORDER));
        }
        
        break;
      case TOTAL_ORDER:
        // Overwrite existing ordering constraints with TOTAL_ORDER.
        existingOrderingConstraintsMap.put(((LogOptSource)source).getEntityName(), 
            new OrderingConstraint(OrderingKind.TOTAL_ORDER));
        break;
      }
      break;
    case TOTAL_ORDER:
      switch(newOrderingConstraint.getKind())
      {
      case UNORDERED:
      case PARTITION_ORDERED:
      case TOTAL_ORDER:
      // Do Nothing. Ordering Constraint is already TOTAL_ORDERED
        break;
      }
      break;
    default:
    }
    
  }
  
  /**
   * Determine ordering constraint for the query plan sub-tree.
   * @param queryPlan root of query subtree
   * @param orderingConstraints map of ordering constraints for each source
   */
  public static void setParallelism(LogOpt queryPlan, Map<String,OrderingConstraint> orderingConstraints)
  {
    boolean hasMarkedPartitionOrdered = false;
    assert queryPlan.sourceLineage != null && queryPlan.sourceLineage.size() > 0;
    
    switch(queryPlan.operatorKind)
    {
    // For Source Operators, Do Nothing as these operators don't have any ordering requirements.
    case LO_STREAM_SOURCE:
    case LO_RELN_SOURCE:
    case LO_TABLE_FUNCTION:
      break;
      
    case LO_RANGE_WIN:
    case LO_NOW_WIN:
    case LO_ISTREAM:
    case LO_DSTREAM:
    case LO_RSTREAM:
    case LO_SLIDE:
    case LO_SELECT:
    case LO_PROJECT:
    case LO_SUBQUERY_SOURCE:
      for(LogOpt source: queryPlan.sourceLineage)
        setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.UNORDERED));
      break;
    
    case LO_ROW_WIN:
    case LO_EXTENSIBLE_WIN:
    case LO_VALUE_WIN:
    case LO_EXCEPT:
    case LO_MINUS:
    case LO_DISTINCT:
    case LO_ORDER_BY:
    case LO_XMLTABLE:
    case LO_EXCHANGE:
      for(LogOpt source: queryPlan.sourceLineage)
        setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.TOTAL_ORDER));
      break;
    
    case LO_PARTN_WIN:
    case LO_GROUP_AGGR:
    case LO_PATTERN_STRM:
      Map<String,List<String>> partnSourceAttrMap = new HashMap<String,List<String>>();
      Attr[] partnAttrs = null;
      
      // Determine partitioning attributes
      if(queryPlan.getOperatorKind() == LogOptKind.LO_PARTN_WIN)
      {
        LogOptPrtnWin partnWinOp = (LogOptPrtnWin)queryPlan;
        partnAttrs = partnWinOp.getPartnAttrs();
      }
      else if(queryPlan.getOperatorKind() == LogOptKind.LO_GROUP_AGGR)
      {
        LogOptGrpAggr logOptGrpAggr = (LogOptGrpAggr)queryPlan;
        ArrayList<Attr> groupingAttrs = logOptGrpAggr.getGroupAttrs();
        if(groupingAttrs != null)
          partnAttrs = groupingAttrs.toArray(new Attr[groupingAttrs.size()]);
      }
      else
      {
        LogOptPatternStrm logPatternStrm = (LogOptPatternStrm)queryPlan;
        partnAttrs = logPatternStrm.getPartnAttrs();
      }
      
      // Set all sources to total ordered if there is no partitioning attributes
      if(partnAttrs == null || partnAttrs.length == 0)
      {
        for(LogOpt source: queryPlan.sourceLineage)
          setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.TOTAL_ORDER));
      }
      else
      {
        // Source-wise distribute partition attributes
        for(Attr a: partnAttrs)
        {
          String actualAttrName = a.getActualName();
          String attrSourceName = actualAttrName.substring(0, actualAttrName.indexOf('.'));
          String partnAttrName  = actualAttrName.substring(actualAttrName.indexOf('.')+1, actualAttrName.length());
          
          if(partnSourceAttrMap.containsKey(attrSourceName))
            partnSourceAttrMap.get(attrSourceName).add(partnAttrName);
          else
          {
            List<String> temp = new LinkedList<String>();
            temp.add(partnAttrName);
            partnSourceAttrMap.put(attrSourceName, temp);
          }
        }
      
        // Only one source can execute in PARTITION ORDERED. In our case, we will choose first 
        // source in the lineage to mark as PARTITION ORDERED.
        hasMarkedPartitionOrdered = false;
        for(LogOpt source: queryPlan.sourceLineage)
        {
          String sourceName = ((LogOptSource)source).getEntityName();
          if(partnSourceAttrMap.containsKey(sourceName))
          {
            List<String> orderingAttrs = partnSourceAttrMap.get(sourceName);
            if(hasMarkedPartitionOrdered)
              setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.TOTAL_ORDER));
            else
            {
              setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.PARTITION_ORDERED, orderingAttrs));
              hasMarkedPartitionOrdered = true;
            }
          }
          else
          {
            setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.TOTAL_ORDER));
          }
        }
      }
      break;
      
    case LO_CROSS:
    case LO_STREAM_CROSS:
      // Join without external relation will always run in Total Order
      boolean joinWithExternalRelation = queryPlan.isExternal();
      if(joinWithExternalRelation)
      {
        boolean isDirectJoinWithExternalReln = true;
        for(LogOpt next: queryPlan.getInputs())
        {
          if(next.isExternal())
            isDirectJoinWithExternalReln = isDirectJoinWithExternalReln && next instanceof LogOptSource;
        }
        if(isDirectJoinWithExternalReln)
        {
          for(LogOpt source: queryPlan.sourceLineage)
            setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.UNORDERED));
        }
        else
        {
          for(LogOpt source: queryPlan.sourceLineage)
            setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.TOTAL_ORDER));
        }
      }
      else
      {
        for(LogOpt source: queryPlan.sourceLineage)
          setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.TOTAL_ORDER));
      }
      
      break;
      
    case LO_UNION:
      boolean isUnionAll = ((LogOptUnion)queryPlan).isUnionAll();
      if(isUnionAll)
      {
        for(LogOpt source: queryPlan.sourceLineage)
          setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.UNORDERED));
      }
      else
      {
        for(LogOpt source: queryPlan.sourceLineage)
          setOrderingConstraint(orderingConstraints, source, new OrderingConstraint(OrderingKind.TOTAL_ORDER));
      }
      break;
    
    default:
    }
    
    // Invoke setParallelism for child operators
    for(LogOpt next: queryPlan.getInputs())
    {
      setParallelism(next,orderingConstraints);
    }
  }
  
  public static LogOpt determineParallelism(ExecContext ec, Query q,
                                            LogOpt queryPlan)
    throws CEPException
  {
    if (q == null)   // subquery and parallelism - don't marry for now
      return queryPlan;
    
    OrderingKind userOrderConstraint = q.getUserOrderingConstraint();
    switch(userOrderConstraint) 
    {
    case TOTAL_ORDER:
      return validateOrderedParallelism(ec, q, queryPlan);
    case PARTITION_ORDERED:
      if(q.isDependentOnPartnStream())
        return determinePartitionParallelism(ec, q, queryPlan, true);
      else
        return determinePartitionParallelism(ec, q, queryPlan, false);
    case UNORDERED:
      return determineUnorderedParallelism(ec, q, queryPlan);
    default:
      return queryPlan;
    }
  }
  
  private static LogOpt validateOrderedParallelism(ExecContext ec, Query q,
      LogOpt queryPlan) throws CEPException
  {
    List<LogOpt> dag = new LinkedList<LogOpt>();
    List<LogOptSource> sourceViews                 
      = new LinkedList<LogOptSource>();
    dag.add(queryPlan);

    while((!dag.isEmpty()))
    {
      LogOpt     node = dag.remove(0);
      LogOptKind kind = node.getOperatorKind();
      
      switch (kind)
      {
      // No need to check for REL_SOURCE, as it would have been 
      //  coerced into ordered...
      case LO_RELN_SOURCE:
      case LO_STREAM_SOURCE:
        if (((LogOptSource) node).isView())
          sourceViews.add((LogOptSource) node);
      }
      
      dag.addAll(node.getInputs());
    }
    
    for(LogOptSource source : sourceViews)
    {
      LogOpt rootLogOpt =
        ec.getViewMgr().getLogPlan(source.getEntityId());

      if (rootLogOpt.getOrderingConstraint() != OrderingKind.TOTAL_ORDER)
      {
        // Base is unordered or partition ordered, but derived has been configured as ordered. 
        // This is a compilation error, as we can't lay out a more restricted constraint on top of a less one.
        throw new CEPException(SemanticError.MISMATCHED_ORDERING_CONSTRAINT);
      }
    }
    
    return queryPlan;
  }

  public static LogOpt determineUnorderedParallelism(ExecContext ec, Query q, 
      LogOpt queryPlan) throws CEPException
  {
    // Iterate through the complete logical plan, and make sure all
    // the operators are stateless

    boolean canParallelise = true;
    
    List<LogOpt> dag = new LinkedList<LogOpt>();
    List<LogOptSource> sourceViews                 
      = new LinkedList<LogOptSource>();
    dag.add(queryPlan);

    // Do not break sooner, as we need to collect all sources.
    while((!dag.isEmpty()))
    {
      LogOpt     node = dag.remove(0);
      LogOptKind kind = node.getOperatorKind();
      
      switch (kind)
      {
      case LO_STREAM_SOURCE:
        if (((LogOptSource) node).isView())
          sourceViews.add((LogOptSource) node);
      case LO_SELECT:
      case LO_PROJECT:
      case LO_STREAM_CROSS:
        break;
      case LO_RELN_SOURCE:  // allow a relation source iff it is external.
        if (node.isExternal())
          break;
        else
        {
          // queries on top of relational views cannot run in parallel, however
          //  query cannot be coerced as usual to ordered if view is partition-ordered.  
          if (((LogOptSource) node).isView()) 
            sourceViews.add((LogOptSource) node);
        }
      default:
        canParallelise = false;
      }
      
      dag.addAll(node.getInputs());
    }
    
    // First check if the views can also run in parallel.
    // If a base view cannot, then the whole query is considered ordered.
    for (LogOptSource source : sourceViews)
    {
      LogOpt rootLogOpt =
        ec.getViewMgr().getLogPlan(source.getEntityId());

      if (rootLogOpt.getOrderingConstraint() == OrderingKind.UNORDERED)
      {
        if (canParallelise)
        {
          // Good, base and derived are unordered.
        }
        else
        {
          // Bad, source view is unordered, but query is found to be ordered.
          throw new CEPException(SemanticError.MISMATCHED_ORDERING_CONSTRAINT);
        }
      }
      else if (rootLogOpt.getOrderingConstraint() == OrderingKind.PARTITION_ORDERED)
      {
        // REVIEW In the future we can try to coerce the results, however this is not  
        //  simple because there is a need to check the schema of the output, etc.
        throw new CEPException(SemanticError.MISMATCHED_ORDERING_CONSTRAINT);
      }
      else // ordered
      {
        if (!canParallelise) 
        {
          // Good, both base and derived are ordered
        }
        else
        {
          // Bad, base is ordered, but derived is unordered. In this case, consider both as 
          //  ordered.
          canParallelise = false; // don't break, as we wan't to validate the other sources as well.
        }
      }
    }
    
    // Set the determined order constraint on each operator of the logical
    // plan
    if (canParallelise)
    {
      dag.clear();
      dag.add(queryPlan);
      
      while (!dag.isEmpty())
      {
        LogOpt node = dag.remove(0);
        node.setOrderingConstraint(OrderingKind.UNORDERED);
        dag.addAll(node.getInputs());
      }
    }
    
    return queryPlan;
  }

  public static LogOpt determinePartitionParallelism(ExecContext ec, Query q, 
                                                     LogOpt queryPlan, 
                                                     boolean skipPushPartnExpr)
    throws CEPException

  {
    Expr ppExpr = queryPlan.getPartitionParallelExpr();
    
    // If query is dependent on partitioned stream, the partition expression
    // will be null as partition id will be provided by input thread
    if(!q.isDependentOnPartnStream())
    {
      assert ppExpr != null : 
      "Partition parallel expr should not be null for PARTITION ORDERED "
      + "constraint for " + q.getName();
    
      LogUtil.fine(LoggerType.TRACE,
                  "Logical form of partition parallel expression for "
                 + q.getName() + " is " +ppExpr);
    }
    
    // Contains all persistent internal sources (base or view push sources)
    // that are in the lineage of this query.
    List<LogOptSource> sources                = new LinkedList<LogOptSource>();

    // Contains all persistent base internal sources (push sources)
    // that are in the lineage of this query.
    List<LogOptSource> baseSources            = new LinkedList<LogOptSource>();

    // Contains all persistent view internal sources (push sources)
    // that are directly referenced by this query
    List<LogOptSource> viewSources            = new LinkedList<LogOptSource>();

    // Contains all persistent base internal sources (push sources) that are in
    // the lineage of this query. 1 entry for each distinct source.
    List<LogOptSource> inputs                 = new LinkedList<LogOptSource>();

    // Contains all persistent external sources (pull sources)
    // that are directly referenced by this query
    List<LogOptSource> externalRelationSources
      = new LinkedList<LogOptSource>();
    
    // If query is dependent on multiple sources(e.g. having binary operators)
    // then we allow only one source to be partitioned and all other sources
    // will be non partitioned.
    List<LogOptSource> nonPartitionOrderedSources = new LinkedList<LogOptSource>();

    List<LogOpt>       dag                    = new LinkedList<LogOpt>();
    Set<String>        depViewSet             = new LinkedHashSet<String>();

    // If a query is dependent on a partition stream, there will be no partition
    // expression. So we will not push any expression below the plan.
    // This will allow us to define any query on top of partition stream with 
    // all CQL constructs.
    
    if(!skipPushPartnExpr)
    {
      // First attempt to push the partition expression all the way down
      dag.add(queryPlan);
      while(!dag.isEmpty())
      {
        LogOpt node      = dag.remove(0);
        int    numInputs = node.getNumInputs();
  
        if (node.getPartitionParallelExpr() != null)
        {
          for (int i=0; i<numInputs; i++)
          {
            Expr e = node.canPartitionExprBePushed(i);
            node.getInput(i).setPartitionParallelExpr(e);
          }
        }
  
        dag.addAll(node.getInputs());
      }
    }

    // Iterate through all the source operators in the logical plan and
    // check that each can be partitioned
    // Even if we cannot partition, we still need to check if there are any
    //  views that can partition, and raise an exception in this case.
    boolean canPartition = true;

    dag.clear();
    dag.add(queryPlan);
    while(!dag.isEmpty())
    {
      LogOpt     node = dag.remove(0);
      LogOptKind kind = node.getOperatorKind();
      
      switch (kind)
      {
      case LO_STREAM_SOURCE:
      case LO_RELN_SOURCE:
        // Even if one of the sources cannot be partitioned for parallelism,
        // then partition parallel execution of query is not possible.
        // In this case it will be executed as per OrderingKind.TOTAL_ORDER
        
        // In case of external relations, an exception is made and they are 
        // allowed to be executed in partitioning parallel order even 
        // without having a partitioning parallel expression.
       
        if ((node.getPartitionParallelExpr() == null) 
            && (!node.isExternal()) 
            && (!q.isDependentOnPartnStream()))
          canPartition = false;
        else
          sources.add((LogOptSource)node);
        break;
      default:
        break;
      }
      dag.addAll(node.getInputs());
    }

    // Iterate through the sources and "expand" them to the base entities
    // That is keep removing the views and replace with the inputs
    // of their corresponding LO_EXCHANGE which are guaranteed to be 
    // source corresponding to base entities

    boolean allOrderedViews          = true;
    boolean existsNonPartOrderedView = false;
    
    // A Flag to check if query has already one partitioned source
    boolean hasPartnOrderedSources = false;

    for(LogOptSource source : sources)
    {
      if(source.isExternal())
      {
        externalRelationSources.add(source);
      }
      else if (source.isView())
      {
        LogOpt         rootLogOpt;
        LogOptExchange viewExch;
        OrderingKind   okind;

        rootLogOpt = ec.getViewMgr().getLogPlan(source.getEntityId());
        okind      = rootLogOpt.getOrderingConstraint();
        

        if (okind == OrderingKind.UNORDERED)
        {
          if(q.isDependentOnPartnStream())
          {
            //FIXME: what should be done here? 
             
          }
          else
          {
            existsNonPartOrderedView = true;
            allOrderedViews = false;
          }
        }
        else if (okind == OrderingKind.TOTAL_ORDER)
        {
          if(q.isDependentOnPartnStream())
          {
            //find source operator(s) for the view and them to the list of 
            //depViewSet. View is added in code at the end of source.isView block.
            addDependenciesToDepViewSet(source, rootLogOpt, depViewSet, ec, false);
          }
          else
            existsNonPartOrderedView = true;
        }
        else // View is partition ordered
        { // a view can be partition ordered in two ways:
          // (1) it is based on a partn stream
          // (2) it is having explicit ordering constraint as PARTITION_ORDERED
          // In the first case we can process if no other source is partitioned
          // or is explicitly partition-ordered.
          // FIXME: what to do in second case?
          if(q.isDependentOnPartnStream())
          { 
            // Determine whether the view is depending on a partitioned stream
            boolean isPartitionedSource 
              = ec.getViewMgr().getView(source.getEntityId()).isPartitioned();
            
            // Raise an exception if the query is dependent on multiple partitioned
            // sources
            if(isPartitionedSource && hasPartnOrderedSources)
            {
              throw new CEPException(
                SemanticError.MULTIPLE_PARTITIONED_SOURCES_NOT_ALLOWED, q.getName());
            }
            
            allOrderedViews = false;
            
            // Set the flag whether query has partitioned source or not
            hasPartnOrderedSources 
              = isPartitionedSource ? true : hasPartnOrderedSources;
            
            viewExch = (LogOptExchange) rootLogOpt;
            Collection<String> depViewDDLs = viewExch.getDDLs();
            if (depViewDDLs != null)
              depViewSet.addAll(depViewDDLs);

            for(LogOpt exchInput : viewExch.getInputs())
            {
              baseSources.add((LogOptSource)exchInput);
            }
          }
          else
          {  
            allOrderedViews = false;
            
            viewExch = (LogOptExchange) rootLogOpt;
            Collection<String> depViewDDLs = viewExch.getDDLs();
            if (depViewDDLs != null)
              depViewSet.addAll(depViewDDLs);

            for(LogOpt exchInput : viewExch.getInputs())
            {
              baseSources.add((LogOptSource)exchInput);
            }
          }
        }
        viewSources.add(source);
      }
      else
      {
        // If Query is dependent on partition stream then
        // Ensure that only one partition source will be added to exchange's 
        // input and others will be added as DDLs.
        if(q.isDependentOnPartnStream())
        {
          boolean isPartitionedSource
            = ec.getTableMgr().getTable(source.getEntityId()).isPartitioned();
          
          if(isPartitionedSource)
          {
             // Currently we allow only one partitioned stream in a query
             if(hasPartnOrderedSources)
             {
               throw new CEPException(
                 SemanticError.MULTIPLE_PARTITIONED_SOURCES_NOT_ALLOWED, 
                 q.getName());
             }
             else
             {
               hasPartnOrderedSources = true;
               baseSources.add(source); 
             }
          }
          else
          {
             nonPartitionOrderedSources.add(source);
          }
        }
        else
        {
          baseSources.add(source); 
        }
        
      }
    }

    // If we cannot partition query, check if we can simply return or if it is an exception.
    if (!canPartition)
    {
      if (allOrderedViews)
        return queryPlan;
      else
        throw new CEPException(SemanticError.MISMATCHED_ORDERING_CONSTRAINT);
    }

    // Now check that all views are PARTITION ORDERED
    if (existsNonPartOrderedView)
    {
      if (allOrderedViews)
        return queryPlan;
      else
        throw new CEPException(SemanticError.MISMATCHED_ORDERING_CONSTRAINT);
    } 

    // Now, check that if a stream appears multiple times in the query
    // (through different aliases), then the partitioning expression is the
    // same
    HashMap<Integer, Expr> streamToPartExprMap = new HashMap<Integer, Expr>();
    for(LogOptSource source : baseSources)
    {
      int  strid          = source.getEntityId();
      Expr streamPartExpr = streamToPartExprMap.get(strid);
      Expr aliasPartExpr  = source.getPartitionParallelExpr();

      if (streamPartExpr == null)
      {
        streamToPartExprMap.put(strid, aliasPartExpr);
        inputs.add(source);
      }
      else if (!(streamPartExpr.equals(aliasPartExpr)))
        return queryPlan;
    }

    // At this point, partition parallel execution is possible
    LogUtil.fine(LoggerType.TRACE,
                 "Partition parallel execution possible for query " 
                 + q.getName());
    
    // FIXME Need to move side-effects to other location, and let this only validate
    //  the ordering constraint

    LogOptExchange exch = new LogOptExchange();
    exch.setDependentOnPartnStream(q.isDependentOnPartnStream());
    
    for(LogOptSource input : inputs)
    {
      LogUtil.fine(LoggerType.TRACE, 
                   "Partition parallel expr for " + input.getEntityName()
                   + " is " + input.getPartitionParallelExpr());
      exch.addInput(input);
      input.setOrderingConstraint(OrderingKind.PARTITION_ORDERED);
    }
    exch.setOrderingConstraint(OrderingKind.PARTITION_ORDERED);

    // First add the DDLs corresponding to all the dependencies
    for(String ddl : depViewSet)
    {
      exch.addDDL(ddl);
    }

    // Now add the "create view ddls"
    for(LogOptSource view : viewSources)
    {
      exch.addDDL(view.getCreateDDL());
    }
    
    // Now add the external relation related ddls if they exist
    for(LogOptSource externalRelation : externalRelationSources)
    {
      exch.addExernalRelationInput(externalRelation);
    }
    
    // ADd the non partitioned source relatd ddls if they exist
    for(LogOptSource nonPartitionedSource : nonPartitionOrderedSources)
    {
      exch.addNonPartitionOrderedSource(nonPartitionedSource);
    }

    // Now add the user-defined functions/aggregates related ddl 
    Integer[] fnIds =
      ec.getDependencyMgr().getMasters(q.getId(), DependencyType.FUNCTION);
    if (fnIds != null)
    {
      for (int fnid : fnIds)
      {
        UserFunction f = ec.getUserFnMgr().getFunction(fnid);
        if (!f.isBuiltIn())
          exch.addDDL(f.getCreationText());
      } 
    }

    // Add the query related items only if it has external destinations 
    // Otherwise it might just be defining a view and the startView may be
    // prompting this analysis (same will happen within the partition schemas)
    if (q.getExtDests().size() > 0)
    {
      exch.addDDL(q.getText());
      
      for(Destination dest : q.getExtDests())
      {
        String queryDestDDL =  "alter query " + q.getName() 
          + " add destination \"" + dest.getExtDest() + "\"";        

        String destProps = null;
        if((destProps=dest.getProps()) != null)
          queryDestDDL = queryDestDDL+destProps;

        exch.addDDL(queryDestDDL);
      }

      
      String startQueryDDL = "alter query " + q.getName() + " start";
      exch.addDDL(startQueryDDL);
    }

    LogUtil.fine(LoggerType.TRACE, exch.toString());

    return exch;
  }

  private static void addDependenciesToDepViewSet(LogOptSource source,
      LogOpt rootLogOpt,
      Set<String> depViewSet,
      ExecContext ec,
      boolean addView) throws CEPException{

    List<LogOpt> viewDAG = new LinkedList<LogOpt>();
    viewDAG.add(rootLogOpt);
    while(!viewDAG.isEmpty())
    {
      LogOpt curr = viewDAG.remove(0);
      switch(curr.getOperatorKind())
      {
      case LO_STREAM_SOURCE:
      case LO_RELN_SOURCE:
        LogOptSource src = (LogOptSource) curr;
        if(src.isView())
        {
          LogOpt root = ec.getViewMgr().getLogPlan(src.getEntityId());
          addDependenciesToDepViewSet(src, root, depViewSet, ec, true);
        }
        else
        {
          depViewSet.add(((LogOptSource)curr).getCreateDDL());
          LogUtil.info(LoggerType.TRACE, "Added DDL: "+((LogOptSource)curr).getCreateDDL());

          boolean isPushSource = ((LogOptSource)curr).getSource() == null; 

          if(src.getIsStream())
          {
            if(isPushSource)
              depViewSet.add("alter stream "+src.entityName+ " add source push");
            else
              depViewSet.add("alter stream "+src.entityName+ " add source \" " +((LogOptSource)curr).getSource() + " \"");
          }
          else
          {
            if(isPushSource)
              depViewSet.add("alter relation "+src.entityName+ " add source push");
            else
              depViewSet.add("alter relation "+src.entityName+ " add source \" " +((LogOptSource)curr).getSource() + " \"");
          }
        }
        break;
      default:
        break;
      }
      viewDAG.addAll(curr.getInputs());
    }
    //finally add the view ddl if addView is true
    if(addView) {
      depViewSet.add(source.getCreateDDL());
      LogUtil.info(LoggerType.TRACE, "Added View DDL: "+source.getCreateDDL());
    }
    
  }

}
