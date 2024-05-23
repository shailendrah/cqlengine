/* $Header: ConstFloatInterp.java 12-apr-2006.23:33:28 anasrini Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    The interpreter for the CEPIntConstExprNode parse tree node    

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    04/12/06 - bug fix 
    najain      04/06/06 - cleanup
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
    anasrini    02/23/06 - Creation
 */

/**
 *  @version $Header: ConstFloatInterp.java 12-apr-2006.23:33:28 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPFloatConstExprNode;
import oracle.cep.exceptions.CEPException;

/**
 * The floaterpreter that is specific to the CEPFloatConstExprNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class ConstFloatInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPFloatConstExprNode floatNode;
    ConstFloatExpr        floatExpr;

    assert node instanceof CEPFloatConstExprNode;
    floatNode = (CEPFloatConstExprNode)node;

    super.interpretNode(node, ctx);

    floatExpr = new ConstFloatExpr(floatNode.getValue());
    ctx.setExpr(floatExpr);
  }
}
