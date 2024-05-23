/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/GenericSetOpInterp.java /main/8 2014/10/14 06:35:33 udeshmuk Exp $ */

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
    udeshmuk    09/18/14 - set partitioned stream flag
    sbishnoi    12/23/13 - bug 17600010
    pkali       04/03/12 - included datatype arg in Attr instance
    udeshmuk    07/12/11 - support for archived relation
    udeshmuk    04/01/11 - store name of attr
    vikshukl    03/07/11 - support n-ary set operators
    vikshukl    03/07/11 - Creation
 */
package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.HashMap;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPGenericSetOpNode;
import oracle.cep.parser.CEPSetOpNode;
import oracle.cep.service.ExecContext;
import oracle.cep.common.Constants;
import oracle.cep.common.RelSetOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.QueryManager;
import oracle.cep.metadata.Table;
import oracle.cep.common.Datatype;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/GenericSetOpInterp.java /main/8 2014/10/14 06:35:33 udeshmuk Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

class GenericSetOpInterp extends QueryRelationInterp {
        
  void interpretNode(CEPParseTreeNode node, SemContext ctx)
      throws CEPException
  { 
    int numAttrs;
    Datatype attrType;
    Expr attrExpr;
    String attrName;
    int left, right;
    boolean isExternal = false, isLeftStream;
    boolean isDependentOnArchivedReln = false;
    boolean isDependentOnPartnStream  = false;
    
    // List of table names referenced by the parser node
    ArrayList<String> tables = new ArrayList<String>();
    
    // List of table identifiers referenced by the parser node
    ArrayList<Integer> listTableIds = new ArrayList<Integer>();
    
    // List of derived timestamp expressions corresponding to the table which are
    // defined as derived timestamped; The list entry will be null if the table
    // is not derived timestamped
    ArrayList<String> derivedTsExprs = new ArrayList<String>();
    
    ExecContext ec = ctx.getExecContext();
    assert node instanceof CEPGenericSetOpNode;
    CEPGenericSetOpNode setNode = (CEPGenericSetOpNode)node;
    
    /* post semantic presentation of the query for later phases */
    GenericSetOpQuery setQuery = new GenericSetOpQuery();
    ctx.setSemQuery(setQuery);
    setQuery.setSymTable(ctx.getSymbolTable());
                
    /* first interpret the parent nodes */
    super.interpretNode(node, ctx);
                
    /* extract information about other sources in this n-ary set operation.
     * After the call to registerTable() setQuery is set with 
     * appropriate metadata for each of the tables/views involved.
     * It also records the type of set operation and whether it is a UnionAll
     * if it is a UNION operator. 
     */
    registerTables(ctx, setNode, setQuery);
    
    /* check that attributes of operands are compatible */
    left = setQuery.getTableId(setQuery.getLeft());
    
    // Get the name of table using table id
    String leftTableName = setQuery.getTableName(setQuery.getLeft());
    
    // Update the lists of table names and table identifiers
    tables.add(leftTableName);
    listTableIds.add(left);

    // Add the derived timstamp expression into appropriate list from Table obj
    boolean isTable = ec.getSourceMgr().isTableObject(left);
    Table table = null;
    if(isTable)
    {
      table = ec.getTableMgr().getTable(left);
      derivedTsExprs.add(table.getDerivedTs());
    }
    else
      derivedTsExprs.add(null);
    
    isLeftStream = ec.getSourceMgr().isStream(left);
    isDependentOnArchivedReln = ec.getSourceMgr().isArchived(left);
    isDependentOnPartnStream  = ec.getSourceMgr().isPartnStream(left);
                
    /* IN and NOT IN are strictly not set operators and are not enabled
     * at parser level itself for participation in N-ary operation.
     * Check that the schema of the operands involved is compatible. 
     */
    try
    {
      numAttrs = ec.getSourceMgr().getNumAttrs(left);
      Datatype leftAttrType;
      Datatype rightAttrType;     
      
      for (int i = 0; i < setQuery.getOperands().size(); i++)
      {
        right = setQuery.getTableId(setQuery.getOperands().get(i));
        
        isTable = ec.getSourceMgr().isTableObject(right);
        if(isTable)
        {
          table = ec.getTableMgr().getTable(right);
          // Add the derived timestamp expression into appropriate list from Table
          // object
          derivedTsExprs.add(table.getDerivedTs());
        }
        else
          derivedTsExprs.add(null);
        
        // Get the name of table using table id
        String rightTableName 
          = setQuery.getTableName(setQuery.getOperands().get(i));
        
        // Update the list of table names and identifiers
        tables.add(rightTableName);
        listTableIds.add(right);
        
       

        
        // a. first check that number of attributes match.
        if (numAttrs != 
            ec.getSourceMgr().getNumAttrs(right))
        {
          throw new 
              SemanticException(SemanticError.NUMBER_OF_ATTRIBUTES_MISMATCH,
                                setNode.getStartOffset(), 
                                setNode.getEndOffset(),
                                new Object[]
                                {setNode.getLeftRelation(),
                                 setNode.getRightRelations().get(i).getRight()});
        }
                                
        // b. now check that the schemas match
        for (int j=0; j < numAttrs; j++)
        {
          leftAttrType  = ec.getSourceMgr().getAttrType(left, j);         
          rightAttrType = ec.getSourceMgr().getAttrType(right, j);
          
          /* FIXME: This is a very strict check. Type conversion/coercion
           * should be tried when possible.
           * For example, Oracle RDBMS allows the following:
           * create table a (c1 integer, c2 float);
           * SELECT c1, c2 FROM a UNION SELECT c2, c1 FROM a
           */ 
          if (!leftAttrType.equals(rightAttrType))
          {
            throw new 
                SemanticException(SemanticError.SCHEMA_MISMATCH_IN_SETOP,
                                  setNode.getStartOffset(),  
                                  setNode.getEndOffset(),
                                  new Object[] {j+1, setNode.getLeftRelation(), 
                               setNode.getRightRelations().get(i).getRight()});
          }
                
          // For non UNION-ALL XML type is not allowed.
          if (!setQuery.getIsUnionAll().get(i) &&
              (leftAttrType == Datatype.XMLTYPE))     
          {
            throw new 
                SemanticException(SemanticError.INVALID_XMLTYPE_USAGE,
                                  setNode.getStartOffset(), 
                                  setNode.getEndOffset(), 
                                  new Object[]{ec.getSourceMgr().
                                               getAttrName(left, j)});
          }        
        }
              
                
        if (setQuery.getIsUnionAll().get(i)) 
        {
          // FIXME: ask Anand why?
          // for "UNION ALL" operations, the case of BOTH stream operands is
          // allowed, other cases are not allowed.                        
          if (isLeftStream && (!(ec.getSourceMgr().isStream(right))))
          {
            throw new SemanticException(SemanticError.INVALID_UNION_ALL_SET_OP, 
                                        setNode.getStartOffset(), setNode.getEndOffset(),
                                        new Object[]{setNode.getRightRelations().get(i).getRight()});
          }
          if (!isLeftStream && (ec.getSourceMgr().isStream(right)))
          {
            throw new SemanticException(SemanticError.INVALID_UNION_ALL_SET_OP, 
                                        setNode.getStartOffset(), setNode.getEndOffset(),
                                        new Object[]{setNode.getLeftRelation()});
          }
        }
        else 
        {
          if (isLeftStream) 
          {
            throw new
                SemanticException(SemanticError.NOT_A_RELATION_ERROR,
                                  setNode.getStartOffset(), setNode.getEndOffset(),
                                  new Object[]{setNode.getLeftRelation()});
          }
          if (ec.getSourceMgr().isStream(right))
          {
            throw new 
                SemanticException(SemanticError.NOT_A_RELATION_ERROR, 
                                  setNode.getStartOffset(), setNode.getEndOffset(),
                                  new Object[]{setNode.getRightRelations().get(i).getRight()});                                                
          }
        }               
                                                                
        // is any of the relation an external relation?
        isExternal = (ec.getSourceMgr().isExternal(left) ||
                      ec.getSourceMgr().isExternal(right));                   
      }

      // Construct a temporary CQL query to get the semantic analysis of the 
      // derived timestamp expression;
      // Reason:
      // Semantic analysis of Derived timestamp expression is first done with 
      // CREATE STREAM DDL; The analysis is done by using a temporary DDL
      // select expression from source;
      // This will not be feasible for the scenario
      // select * from (source1 union source2) and both sources are derived
      // timestamped. Because previously analyzed derived timestamp expression
      // was analyzed only for one source.
      // To solve this issue, we have to analyze the derived timestamp expressions
      // for all sources;
      // For that we will construct a DDL based on join of all sources as follows:
      // select expr1, expr2 from source1[now], source2[now]
      // Note: NOW window is used as unbounded streams cant be joined.
      
      // The temporary query's name will be source1_source2
      
      
      StringBuffer cqlDDL = new StringBuffer();
      cqlDDL.append("select ");
      boolean isCommaRequired = false;
      StringBuffer qName = new StringBuffer(Constants.CQL_RESERVED_PREFIX);
      boolean hasDerivedTsExprs = false;
      for(String derivedTsExpr: derivedTsExprs)
      {
        if(isCommaRequired && derivedTsExpr != null)
        {
          cqlDDL.append(" ," + derivedTsExpr);
          hasDerivedTsExprs = true;
        }
        else if(derivedTsExpr != null)
        {
          cqlDDL.append(derivedTsExpr);
          isCommaRequired = true;
          hasDerivedTsExprs = true;
        }
      }
      if(hasDerivedTsExprs)
      {
        cqlDDL.append(" from ");
        isCommaRequired = false;
        for(String tableName: tables)
        {
          if(isCommaRequired && tableName != null)
          {
            cqlDDL.append(" ," + tableName + "[now]");
            qName.append(tableName + "_");
          }
          else if(tableName != null)
          {
            cqlDDL.append(tableName + "[now]");
            qName.append(tableName + "_");
            isCommaRequired = true;
          }
        }
        String cqlDDLStr = cqlDDL.toString();
        
        LogUtil.fine(LoggerType.TRACE, "Evaluating derived timestamp expressions" + 
          " using CQL query : " + cqlDDLStr);
  
        // Get the derived timestamped semantic expressions
        ArrayList<Expr> derivedTsSemExprs = 
            getDerivedTimeExprs(cqlDDL.toString(), ec, qName.toString());
        
        // Populate the map of table identifiers to derived ts semantic expressions 
        // inside SemQuery
        int derivedTsIndex = 0;
        int nextIndex = 0;
        Expr derivedTsSemExpr = null;
        for(String derivedTsExpr: derivedTsExprs)
        {
          if(derivedTsExpr != null)
          {
            derivedTsSemExpr = derivedTsSemExprs.get(derivedTsIndex);
            int tableId = listTableIds.get(nextIndex);
            DerivedTimeSpec dTSpec = new DerivedTimeSpec(tableId, derivedTsSemExpr);
            setQuery.setDerivedTsSpec(tableId, dTSpec);
            derivedTsIndex++;
          }
          nextIndex++;
        }
      }
      
      //set isDependentOnArchivedReln to true if any of the inputs is archived
      if(!isDependentOnArchivedReln)
      {
        for(int i = 0; i < setQuery.getOperands().size(); i++)
        {
          int tableId = setQuery.getTableId(setQuery.getOperands().get(i));
          if(ec.getSourceMgr().isArchived(tableId))
          {
            isDependentOnArchivedReln = true;
            break;
          }
        }
      }
      setQuery.setIsDependentOnArchivedRelation(isDependentOnArchivedReln);

      //set isDependentOnPartnStream to true if any of the inputs is partition stream
      if(!isDependentOnPartnStream)
      {
        for(int i=0; i < setQuery.getOperands().size(); i++)
        {
          int tableId = setQuery.getTableId(setQuery.getOperands().get(i));
	  if(ec.getSourceMgr().isPartnStream(tableId))
          {
            isDependentOnPartnStream = true;
            break;
          }
        }
      }
      setQuery.setIsDependentOnPartnStream(isDependentOnPartnStream); 
      
      // all semantic checks are done. now set up setQuery for later
      // phase, i.e., logical plan generation

      Attr[] leftSrcAttrs = new Attr[numAttrs];
      for (int i = 0; i < numAttrs; i++) {
        attrName = ec.getSourceMgr().getAttrName(left, i);
        attrType = ec.getSourceMgr().getAttrType(left, i);
        Attr attr= new Attr(left, i, setNode.getLeftRelation()+"."+attrName,
                              attrType);
        leftSrcAttrs[i] = attr;
        attrExpr = new AttrExpr(new Attr(0, i, setNode.
                           getLeftRelation()+"."+attrName, attrType), attrType);
        attrExpr.setName(setNode.getLeftRelation() + "." + attrName, 
                         false, isExternal);
        ctx.addSelectListExpr(attrExpr);
      }               
      setQuery.setSelectListExprs(ctx.getSelectList());
    }
    catch (MetadataException me)
    {
      me.setStartOffset(setNode.getStartOffset());
      me.setEndOffset(setNode.getEndOffset());
      throw me;                 
    }
  }

  private void registerTables(SemContext ctx, CEPGenericSetOpNode setNode,
                              GenericSetOpQuery setQuery)
  throws CEPException
  {
    String table;
    String view;
    int leftid = 0;
    int rightid = 0;
    RelSetOp op;
    boolean isUnionAll;     
    HashMap<String, Integer> localcache = new HashMap<String, Integer>();
    
    table = setNode.getLeftRelation();
    assert table != null;
    view = table;
    
    try {
      leftid = 
         ctx.getSymbolTable().addPersistentSourceEntry(ctx.getExecContext(), 
                                                       table, 
                                                       view, 
                                                       ctx.getSchema());
      
      // add it to local map
      localcache.put(table, leftid);
      
      setQuery.setLeft(leftid);
      setQuery.addReferencedTable(
                ctx.getSymbolTable().lookupSource(leftid).getTableId());                
    }
    catch (CEPException ce)
    {
      ce.setStartOffset(setNode.getStartOffset());
      ce.setEndOffset(setNode.getEndOffset());
      throw ce;
    }
                
    // now add rest of the operands and operators
    for (CEPSetOpNode node : setNode.getRightRelations()) 
    {
      table = node.getRight();
                
      assert table != null;
      view = table;
      op = node.getRelSetOp();
      isUnionAll = node.isUnionAll();
                                
      try 
      {
         rightid = 
             ctx.getSymbolTable().addPersistentSourceEntry(ctx.getExecContext(), 
                                                           table, 
                                                           view, 
                                                           ctx.getSchema());
         localcache.put(table,  rightid);
      }
      catch (CEPException e) {
        if (e.getErrorCode() != SemanticError.AMBIGUOUS_TABLE_ERROR)
        {
          e.setStartOffset(setNode.getStartOffset());
          e.setEndOffset(setNode.getEndOffset());
          throw e;
        }
        else {
          //duplicate entry, must be in our local cache.
          assert localcache.containsKey(table);
          rightid = localcache.get(table);
        }
      }
      
      setQuery.addOperand(rightid);
      setQuery.addOperator(op);
      setQuery.addIsUnionAll(isUnionAll);     
      setQuery.addReferencedTable(ctx.getSymbolTable().lookupSource(rightid).getTableId());
    }
    return;
  }
  
  /**
   * Get the derived timestamp semantic expressions 
   * @param qry temporary query ddl
   * @param ec
   * @param qryName Name of temporary query.
   * @return select list expressions which will be semantic represenation of 
   * derived timestamp expressions
   * @throws CEPException
   */
  private ArrayList<Expr> getDerivedTimeExprs(String qry, ExecContext ec, 
                                              String qryName)
    throws CEPException
  {
    ArrayList<Expr> output = null;
    QueryManager qryMgr = ec.getQueryMgr();
    int qryid = -1;
    try
    {
      // Add the query with given name and ddl
      qryid = qryMgr.addNamedQuery(qryName, qry, ec.getSchema(), null);
      
      // Fetch the select list expressions from query object
      Query query = qryMgr.getQuery(qryid);
      output = query.getSemQuery().getSelectListExprs();
      
      // Drop the query
      qryMgr.dropQuery(qryid);
    } 
    catch (CEPException ce)
    {
      LogUtil.fine(LoggerType.TRACE, "Internal Error:\n" + ce.getMessage() + 
        "\nCause:\n" + ce.getCauseMessage() + "\nAction:\n" + ce.getAction());
      throw new MetadataException(MetadataError.INVALID_DERIVED_TIMESTAMP,
                                  new Object[]{qry});
    }
    catch(Exception e)
    {
      // In case of post registration failure in addNamedQuery, 
      // Get the select list expression from query object 
      Query query = qryMgr.getQuery(qryid);
      output = query.getSemQuery().getSelectListExprs();
      
      // Drop the Query
      qryMgr.dropQuery(qryid);
    }
    return output;
  }
}
