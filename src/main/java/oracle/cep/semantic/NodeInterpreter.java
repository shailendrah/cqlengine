/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/NodeInterpreter.java /main/3 2009/03/19 20:24:41 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The base interface for a parse tree node specific interpreter

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/12/09 - make interpreters stateless
    anasrini    05/23/07 - symbol table reorg
    najain      04/06/06 - cleanup
    anasrini    02/27/06 - call resetTransient on the context 
    anasrini    02/20/06 - Creation
    anasrini    02/20/06 - Creation
    anasrini    02/20/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/NodeInterpreter.java /main/3 2009/03/19 20:24:41 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.exceptions.CEPException;

/**
 * The base interface for an interpreter that is specific to a particular
 * type of parse tree node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

abstract class NodeInterpreter {

  
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  /**
   * This is the main method where the interpretation is done
   * @param node the parse tree node to be interpreted. The type (subclass)
   *             of this parse tree node will have to be compatible with
   *             the concrete instance of the NodeInterpreter
   * @param ctx  the global semantic analysis context
   *             
   */
  void interpretNode(CEPParseTreeNode node, SemContext ctx)
    throws CEPException {

    ctx.resetTransient();

    if(ctx.getSemQuery() != null)
      ctx.getSemQuery().setSymTable(ctx.getSymbolTable());
  }
}
