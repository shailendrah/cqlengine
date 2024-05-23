/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SlideExprInterp.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/17/12 - adding interpreter for slide expression
    sbishnoi    05/17/12 - Creation
 */
package oracle.cep.semantic;

import oracle.cep.common.RangeConverter;
import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSlideExprNode;
import oracle.cep.parser.CEPTimeSpecNode;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/SlideExprInterp.java /main/1 2012/06/07 03:24:37 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class SlideExprInterp extends NodeInterpreter
{

  /* (non-Javadoc)
   * @see oracle.cep.semantic.NodeInterpreter#interpretNode
   * (oracle.cep.parser.CEPParseTreeNode, oracle.cep.semantic.SemContext)
   */
  @Override
  void interpretNode(CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    super.interpretNode(node, ctx);
    
    // Get the CEPSlideExprNode
    CEPSlideExprNode slideExprNode = (CEPSlideExprNode)node;
    
    long slideNanos = -1l;
    
    if(slideExprNode.isNumericTsSpecification())
    {
      slideNanos = slideExprNode.getConstTimeVal();
    }
    else
    { 
      CEPTimeSpecNode timeSpec = slideExprNode.getConstTimeSpec();
      // The time is specified as a constant with time unit,
      // We will convert that duration into NANOSECONDS
      slideNanos 
        = RangeConverter.interpRange(timeSpec.getAmount(), 
                                     timeSpec.getTimeUnit());
    }
    
    SemQuery query = ctx.getSemQuery();
    query.setSlideInterval(slideNanos);
  }
  
}