/* $Header: RowsWindowInterp.java 17-jun-2008.10:49:23 parujain Exp $ */

/* Copyright (c) 2006, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    The interpreter for the CEPRowsWindowExprNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    06/17/08 - slide support
    najain      04/06/06 - cleanup
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
 */

/**
 *  @version $Header: RowsWindowInterp.java 17-jun-2008.10:49:23 parujain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPRowsWindowExprNode;
import oracle.cep.exceptions.CEPException;

/**
 * The interpreter that is specific to the CEPRowsWindowExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class RowsWindowInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    RowWindowSpec         winspec;
    CEPRowsWindowExprNode rowsNode;    

    assert node instanceof CEPRowsWindowExprNode;
    rowsNode = (CEPRowsWindowExprNode)node;

    super.interpretNode(node, ctx);

    winspec = new RowWindowSpec(rowsNode.getNumRows(), rowsNode.getSlide());
    ctx.setWindowSpec(winspec);
  }
}
