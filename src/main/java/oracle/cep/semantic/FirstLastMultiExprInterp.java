/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/FirstLastMultiExprInterp.java /main/3 2008/09/17 15:19:47 parujain Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    09/08/08 - support offset
    rkomurav    04/21/08 - restrict usage of firt and last beyond pattern
                           clause
    rkomurav    04/01/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/FirstLastMultiExprInterp.java /main/3 2008/09/17 15:19:47 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPFirstLastMultiExprNode;
import oracle.cep.parser.CEPIntConstExprNode;
import oracle.cep.parser.CEPParseTreeNode;

class FirstLastMultiExprInterp extends FuncExprInterp
{
  void interpretNode( CEPParseTreeNode node, SemContext ctx)
  throws CEPException 
  {
    CEPExprNode[]             parserParams;
    CEPFirstLastMultiExprNode parserNode;
    
    
    assert node instanceof CEPFirstLastMultiExprNode;
    parserNode = (CEPFirstLastMultiExprNode)node;
    
    super.interpretNode(node, ctx);
    
    if(!ctx.isFirstLastAllowed())
    {
      throw new SemanticException(SemanticError.AGGR_FN_NOT_ALLOWED_HERE,
    	  parserNode.getStartOffset(), parserNode.getEndOffset(),
          new Object[]{parserNode.getName()});
    }
    
    parserParams = parserNode.getParams();
    assert parserParams[1] instanceof CEPIntConstExprNode;
    CEPIntConstExprNode offset = (CEPIntConstExprNode)parserParams[1];
    if(offset.getValue() <= 0)
    {
      throw new SemanticException(SemanticError.INVALID_FIRST_LAST_ARGUMENT,
          offset.getStartOffset(), offset.getEndOffset(),
          new Object[]{parserNode.getName()});
    }
  }
}
