/* $Header: ConstCharInterp.java 06-apr-2006.11:26:59 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    The interpreter for the CEPStringConstExprNode parse tree node    

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      04/06/06 - cleanup
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
 */

/**
 *  @version $Header: ConstCharInterp.java 06-apr-2006.11:26:59 najain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPStringConstExprNode;
import oracle.cep.exceptions.CEPException;

/**
 * The interpreter that is specific to the CEPStringConstExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class ConstCharInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPStringConstExprNode charNode;
    ConstCharExpr          charExpr;

    assert node instanceof CEPStringConstExprNode;
    charNode = (CEPStringConstExprNode)node;

    super.interpretNode(node, ctx);

    charExpr = new ConstCharExpr(charNode.getValue());
    ctx.setExpr(charExpr);
  }
}
