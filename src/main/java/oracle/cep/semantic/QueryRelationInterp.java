/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/QueryRelationInterp.java /main/3 2012/08/09 00:10:57 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The interpreter for the CEPQueryRelationNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/02/12 - support evaluate clause
    anasrini    05/23/07 - symbol table reorg
    najain      04/06/06 - cleanup
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/QueryRelationInterp.java /main/3 2012/08/09 00:10:57 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPQueryRelationNode;
import oracle.cep.parser.CEPSFWQueryNode;
import oracle.cep.parser.CEPSlideExprNode;
import oracle.cep.exceptions.CEPException;

/**
 * The interpreter that is specific to the CEPQueryRelationNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

abstract class QueryRelationInterp extends NodeInterpreter 
{

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException 
  {

    super.interpretNode(node, ctx);
    
    assert node instanceof CEPQueryRelationNode;
    
    CEPSlideExprNode evaluateExprNode 
      = (CEPSlideExprNode) ((CEPQueryRelationNode)(node)).getEvaluateClause();
    
    interpretEvaluateClause(ctx, evaluateExprNode); 
  }
  
  /**
   * Interpret the optional evaluate clause
   * @param ctx
   * @param sfwNode
   * @param sfwQuery
   * @throws CEPException
   */
  private void interpretEvaluateClause(SemContext ctx,  
                                       CEPSlideExprNode evaluateExprNode) 
    throws CEPException
  {
    if(evaluateExprNode != null)
    { 
      // Get the query node specific interpreter
      NodeInterpreter queryInterp =
        InterpreterFactory.getInterpreter(evaluateExprNode);

      // Perform the semantic analysis
      queryInterp.interpretNode(evaluateExprNode, ctx);
    }
  }

}
