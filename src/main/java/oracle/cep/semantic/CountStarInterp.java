/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/CountStarInterp.java /main/9 2012/05/02 03:06:02 pkali Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       04/03/12 - included datatype arg in Attr instance
    vikshukl    06/22/11 - subquery support
    udeshmuk    04/01/11 - store name of attr
    parujain    03/12/09 - make interpreters stateless
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    09/04/08 - maintain offsets
    parujain    08/27/08 - semantic offset
    parujain    11/09/07 - external source
    parujain    10/25/07 - if using ondemand
    udeshmuk    09/21/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/CountStarInterp.java /main/9 2012/05/02 03:06:02 pkali Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.AggrFunction;
import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.MetadataException;
import oracle.cep.parser.CEPAggrExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.service.ExecContext;

/**
 * The interpreter that is specific to the CEPCountStarNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 
 */

public class CountStarInterp extends AggrExprInterp {

  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx)
    throws CEPException {

    SymbolTableAttrEntry[] attrEntries;
    SymbolTableAttrEntry   attrEntry;
    SymbolTableEntry       tabEntry;
    SymbolTableSourceEntry srcEntry;
    SymTableEntryType      entryType;
    SFWQuery               sfwQuery;
    int[]                  varIds;
    int                    varId;
    Attr                   attr;
    int                    attrId;
    AttrExpr               expr = null;
    Datatype               paramType = null;
    String                 argName = null;
    boolean                external = false;
    
    super.interpretNode(node, ctx);
    AggrFunction   aggrFn = ((CEPAggrExprNode)node).getAggrFunction();

    // Count_star aggr. function is attribute independent.  So we make it
    // the count over first table, first attribute without changing
    // query semantics
    try
    {
      sfwQuery    = (SFWQuery)ctx.getSemQuery();
      varIds      = sfwQuery.getFromClauseTables();
      varId       = varIds[0];
      ExecContext ec = ctx.getExecContext();
      attrEntries = ctx.getSymbolTable().getAllAttrs(ec, varId);
      String tabName = ctx.getSymbolTable().getTableName(varId);
      
      attrEntry   = attrEntries[0];
      attrId      = attrEntry.getAttrId();
      paramType   = attrEntry.getAttrType();
      attr        = new Attr(varId, attrId, attrEntry.getVarName(), paramType);
      expr        = new AttrExpr(attr,paramType);
            
      // With subquery support we cannot lookup the metadata cache directly
      // Need to consult symbol table first as inline subqueries don't really
      // have ondisk or cache metadata.
      // Stolen from AttrInterp.java
      // FIXME: How would isExternal get set for subqueries?
      tabEntry = ctx.getSymbolTable().lookup(attrEntry.getInlineViewVarId());
      entryType = tabEntry.getEntryType();
      if (entryType == SymTableEntryType.SOURCE) 
      {
        srcEntry = (SymbolTableSourceEntry)tabEntry;        
        
        if (srcEntry.getSourceType() == SymbolTableSourceType.PERSISTENT)
        {
          try 
          {
            external = ec.getTableMgr().isExternal(tabName, ctx.getSchema());
          }
          catch (MetadataException me)
          {
            me.setStartOffset(node.getStartOffset());
            me.setEndOffset(node.getEndOffset());
            throw me;
          }
        }
        else if (srcEntry.getSourceType() == SymbolTableSourceType.INLINE_VIEW)
        {
          external = ((SymbolTableSourceEntry)tabEntry).isExternal();
        }
      }     

      expr.setIsExternal(external);
      expr.setActualName(tabName + "." + attrEntry.varName);
      argName     = "*";
    }
    catch(CEPException e)
    {
      e.setStartOffset(node.getStartOffset());
      e.setEndOffset(node.getEndOffset());
      throw e;
    }
    buildAggrExpr(ctx, expr, paramType, argName, aggrFn);
  }
}
