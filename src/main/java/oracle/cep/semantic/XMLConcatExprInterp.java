/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XMLConcatExprInterp.java /main/4 2015/03/20 03:04:38 udeshmuk Exp $ */

/* Copyright (c) 2008, 2015, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    03/18/15 - call setName
    parujain    03/13/09 - stateless interp
    parujain    08/26/08 - semantic exception offset
    mthatte     04/17/08 - Creation
 */

package oracle.cep.semantic;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPXMLConcatExprNode;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/XMLConcatExprInterp.java /main/4 2015/03/20 03:04:38 udeshmuk Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
public class XMLConcatExprInterp extends NodeInterpreter
{
  
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    
    assert node instanceof CEPXMLConcatExprNode;

    super.interpretNode(node,ctx);
    
    String name = new String("XMLCONCAT(");
    CEPXMLConcatExprNode xmlNode = (CEPXMLConcatExprNode)node;
    CEPExprNode[] concatExprs = xmlNode.getConcatExprList();
    List<Expr> argList = new ArrayList<Expr>();
    
    //For each expr in the parseTree, evaluate it and ensure that it returns Xmltype
    for(CEPExprNode concatExpr:concatExprs)
    {
      NodeInterpreter exprInterp = InterpreterFactory.getInterpreter(concatExpr);
      exprInterp.interpretNode(concatExpr, ctx);
      Expr expr = ctx.getExpr();
      //All arguments to Xmlconcat must be of Xmltype or NULL 
      if(expr.getReturnType() == Datatype.XMLTYPE || expr.getReturnType() == Datatype.UNKNOWN)
        argList.add(expr);
      else
        throw new SemanticException(SemanticError.INVALID_XMLCONCAT_ARGUMENT,
        		concatExpr.getStartOffset(), concatExpr.getEndOffset(),
        		new Object[]{expr.toString()});
      name = name + expr.getName();
    }
    
    XMLConcatExpr xExpr = new XMLConcatExpr(argList);
    name = name + ")";
    xExpr.setName(name, false);
    ctx.setExpr(xExpr);
  }
}
