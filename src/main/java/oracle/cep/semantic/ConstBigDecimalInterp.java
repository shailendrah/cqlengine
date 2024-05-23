/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ConstBigDecimalInterp.java /main/1 2009/11/09 10:10:58 sborah Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      06/24/09 - support for bigdecimal
    sborah      06/24/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ConstBigDecimalInterp.java /main/1 2009/11/09 10:10:58 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPBigDecimalConstExprNode;
import oracle.cep.parser.CEPParseTreeNode;

/**
 * The const BigDecimal interpreter that is specific to CEPBigDecimalConstExprNode
 * <p>
 * This is private to semantic analysis module
 */

class ConstBigDecimalInterp extends NodeInterpreter {
  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
  throws CEPException {
    
    CEPBigDecimalConstExprNode bigDecimalNode;
    ConstBigDecimalExpr        bigDecimalExpr;
    
    assert node instanceof CEPBigDecimalConstExprNode;
    bigDecimalNode = (CEPBigDecimalConstExprNode) node;
    
    super.interpretNode(node, ctx);
    
    bigDecimalExpr = new ConstBigDecimalExpr(bigDecimalNode.getValue());
    ctx.setExpr(bigDecimalExpr);
  }
}
