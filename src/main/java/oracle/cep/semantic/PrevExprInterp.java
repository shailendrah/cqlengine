/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/PrevExprInterp.java /main/6 2009/03/19 20:24:41 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/13/09 - stateless interp
    parujain    09/08/08 - support offset
    sbishnoi    08/06/08 - support for nanosecond timestamp
    rkomurav    11/27/07 - add semantic checks
    rkomurav    09/25/07 - prev range support
    rkomurav    09/05/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/PrevExprInterp.java /main/6 2009/03/19 20:24:41 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.parser.CEPBigintConstExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPIntConstExprNode;
import oracle.cep.parser.CEPPREVExprNode;
import oracle.cep.parser.CEPParseTreeNode;

class PrevExprInterp extends FuncExprInterp
{
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode( CEPParseTreeNode node, SemContext ctx) 
  throws CEPException
  {
    int             maxPrevIndex;
    CEPPREVExprNode prevExprNode;
    CEPExprNode[]   parserParams;
    int             temp;
    long            prevRange;
    AttrExpr        corrExpr;
    Attr            attr;
    SemCorrAttr     corrAttr;
    
    assert node instanceof CEPPREVExprNode;
    prevExprNode = (CEPPREVExprNode)node;

    if(!ctx.isPrevAllowed())
    {
      Object[] args = new Object[1];
      args[0] = new String("PREV");
      throw new SemanticException(SemanticError.FN_NOT_ALLOWED_HERE, 
           prevExprNode.getStartOffset(), prevExprNode.getEndOffset(), args);
    }
    
    super.interpretNode(node, ctx);
    
    parserParams = prevExprNode.getParams();
    Expr[] params = ctx.getParams();
    assert params[0] instanceof AttrExpr;
    corrExpr     = (AttrExpr) params[0];
    attr         = corrExpr.getAttr();
    assert attr instanceof SemCorrAttr;
    corrAttr     = (SemCorrAttr) attr;

    if(corrAttr.getVarId() != ctx.getCorrVarId())
      throw new SemanticException(SemanticError.INVALID_PREV_PARAM,
    		  prevExprNode.getStartOffset(), prevExprNode.getEndOffset());

    if(parserParams.length == 1)
      maxPrevIndex = 1;
    else
    {
      assert parserParams[1] instanceof CEPIntConstExprNode;
      CEPIntConstExprNode constExprNode = (CEPIntConstExprNode)parserParams[1];
      maxPrevIndex = constExprNode.getValue();
    }
    
    temp = ctx.getMaxPrevIndex();
    if(maxPrevIndex > temp)
      ctx.setMaxPrevIndex(maxPrevIndex);
    
    // for prev(A.c1, 3, <range>, timestamp)
    if(parserParams.length == 4)
    {
      assert parserParams[2] instanceof CEPBigintConstExprNode;
      CEPBigintConstExprNode rangeConst = (CEPBigintConstExprNode)parserParams[2];
      prevRange = rangeConst.getValue();
      
      if(!ctx.isPrevRangeExists())
      {
        ctx.setPrevRangeExists(true);
        ctx.setMaxPrevRange(prevRange);
      }
      else
      {
        if(ctx.getMaxPrevRange() < prevRange)
          ctx.setMaxPrevRange(prevRange);
      }
    }
  }
}
