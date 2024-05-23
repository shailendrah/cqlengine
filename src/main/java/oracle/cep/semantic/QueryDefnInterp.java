/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/QueryDefnInterp.java /main/5 2012/08/09 00:10:57 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    08/02/12 - evaluate clause cleanup
    sbishnoi    05/16/12 - support for slide without window; interpret slide
                           expression
    sbishnoi    12/04/07 - support for update semantics
    dlenkov     08/18/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/QueryDefnInterp.java /main/5 2012/08/09 00:10:57 sbishnoi Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPQueryDefnNode;
import oracle.cep.parser.CEPQueryNode;
import oracle.cep.parser.CEPRelationConstraintNode;
import oracle.cep.exceptions.CEPException;

/**
 * The interpreter for CEPQueryDefnNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class QueryDefnInterp extends QueryRelationInterp {

  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    assert node instanceof CEPQueryDefnNode;

    CEPQueryNode queryNode = ((CEPQueryDefnNode)node).getQueryNode();

    // Get the query node specific interpreter
    NodeInterpreter queryInterp =
	    InterpreterFactory.getInterpreter( queryNode);

    // Perform the analysis
    queryInterp.interpretNode( queryNode, ctx);
    
    interpretConstraintAttrs(((CEPQueryDefnNode)node).getPrimaryKeyNode(), ctx);
  }

  void registerTables() throws CEPException {
      return;
  }
  
  
  /**
   * Interpret CEPRelationConstraintNode
   * @param outputConstraintNode
   * @param ctx
   * @throws CEPException
   */
  void interpretConstraintAttrs(CEPRelationConstraintNode outputConstraintNode,
                                SemContext ctx)
    throws CEPException
  {
    if(outputConstraintNode != null)
    {
      // Get the query node specific interpreter
      NodeInterpreter queryInterp =
        InterpreterFactory.getInterpreter(outputConstraintNode);

      // Perform the semantic analysis
      queryInterp.interpretNode( outputConstraintNode, ctx);
    }
  }
  
}
