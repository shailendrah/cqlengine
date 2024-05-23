/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/WindowRelInterp.java /main/10 2011/09/23 11:16:36 vikshukl Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    The interpreter for the CEPWindowRelationNode parse tree node

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    08/30/11 - save and restore subquery spec
    sbishnoi    02/23/11 - allowing windows over relation in case of
                           valuewindow
    parujain    05/19/09 - support outer join
    sborah      03/16/09 - fix for bug 8334276
    hopark      02/01/09 - set table name to context
    parujain    09/08/08 - support offset
    parujain    08/26/08 - semantic exception offset
    udeshmuk    02/05/08 - parameterize error.
    najain      12/07/07 - 
    rkomurav    02/20/07 - remember patternSpec before interpreting window
    najain      04/06/06 - cleanup
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/WindowRelInterp.java /main/8 2009/06/04 17:45:06 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPWindowRelationNode;
import oracle.cep.parser.CEPStreamNode;
import oracle.cep.parser.CEPWindowExprNode;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;

/**
 * The interpreter that is specific to the CEPWindowRelationNode parse tree 
 * node.
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

class WindowRelInterp extends NodeInterpreter {

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {

    CEPWindowRelationNode winRelNode;
    CEPStreamNode         streamNode;
    CEPWindowExprNode     windowExprNode;
    NodeInterpreter       streamInterp;
    NodeInterpreter       windowExprInterp;

    assert node instanceof CEPWindowRelationNode;
    winRelNode = (CEPWindowRelationNode)node;

    super.interpretNode(node, ctx);

    streamNode     = winRelNode.getStreamNode();
    windowExprNode = winRelNode.getWindowExprNode();

    streamInterp     = InterpreterFactory.getInterpreter(streamNode);
    windowExprInterp = InterpreterFactory.getInterpreter(windowExprNode);

    try {
      streamInterp.interpretNode(streamNode, ctx);
    }
    catch (CEPException e) {
      if (e.getErrorCode() == SemanticError.NOT_A_STREAM_ERROR && 
          !windowExprNode.isWindowOverRelationAllowed())
        throw new SemanticException(SemanticError.WINDOW_OVER_REL_ERROR,
                        streamNode.getStartOffset(), streamNode.getEndOffset(),
                        new Object[]{streamNode.getName()});
      else if(e.getErrorCode() != SemanticError.NOT_A_STREAM_ERROR)
        throw e;
    }

    PatternSpec pSpec = ctx.getPatternSpec();
    XmlTableSpec xSpec = ctx.getXmlTableSpec();
    DerivedTimeSpec dSpec = ctx.getDerivedTimeSpec();    
    RelationSpec rSpec   = ctx.getRelationSpec();
    SubquerySpec subqSpec = ctx.getSubquerySpec();
    
    ctx.setWindowRelName( streamNode.getName() );
    ctx.setWindowRelAlias( streamNode.getAlias() );
    
    windowExprInterp.interpretNode(windowExprNode, ctx);

    ctx.setPatternSpec(pSpec);
    ctx.setXmlTableSpec(xSpec);
    ctx.setDerivedTimeSpec(dSpec);
    ctx.setRelationSpec(rSpec);
    ctx.setSubquerySpec(subqSpec);
  }
}
