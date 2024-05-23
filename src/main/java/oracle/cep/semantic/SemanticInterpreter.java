/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SemanticInterpreter.java /main/13 2013/01/17 09:23:51 udeshmuk Exp $ */
/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   The semantic analysis module

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    01/15/13 - XbranchMerge udeshmuk_bug-15962424_ps6 from
                           st_pcbpel_11.1.1.4.0
    anasrini    03/23/11 - support for PARTITION_ORDERED
    parujain    01/28/09 - transaction mgmt
    hopark      11/17/08 - set transaction in context
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/15/08 - multiple schema support
    parujain    08/26/08 - semantic exception offset
    sbishnoi    12/18/07 - cleanup
    sbishnoi    12/04/07 - support for update semantics
    parujain    01/31/07 - drop function
    rkomurav    01/13/07 - call toString
    anasrini    06/02/06 - support for syntax shortcuts 
    najain      04/06/06 - cleanup
    anasrini    02/24/06 - add javadoc comments 
    anasrini    02/15/06 - Implementation of interpretQuery 
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
    anasrini    02/13/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SemanticInterpreter.java /main/13 2013/01/17 09:23:51 udeshmuk Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.common.RelToStrOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.Query;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPQueryNode;
import oracle.cep.service.ExecContext;


/**
 * The semantic analysis module.
 * <p>
 * This represents the second phase of converting the query string 
 * into the actual execution units:
 * 
 * CQL query string --> parse tree representation ---> Query ---> LogOp tree
 *  --> PhyOp tree ---> Execution units .
 *
 * @since 1.0
 */

public class SemanticInterpreter {
  private SymbolTable  symTable;

  /**
   * Constructor for the semantic analyser
   */
  public SemanticInterpreter() {
    this.symTable = new SymbolTable();
  }

  /**
   * The "main" semantic analysis method
   *<p>
   * Converts a syntactic parsed form of a query into a "semantic"
   * representation (SemQuery object). 
   */
  public SemQuery interpretQuery(ExecContext ec, Query q,
                                 CEPParseTreeNode parseTree) 
    throws CEPException {
    
    SemContext      semctx = new SemContext(ec);
    CEPQueryNode    queryNode;
    NodeInterpreter queryInterp;
    SemQuery        semQuery;

    if ((parseTree == null) || !(parseTree instanceof CEPQueryNode))
      throw new IllegalArgumentException(parseTree.getClass().getName());

    queryNode = (CEPQueryNode)parseTree;

    // Reset the symbol table since we are about to begin a fresh
    // interpretation
    symTable.reset();

    // set up the semantic analysis global context
    semctx.setSymbolTable(symTable);
    
    //set the query metadata object being referred
    semctx.setQueryObj(q);
    
    // set the schema under which the query is getting registered
    semctx.setSchema(q.getSchema());

    // Get the parse tree node specific interpreter
    queryInterp = InterpreterFactory.getInterpreter(queryNode);

    // Perform the analysis
    queryInterp.interpretNode(queryNode, semctx);

    semQuery = semctx.getSemQuery();
    
    if (!(semQuery.isStreamQuery())) 
    {
      if (semQuery.isMonotonicRel(ec)) 
      {
        semQuery.setR2SOp(RelToStrOp.ISTREAM);
      }
    }

    // Interpret the partition parallel expression associated with this 
    // query (only if it exists)
    CEPExprNode partitionParallelExprNode = q.getPartitionParallelExprNode();
    if (partitionParallelExprNode != null)
    {
      semctx.setInterpretingPartnExpr(true);
      SymbolTable oldSymbolTable = null;
      NodeInterpreter exprInterp = 
        InterpreterFactory.getInterpreter(partitionParallelExprNode);
      if(semQuery.getPatternInlineViewSymTable() != null)
      {
        oldSymbolTable = semctx.getSymbolTable();
        semctx.setSymbolTable(semQuery.getPatternInlineViewSymTable());
      }
      exprInterp.interpretNode(partitionParallelExprNode, semctx);
      semQuery.setPartitionParallelExpr(semctx.getExpr());

      LogUtil.fine(LoggerType.TRACE,
                   "Semantic form of partition parallel expression for "
                   + q.getName() + " is " 
                   + semQuery.getPartitionParallelExpr());
      
      //restore old symbol table
      if(oldSymbolTable != null)
      {
        semctx.setSymbolTable(oldSymbolTable);
        oldSymbolTable = null;
      }
      semctx.setInterpretingPartnExpr(false);
    }
    
    // If a Query contains a primary key then
    // It should evaluates to a relation only
    if(semQuery.getIsPrimaryKeyExists() && semQuery.isStreamQuery())
      throw new CEPException(SemanticError.STREAM_NOT_ALLOWED_HERE);
        
    StringBuilder sb = new StringBuilder();
    sb.append(semQuery.toString());
    
    return semQuery;
  }

}
