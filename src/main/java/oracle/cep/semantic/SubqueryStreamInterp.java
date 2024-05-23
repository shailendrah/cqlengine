/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SubqueryStreamInterp.java /main/7 2014/10/14 06:35:33 udeshmuk Exp $ */

/* Copyright (c) 2011, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/18/14 - set partitioned stream context
    vikshukl    04/07/13 - subquery and views: fix dependency
    udeshmuk    10/03/11 - set ctx.isArchived to true if subquery refers
                           archived relation
    vikshukl    09/19/11 - subquery and value window
    vikshukl    09/01/11 - deal with derived timestamp
    vikshukl    08/25/11 - add alias to subquery spec
    vikshukl    07/11/11 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SubqueryStreamInterp.java /main/7 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */



package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPQueryNode;
import oracle.cep.parser.CEPStreamSubqueryNode;
import oracle.cep.service.ExecContext;
import oracle.cep.common.RelToStrOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 * The interpreter that is specific to the CEPStreamSubqueryNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class SubqueryStreamInterp extends NodeInterpreter 
{
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
  throws CEPException 
  {
    CEPStreamSubqueryNode streamNode;
    CEPQueryNode          queryNode;
    NodeInterpreter       queryInterp;
    int                   inlineviewid;
    
    assert node instanceof CEPStreamSubqueryNode;
    streamNode = (CEPStreamSubqueryNode)node;
    ExecContext ec = ctx.getExecContext();
    
    // interpret the parent node first
    super.interpretNode(node, ctx);
        
    // stream is derived from an inline subquery. 
    // push a new scope (SemContext and symbol table)
    // Note that variants of SemQuery will get created as part interpreting the
    // subquery itself and hence there is no need to create it explicitly.    
    SemContext newctx = new SemContext(ec);
    newctx.setQueryObj(ctx.getQueryObj()); // indirectly add dependency on the root parent
    newctx.setSchema(ctx.getSchema());
    SymbolTable oldsymtab = ctx.getSymbolTable();
    SymbolTable newsymtab = new SymbolTable();
    newctx.setSymbolTable(newsymtab);
    
    // now interpret the query
    // this should work for all types of queries
    // i.e. SFW, binary setop and n-ary setop
    queryNode = streamNode.getQuery();
    queryInterp = InterpreterFactory.getInterpreter(queryNode);
    queryInterp.interpretNode(queryNode, newctx);
    
    /* check that the query evaluates to a stream? This check almost identical
     * to the one done in SemanticInterpreter.java
     */
    SemQuery semQuery = newctx.getSemQuery();
    if (!(semQuery.isStreamQuery())) {
      if (semQuery.isMonotonicRel(ec)) {
        semQuery.setR2SOp(RelToStrOp.ISTREAM);
      }
    }   
       
    // restore symbol table
    ctx.setSymbolTable(oldsymtab);
    ctx.getSemQuery().setSymTable(oldsymtab);
    
    // To alias or not to alias? 
    // Without aliasing, subqueries are of limited interest and usage.
    // The forms that can be expressed are:
    // a. SELECT * FROM (...)
    // b. SELECT count(*) FROM (...)
    // c. SELECT f(c2), c1, g(c3) FROM (select * from S);  S={c1,c2,c3}
    // d. SELECT c1, c2 FROM (select <expr> as c1, <expr> as c2 from S)
    // e. SELECT * from (...), (...) -- cross join can't select individual 
    //                                  fields unless uniquely aliased.
    // For (e) a join can be specified only if join columns are uniquely
    // identifiable.
    // The problem arises when putting these entries in symbol table 
    // when there are multiple anon subqueries. One solution could be to
    // use system-generated name like SUBQ_SYS_<number>
    
    // not allowing subqueries without aliases for now.    
    if (streamNode.getAlias() != null) {
      try 
      {
        inlineviewid = 
          ctx.getSymbolTable().addInlineSourceEntry(streamNode.getAlias(),
              streamNode.getAlias(), 
              newctx.getSemQuery().isStreamQuery());              
      }
      catch (CEPException ce) 
      {
        ce.setStartOffset(streamNode.getStartOffset());
        ce.setEndOffset(streamNode.getEndOffset());
        throw ce;
      }
    }
    else 
    {
      throw new 
        SemanticException(SemanticError.SUBQUERY_ALIAS_NOT_PROVIDED,
                          streamNode.getStartOffset(), 
                          streamNode.getEndOffset());                         
    }
         
    // add the project list of the subquery to this inline view 
    Expr[] subqSelectList = newctx.getSelectList();
    Expr selExpr;
    String attrName;
    for (int k=0; k < subqSelectList.length; k++)
    {            
      selExpr = subqSelectList[k];            
      if (selExpr.getExprType() != ExprType.E_ATTR_REF) 
      {
        if (selExpr.isUserSpecifiedName()) 
        {
          attrName = selExpr.getName(); // non-attribute must be aliased
        }
        else  
        {
          // Today a user can't create a view like
          // create view v0 max(c1) from S without aliasing the SELECT
          // item. (alas the exact location of select item is gone).
          throw new 
          SemanticException(SemanticError.SUBQUERY_SELECT_EXPR_NOT_ALIASED,
              streamNode.getStartOffset(), streamNode.getEndOffset(),
              new Object[]{k+1});
        }
      }
      else 
      {
        // need to extract the select list name:
        // SELECT * from S; or SELECT c1, c2, c3 from S;(S0.c1, S0..c3)
        attrName = selExpr.getName();
        attrName = attrName.substring(attrName.lastIndexOf('.') + 1);
      }
        
      try 
      {
        // add entries from the subquery to the parent symbol table  
        ctx.getSymbolTable().addAttrEntry(attrName, 
                                          streamNode.getAlias(), k,
                                          selExpr.getReturnType(), 
                                          0);
      }
      catch (CEPException ce)
      {
        ce.setStartOffset(streamNode.getStartOffset());
        ce.setEndOffset(streamNode.getEndOffset());
        throw ce;
      }            
    } /* end: copy select list */
    
    // Construct a subquery spec and stash in the outer context.
    // Not sure about symbol table for now, remove later if not needed.
    SubquerySpec subquerySpec = new SubquerySpec(inlineviewid,
                                                 streamNode.getAlias(),
                                                 newsymtab,
                                                 newctx.getSemQuery());
    ctx.setSubquerySpec(subquerySpec);
    
    // does the subquery contain an archived relation?
    // Even though the subquery evaluates to a STREAM, it can refer
    // archived relations in its from clause. There could be rel-to-stream
    // operator like ISTREAM on top because of which the subquery evaluates
    // to a stream
    if (semQuery.isDependentOnArchivedRelation()) {
      ctx.setIsArchived(true);
    }

    if(semQuery.isDependentOnPartnStream()) {
      ctx.setIsPartnStream(true);
    }
    
    // #(14025832): add dependent tables/views in the subquery to the outer
    // query, which causes the required views to be started before starting
    // the query.
    for (int tid : newctx.getSemQuery().getReferencedTables())
      ctx.getSemQuery().addReferencedTable(tid);

    // The reason to defer throwing this exception is value windows
    // over relations. If it is eagerly thrown then the symbol table is not
    // populated and semantic analysis fails.
    if (!(semQuery.isStreamQuery()))
      throw new SemanticException(SemanticError.NOT_A_STREAM_ERROR, 
          queryNode.getStartOffset(),
          queryNode.getEndOffset(),
          new Object[] {queryNode});      

    
  }
}
