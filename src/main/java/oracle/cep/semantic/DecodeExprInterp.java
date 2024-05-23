/* $Header: DecodeExprInterp.java 28-jun-2007.11:18:20 sbishnoi Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    06/28/07 - Creation
 */

/**
 *  @version $Header: DecodeExprInterp.java 28-jun-2007.11:18:20 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPDecodeExprNode;

public class DecodeExprInterp extends NodeInterpreter {
  
  void interpretNode(CEPParseTreeNode node, SemContext ctx)
  throws CEPException {
    
    CEPDecodeExprNode decodeExpr = (CEPDecodeExprNode)node;
    super.interpretNode(node, ctx);
    NodeInterpreter interp = InterpreterFactory.getInterpreter(decodeExpr.getDecodeCaseExpr());
    interp.interpretNode(decodeExpr.getDecodeCaseExpr(), ctx);
  }
}
