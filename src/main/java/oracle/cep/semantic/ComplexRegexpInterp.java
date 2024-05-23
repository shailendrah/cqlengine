/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/ComplexRegexpInterp.java /main/4 2008/09/17 15:19:47 parujain Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    Interpret complex regular expression for patterns

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/08/08 - support offset
    rkomurav    03/03/08 - set groupingexists
    rkomurav    02/01/08 - handle alternation to classB
    rkomurav    02/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/ComplexRegexpInterp.java /main/4 2008/09/17 15:19:47 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.common.RegexpOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPComplexRegexpNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPRegexpNode;

class ComplexRegexpInterp extends NodeInterpreter
{
  void interpretNode(CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    Regexp               semleft;
    Regexp               semright;
    RegexpOp             complexOp;
    CEPRegexpNode        left;
    CEPRegexpNode        right;
    CEPRegexpNode        unary;
    ComplexRegexp        complexRegexp;
    NodeInterpreter      unaryOperandInterp;
    NodeInterpreter      leftOperandInterp;
    NodeInterpreter      rightOperandInterp;
    CEPComplexRegexpNode complexRegexpNode;
    
    assert node instanceof CEPComplexRegexpNode;
    
    complexRegexpNode = (CEPComplexRegexpNode)node;
    unary             = complexRegexpNode.getUnaryOperand();
    
    if(unary != null)
    {
      unaryOperandInterp = InterpreterFactory.getInterpreter(unary);
      unaryOperandInterp.interpretNode(unary, ctx);
      complexRegexp = new ComplexRegexp(complexRegexpNode.getRegexpOp(),
                                        ctx.getRegExp());
      if(unary instanceof CEPComplexRegexpNode)
        ctx.setGroupingExists(true);
    }
    else
    {
      complexOp          = complexRegexpNode.getRegexpOp();
      left               = complexRegexpNode.getLeftOperand();
      right              = complexRegexpNode.getRightOperand();
      leftOperandInterp  = InterpreterFactory.getInterpreter(left);
      rightOperandInterp = InterpreterFactory.getInterpreter(right);
      
      leftOperandInterp.interpretNode(left, ctx);
      semleft = ctx.getRegExp();
      rightOperandInterp.interpretNode(right, ctx);
      semright = ctx.getRegExp();
      complexRegexp = new ComplexRegexp(complexOp, semleft, semright);
      if(complexOp == RegexpOp.ALTERNATION)
        ctx.setAlternationExists(true);
    }
    
    ctx.setRegExp(complexRegexp);
  }
}
