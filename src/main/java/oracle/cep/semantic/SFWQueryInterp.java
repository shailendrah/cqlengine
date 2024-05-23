/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SFWQueryInterp.java /main/37 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2006, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    The interpreter for the CEPSFWQueryNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - set partitioned stream in context
    pkali       09/30/12 - XbranchMerge pkali_bug-14464424_ps6 from
                           st_pcbpel_11.1.1.4.0
    pkali       09/23/12 - removed the support-predicate check for extenal
                           relation
    pkali       08/30/12 - XbranchMerge pkali_bug-14465875_ps6 from
                           st_pcbpel_11.1.1.4.0
    pkali       08/27/12 - fixed err msg arg
    vikshukl    08/01/12 - archived dimension
    pkali       07/10/12 - handled null in select * with groupby
    pkali       03/21/12 - groupbyexpr support
    vikshukl    02/20/12 - group by with having and order by
    vikshukl    11/07/11 - group by expr
    vikshukl    07/11/11 - subquery support
    anasrini    04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
    udeshmuk    03/21/11 - use isArchived flag and set it in SFWQuery
    anasrini    03/19/11 - indentation fixes
    sbishnoi    09/24/09 - support for table functions
    parujain    05/18/09 - ansi outer join
    sbishnoi    04/26/09 - throw exception if orderbytop refers to
                           nongroupbyattr
    sbishnoi    03/17/09 - code restructuring
    udeshmuk    03/16/09 - fix semantic issue related to group by and having
    parujain    03/12/09 - make interpreters stateless
    sbishnoi    03/05/09 - adding support to process partition by clause
    sbishnoi    02/09/09 - support for ordered window
    anasrini    02/06/09 - relax predicate checks for external relations
    sborah      01/28/09 - handle constants
    sbishnoi    01/15/09 - apply checks on predicate clause if refers to
                           external relation
    parujain    09/08/08 - support offset
    parujain    08/26/08 - semantic exception offset
    parujain    06/06/08 - invalid usage of xmltype
    udeshmuk    04/19/08 - support for aggr distinct.
    parujain    03/10/08 - derived timestamp
    udeshmuk    02/05/08 - parameterize errors.
    najain      12/05/07 - xmltable support
    rkomurav    08/17/07 - fix group by bug
    parujain    06/22/07 - order by support
    rkomurav    05/28/07 - restructure collectAttrs
    anasrini    05/23/07 - symbol table reorg
    anasrini    05/22/07 - 
    sbishnoi    04/25/07 - support for having clause
    parujain    03/29/07 - case expr with group by
    rkomurav    02/22/07 - cleanup reftables
    rkomurav    02/08/07 - support patterns
    rkomurav    12/05/06 - add checks for select list with aggr
    parujain    11/03/06 - Tree representation for Conditions
    najain      04/06/06 - cleanup
    anasrini    02/26/06 - use getTableId to get tableId given varId 
    anasrini    02/23/06 - support for select clause 
    anasrini    02/23/06 - support groupByClause and selectClause 
    anasrini    02/21/06 - support for whereClause etc. 
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SFWQueryInterp.java /main/37 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import java.util.ArrayList;

import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPOrderByNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSFWQueryNode;
import oracle.cep.parser.CEPRelationNode;
import oracle.cep.parser.CEPBooleanExprNode;
import oracle.cep.parser.CEPSelectListNode;
import oracle.cep.common.Datatype;
import oracle.cep.common.SplRangeType;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 * The interpreter that is specific to the CEPSFWQueryNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class SFWQueryInterp extends QueryRelationInterp {


  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    assert node instanceof CEPSFWQueryNode;
    CEPSFWQueryNode sfwNode = (CEPSFWQueryNode)node;

    SFWQuery sfwQuery = new SFWQuery();
    ctx.setSemQuery(sfwQuery);
    
    // set symbol table
    sfwQuery.setSymTable(ctx.getSymbolTable());

    super.interpretNode(node, ctx);
    
    // At this point the tables have been registered with the symbol table

    // Interpret the from clause
    interpretFromClause(ctx, sfwNode, sfwQuery);

    // Interpret the (optional) where clause.
    interpretWhereClause(ctx, sfwNode, sfwQuery);

    // Interpret the (optional) group by clause
    interpretGroupByClause(ctx, sfwNode, sfwQuery);
    
    // Interpret the (optional) having clause
    interpretHavingClause(ctx, sfwNode, sfwQuery);

    // Interpret the select clause.  We do it last because it references
    // FROM clause and GROUP by clause.
    interpretSelectClause(ctx, sfwNode, sfwQuery);
    
    // Interpret the Order by clause(optional). We do it select clause so as to
    // refer the select list
    interpretOrderByClause(ctx, sfwNode, sfwQuery);
  }

  /**
   * Interpret the from clause of a query
   */
  void interpretFromClause(SemContext ctx, CEPSFWQueryNode sfwNode, 
                           SFWQuery sfwQuery) 
  throws CEPException {
    CEPRelationNode[] rels;
    CEPRelationNode   rel;
    WindowSpec        winspec;
    int               varId;
    NodeInterpreter   relInterp;
    String            relName;
    String            varName;
    PatternSpec       patternSpec;    
    SubquerySpec      subquerySpec;
    SymbolTableEntry  entry = null;
    boolean           isOuterJoinTypeRelNode = false;
    TableFunctionRelationSpec tableFunctionRelSpec = null;

    rels = sfwNode.getFromClauseRelations();
    for (int i=0, r=rels.length; i<r; i++) 
    {
      rel     = rels[i];
      relName = rel.getName();
      varName = rel.getAlias();
      
      if (varName == null)
        varName = relName;

      isOuterJoinTypeRelNode = rel.isOuterJoinRelationNode();
      if(isOuterJoinTypeRelNode) 
       assert rel instanceof oracle.cep.parser.CEPOuterJoinRelationNode;
      
      // Note: varName will be null for the outer_join_relation nodes
      // Bypass this check. Even though anonymous subqueries are not
      // supported today, they might be in the future. Plus there is a 
      // check for that too further down in SubqueryInterp.java. This is 
      // just to eager a check for varName is null.
      if (!rel.isQueryRelationNode())
        assert isOuterJoinTypeRelNode || varName != null;

      // Get interpreter for this relation
      relInterp = InterpreterFactory.getInterpreter(rels[i]);
      
      // interpret the relation
      try
      {
        ctx.setOuterJoinTypeNode(isOuterJoinTypeRelNode);
        
        relInterp.interpretNode(rels[i], ctx);        
        
        winspec = ctx.getWindowSpec();
        ctx.setOuterJoinTypeNode(false);
      }
      catch (CEPException e) 
      {
        if (e.getErrorCode() == SemanticError.NOT_A_RELATION_ERROR) 
        {
          // In case of outerJoinTypeRelNode, This error must be handled in
          // OuterJoinExprInterp
          assert !isOuterJoinTypeRelNode;
        
          // convert into a relation by adding the UNBOUNDED window
          // operator
          winspec = new TimeWindowSpec(SplRangeType.UNBOUNDED);
        }
        else
          throw e;
      }
      patternSpec = ctx.getPatternSpec();
      
      // Get the table function relation spec (if any)
      tableFunctionRelSpec = ctx.getTableFunctionSpec();

      // Get subquery spec (if any)
      subquerySpec = ctx.getSubquerySpec();
      
      // Assumption: 
      // For OuterJoinTypeRelNodes, we have already added the specS while
      // interpreting OuterJoinNode
      if(!isOuterJoinTypeRelNode)
      {
        // update the query with varID,window and pattern specifications
        try
        {
          entry = ctx.getSymbolTable().lookupSource(varName);
        }
        catch(CEPException ce)
        {
          ce.setStartOffset(rel.getStartOffset());
          ce.setEndOffset(rel.getEndOffset());
          throw ce;
        }
        varId = entry.getVarId();
        
        // create RelationSpec for this Relation Node
        RelationSpec rspec = new RelationSpec(varId);
        ctx.setRelationSpec(rspec);        
        sfwQuery.addRelation(rspec, subquerySpec, winspec, patternSpec, 
                             ctx.getXmlTableSpec(), 
                             ctx.getDerivedTimeSpec(), 
                             tableFunctionRelSpec,
                             ctx.isArchived(),
                             ctx.isDimension(),
                             ctx.isPartnStream());
        ctx.setIsArchived(false);
        ctx.setIsDimension(false);
        ctx.setIsPartnStream(false);
      }
      // For every comma separated from clause entry, we will add 
      // its corresponding RelSpec
      sfwQuery.addFromClauseEntries(ctx.getRelationSpec());
    }
  }

  /**
   * Interpret the where clause of a query
   * <p>
   * Converts the list of parsed conditions in the where clause to the
   * semantic forms
   */
  void interpretWhereClause(SemContext ctx, CEPSFWQueryNode sfwNode, 
                            SFWQuery sfwQuery)
  throws CEPException {
    
    CEPBooleanExprNode condNode;
    NodeInterpreter    condInterp;

    condNode   = sfwNode.getWhereClause(); 
  
    if (condNode != null) 
    {
      condInterp = InterpreterFactory.getInterpreter(condNode);    

      condInterp.interpretNode(condNode, ctx);
     
      sfwQuery.setPredicate((BExpr)ctx.getExpr());
    }
  }
  
  /**
   * Interpret the group by clause of a query
   * <p>
   * Converts the list of group by attributes in the clause to their
   * semantic forms
   */
  void interpretGroupByClause(SemContext ctx, CEPSFWQueryNode sfwNode,
                              SFWQuery sfwQuery)
    throws CEPException 
  {    
    CEPExprNode[]   gbyNode;
    NodeInterpreter attrInterp;
    Expr            gbyexpr;
    int             g;
    
    gbyNode = sfwNode.getGroupByClause();    
    Expr[] gbyexprArray = null;

    if (gbyNode != null) 
    {
      g = gbyNode.length;
      gbyexprArray = new Expr[g];
      
      for (int i=0; i<g; i++) 
      {
        attrInterp = InterpreterFactory.getInterpreter(gbyNode[i]);    
        attrInterp.interpretNode(gbyNode[i], ctx);
      
        gbyexpr = ctx.getExpr();
        
        //For Group by NULL case, throw not by group expr error
        if(gbyexpr instanceof ConstNullExpr)
          throw new SemanticException(SemanticError.NOT_A_GROUP_BY_EXPRESSION,
              gbyNode[i].getStartOffset(),
              gbyNode[i].getEndOffset(),
              new Object[]{gbyexpr.getName()});
        
        if(gbyexpr.getReturnType() == Datatype.XMLTYPE)
           throw new SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
                                       gbyNode[i].getStartOffset(),
                                       gbyNode[i].getEndOffset(),
                                       new Object[]{gbyexpr.getName()});
        gbyexprArray[i] = gbyexpr;

        //GroupByExpr is added in interpretSelectClause method
        //sfwQuery.addGroupByExpr(gbyexpr)
      }      
    }
    else 
      gbyexprArray = new Expr[0];
     
    ctx.setGbyExprs(gbyexprArray);
  }

  /**
   * Interpret the Having clause of a query
   * <p>
   * Converts the list of parsed conditions in Having clause to the
   * semantic forms
   */
  void interpretHavingClause(SemContext ctx, CEPSFWQueryNode sfwNode,
                             SFWQuery sfwQuery)
  throws CEPException {
    
    CEPBooleanExprNode condNode;
    NodeInterpreter    condInterp;

    condNode   = sfwNode.getHavingClause(); 
  
    if (condNode != null) {
      condInterp = InterpreterFactory.getInterpreter(condNode);    

      boolean isAggr;
      isAggr = ctx.isAggrAllowed();
      ctx.setIsAggrAllowed(true);
      
      condInterp.interpretNode(condNode, ctx);
      
      ctx.setIsAggrAllowed(isAggr);
      
      BExpr predicateExpr = (BExpr)ctx.getExpr();
      
      // update the query
      if(supportsPredicate(predicateExpr))
        sfwQuery.setHavingPredicate((BExpr)ctx.getExpr());
      else
        throw new CEPException(SemanticError.PREDICATE_CLAUSE_NOT_SUPPORTED,
            condNode.getStartOffset(), condNode.getEndOffset());
      
      ArrayList<Attr> havingExprList = new ArrayList<Attr>();     
      ArrayList<Expr> expr_r = new ArrayList<Expr>();     
      this.collectAttrs(sfwQuery.getHavingPredicate(), havingExprList);      
      Expr[] groupByExprs = ctx.getGbyExprs();
      
      if (groupByExprs.length != 0)   // GROUP BY clause is present
      {
        if(!exprContainsGbyExpr(groupByExprs, 
                                sfwQuery.getHavingPredicate(),
                                expr_r))
        {
          throw new SemanticException(SemanticError.NOT_A_GROUP_BY_EXPRESSION,
                                      condNode.getStartOffset(), 
                                      condNode.getEndOffset());
        }
        // It is not possible to know at this point (unlike select list with only
        // aggregates) whether an expression got rewritten or not.
        // For example for having predicates with only aggregates and constants
        // no rewrite will happen. 
 
        if(expr_r.size() != 0)
        {
          Expr expr = expr_r.get(0);
          assert expr instanceof BExpr;
          sfwQuery.setHavingPredicate((BExpr)expr); //setting the rewritten expr
        }
      }
      else
      {
        // NO group by clause present
        // Here havingExprList actually has the attributes referenced in 
        // non-aggregate functions. This is a good enough test in absence of
        // GROUP BY clause.
        // TODO: very with constants in having clause.
        if (havingExprList.size() != 0)
          throw new SemanticException(SemanticError.NOT_A_GROUP_BY_EXPRESSION,
            condNode.getStartOffset(), condNode.getEndOffset());
      }
    }
  }

  /**
   * converts a syntactic parsed form of the select clause to the semantic
   * representation of the select clause in the query object
   */
  void interpretSelectClause(SemContext ctx, CEPSFWQueryNode sfwNode,
          SFWQuery sfwQuery) 
  throws CEPException 
  {
    CEPSelectListNode selNode;
    NodeInterpreter   selInterp;
    boolean isAggrQuery = false;

    selNode = sfwNode.getSelectClause();
    selInterp = InterpreterFactory.getInterpreter(selNode);        
    selInterp.interpretNode(selNode, ctx);

    Expr[] selectExprs = ctx.getSelectList();
    Expr[] gbyExprs = ctx.getGbyExprs();

    if(selectExprs.length != 0)
    {
      ArrayList<Attr>     selectAttrs = new ArrayList<Attr>();
      ArrayList<AggrExpr> selectAggrs = new ArrayList<AggrExpr>();

      for(int i = 0; i < selectExprs.length; i++)
      {
        collectAttrs(selectExprs[i], selectAttrs);
        collectAggrs(selectExprs[i], selectAggrs);
      }

      if((selectAggrs.size() > 0) || (sfwNode.getHavingClause() != null) ||
            (gbyExprs.length > 0))
        isAggrQuery = true;

      // Set the isAggrExist flag to true if any aggregate in select list
      ctx.setAggrExist(isAggrQuery);

      int len = selectAttrs.size();

      //The 'not a singlegroup_group_function' error should be thrown only when
      // - SELECT list has at least on aggr and/or HAVING clause is present
      //   AND
      // - SELECT list refers at least one attr
      //   AND
      // - NO group by clause 
      if(isAggrQuery && (len != 0) && (gbyExprs.length == 0))
        throw new 
        SemanticException(SemanticError.NOT_A_SINGLEGROUP_GROUP_FUNCTION,
         selNode.getStartOffset(), selNode.getEndOffset());

      if ( gbyExprs.length > 0) 
      {
        for (int i = 0; i < selectExprs.length; i++) 
        {
         /*
          * TODO: This is the most important check. Put more
          * comments. Don't peek into SELECT items with Aggr functions;
          */
          ArrayList<Expr> selExpr_r = new ArrayList<Expr>();

          if (!exprContainsGbyExpr(gbyExprs, selectExprs[i], selExpr_r))
          {
            CEPParseTreeNode node = null;
            CEPExprNode[] selectList = selNode.getSelectListExprs();
            if(selectList != null && selectList.length < i)
              node = selectList[i];
            else 
              node = selNode;
             throw new SemanticException(
                SemanticError.NOT_A_GROUP_BY_EXPRESSION,
                 node.getStartOffset(),node.getEndOffset());
          }

         // Aggregates are not rewritten as GroupByExpr as in GROUP
         // BY clause
         // aggregate or grouping functions are not allowed.
         // No need to wrap the AttrExpr as GroupExpr
         if (!(selectExprs[i] instanceof AggrExpr)) 
         {
           // it is possible that it is a complex expr composed of constants
           // (which is valid). In that case we don't rewrite the expression
           ArrayList<Attr> attrs = new ArrayList<Attr>();
           collectAttrs(selectExprs[i], attrs);
           if (attrs.size() != 0) // there are at least some attributes
           {
             assert selExpr_r.size() == 1;
             selectExprs[i] = selExpr_r.remove(0); // rewritten expr
           }
         } 
         else 
         {
           // nothing to do for aggr, so it should not have been rewritten.
           assert selExpr_r.size() == 0;
         }
        }

        // .. now rewrite GROUP BY expressions as GroupByExpr
        // Ideally no need convert all of them, only the ones referred in
        // the SELECT clause. 
        // TODO: 
        //  1. optimize later.
        //  2. Could this be moved to a better place?    
        for (int g=0; g < gbyExprs.length; g++)
        {
          if( gbyExprs[g] instanceof AttrExpr)
          {
            sfwQuery.addGroupByExpr(gbyExprs[g]);
          }
          else
          {
            GroupByExpr gbyexpr_r = new GroupByExpr(gbyExprs[g]);
            sfwQuery.addGroupByExpr(gbyexpr_r); // add it to SFWQuery.
          }
        }        
      }
    }

    // update semantic representation of the query
    if (gbyExprs.length != 0) // valid group by expression in this query
    {
       sfwQuery.setSelectList(selectExprs);
    }
    else 
      sfwQuery.setSelectList(ctx.getSelectList()); // create a new copy

  sfwQuery.setIsDistinct(ctx.isDistinct());
}

  
  /**
   * Interpret Order by Expressions in the query
   * @param ctx Semantic Context
   * @throws CEPException throws Exception when order by expression
   *                      is not valid
   */
  void interpretOrderByClause(SemContext ctx, CEPSFWQueryNode sfwNode, 
                              SFWQuery sfwQuery)
  throws CEPException
  {
    CEPOrderByNode  orderByNode      = sfwNode.getOrderByClause();
    NodeInterpreter orderByNodeInterp;
    
    // if there is no order-by clause in query; orderByNode will be null
    if(orderByNode != null)
    {
      orderByNodeInterp = InterpreterFactory.getInterpreter(orderByNode);
      orderByNodeInterp.interpretNode(orderByNode, ctx);
      
     // Condition: If there is any group by attributes; then only 
      // 1) group by attributes can be used as partition attributes
      // 2) group by attributes can be used as order by expressions
      
      ArrayList<Expr> partitionByAttrs = ctx.getPartitionByAttrs();
      ArrayList<Expr> orderByExprs     = ctx.getOrderByExprs();
      
      // Get array of GroupBy attributes from SemContext
      Expr[] gbyExprArray = ctx.getGbyExprs();
      
          
      // Condition: If there are group by attributes; then
      // Order by expression and Partition by attributes must be a subset of
      // group by attributes
      boolean isAggrExist = ctx.isAggrExist();
      
      if(isAggrExist)
      {
        ArrayList<Attr> finalAttrList = new ArrayList<Attr>();
        
        // Collect all attributes in partition by and order by expressions
        if(orderByExprs != null)
        {          
          for(int i = 0; i < orderByExprs.size(); i++)
            collectAttrs(orderByExprs.get(i), finalAttrList);
        }
        
        if(partitionByAttrs != null)
        {
          for(int i = 0; i < partitionByAttrs.size(); i++)
            collectAttrs(partitionByAttrs.get(i), finalAttrList);
        }
       
        if(gbyExprArray == null || gbyExprArray.length == 0)
        {
          if(finalAttrList.size() != 0 && ctx.getNumOrderByRows() != 0)
            throw new SemanticException(
              SemanticError.NOT_A_GROUP_BY_EXPRESSION,
              orderByNode.getStartOffset(), orderByNode.getEndOffset());
        }
        else
        {
          for(int i = 0 ; i < finalAttrList.size() ; i++)
          {
            if(!gbyAttrsContain(gbyExprArray, finalAttrList.get(i)))
              throw new SemanticException(
                SemanticError.NOT_A_GROUP_BY_EXPRESSION,
                orderByNode.getStartOffset(), 
                orderByNode.getEndOffset());
          }
        }
          
      } // Check Complete if GroupByAttr is non-empty
      
      // Update the query
      sfwQuery.setOrderByList(ctx.getOrderByExprs());
      sfwQuery.setNumOrderByRows(ctx.getNumOrderByRows());
      sfwQuery.setPartitionByAttrs(ctx.getPartitionByAttrs());      
    }
  }
  
  /**
   * collect all the attributes present in the select/having expressions
   * do not visit aggregate param exprs
   * @param expr select/having expr
   * @param attrs
   */
  private void collectAttrs(Expr expr, ArrayList<Attr>attrs)
  {
    expr.getAllReferencedAttrs(attrs, SemAttrType.NAMED, false);
  }
  
  /**
   * collect all the aggr exprs present in the select/having expressions
   * @param expr select/having expr
   * @param aggrs list of aggrexprs
   */
  private void collectAggrs(Expr expr, ArrayList<AggrExpr>aggrs)
  {
    expr.getAllReferencedAggrs(aggrs);
  }
  
  /**
   * checks if the attr is present in the group by expressions. 
   * The attr is wrapped as AttrExpr expression in group by.  
   * gbyExprArray contains the group by expressions 
   * @param a attr
   * @return
   */
  private boolean gbyAttrsContain(Expr[] gbyExprArray, Attr a)
  {
    int len = gbyExprArray.length;
    for(int i = 0; i < len; i++) 
    {
      //Verify only for attributes
      if (gbyExprArray[i] instanceof AttrExpr && 
    	  ((AttrExpr)gbyExprArray[i]).getAttr().getAttrId() == a.getAttrId() && 
          ((AttrExpr)gbyExprArray[i]).getAttr().getVarId() == a.getVarId())
        return true;
    }
    return false;
  }
  
  /**
   * checks if the expression passed in contains group by expr
   * @param gbyExprs GROUP BY expression array
   * @param expr     SELECT list expression
   * @return true    if select list contains a group by expr
   *                 if TRUE select expression will be rewritten to use
   *                 GroupByExpr
   */
  private boolean exprContainsGbyExpr(Expr[] gbyExprs, 
                                      Expr expr,
                                      ArrayList<Expr> selExpr_r)
  {
    Expr reWrittenExpr = null;
    if (!(expr instanceof AggrExpr))
    {
      reWrittenExpr = expr.getRewrittenExprForGroupBy(gbyExprs);
      if( reWrittenExpr != null)
      {
        selExpr_r.add(reWrittenExpr);
        return true;
      }
      else
        return false;
    }
    return true;
 }
  
  /**
   * Check if predicate clause is supported
   * Each clause should follow the condition below:
   *  If Clause refers to an external relation; It should be an attribute.
   * @param predicateExpr the predicate Clause
   * @return       
   */
  private boolean supportsPredicate(BExpr predicateExpr)
  {
    BaseBExpr tempBaseBExpr;
    ComplexBExpr tempComplexBExpr;
    boolean  supportsPredicate = true;
    if(predicateExpr instanceof BaseBExpr)
    {
      tempBaseBExpr = (BaseBExpr)predicateExpr;
      //Check if BaseBExpr is unary expression
      if(tempBaseBExpr.getUnaryOp() != null)
      {
        // If Base Boolean expression is a unary expression
        //   then expression should not refer to any external relation
        // e.g. !(c1) is not allowed if c1 is an external attribute expression
        supportsPredicate = supportsPredicate &&
          !tempBaseBExpr.getUnaryOperand().isExternal;
        
      }
      // else - no need to check for external relations
      // because the predicate evaluation happens in CQLEngine
    }
    else if(predicateExpr instanceof ComplexBExpr)
    {
      tempComplexBExpr = (ComplexBExpr)predicateExpr;
      // If Complex Boolean Expression refers to external relation
      //   then it should contain only AND operator.
      // e.g. S.c1 = Ext.c1 and S.c2 = Ext.d2 is allowed
      // e.g. S.c1 = Ext.c1 or S.c2 = Ext.d2 is not allowed
      // e.g. not (S.c1 = Ext.c1) is not allowed
      
      if(tempComplexBExpr.isExternal)
        return tempComplexBExpr.getLogicalOp() == oracle.cep.common.LogicalOp.AND;
        
      // There is no need to check Complex Unary Expressions
      // As we are not allowing any unary operation over a complex expression
      // which refers to external relation in some way.
      // Another case; If expression does not refer to external relation
      // then there is no need to check further.
      
      // If Left or Right operand of Complex Boolean Expression is instance of
      // Boolean expression; then check whether CEP supports it or not.
      // else 
      // If operand refers to external relation; then it should be an instance
      // of AttrExpr; No other complex expressions will be allowed in this case
      
      // Check Left Operand
      if(tempComplexBExpr.getLeftOperand() instanceof BExpr)
      {
        supportsPredicate = supportsPredicate &&
          supportsPredicate((BExpr)tempComplexBExpr.getLeftOperand());
      }
      else if(tempComplexBExpr.getLeftOperand().isExternal)
      {
        supportsPredicate = supportsPredicate &&
          tempComplexBExpr.getLeftOperand() instanceof AttrExpr;
      }
      if(tempComplexBExpr.getRightOperand() != null)
      {    
        // Check Right Operand 
        if(tempComplexBExpr.getRightOperand() instanceof BExpr)
        {
          supportsPredicate = supportsPredicate &&
            supportsPredicate((BExpr)tempComplexBExpr.getRightOperand());
        }
        else if(tempComplexBExpr.getRightOperand().isExternal)
        { 
          supportsPredicate = supportsPredicate &&
            tempComplexBExpr.getRightOperand() instanceof AttrExpr;
        }
      }
    }
    return supportsPredicate;
  }
}
