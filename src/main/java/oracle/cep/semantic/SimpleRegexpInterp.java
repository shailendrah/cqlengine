/* $Header: pcbpel/cep/server/src/oracle/cep/semantic/SimpleRegexpInterp.java /main/8 2009/03/19 20:24:41 parujain Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Interpreter for Simple regular expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/12/09 - make interpreters stateless
    parujain    09/08/08 - support offset
    parujain    08/27/08 - semantic offset
    rkomurav    03/02/08 - redo corr registration
    rkomurav    01/14/08 - support repititive correlation names and not
                           defining already defined / registerd correlation
                           names
    rkomurav    09/28/07 - support non mandatory definition for correlation
                           name
    rkomurav    09/26/07 - support string correlation names
    anasrini    05/23/07 - symbol table reorg
    rkomurav    02/22/07 - populate varIds instead of correlation names
    rkomurav    02/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/SimpleRegexpInterp.java /main/8 2009/03/19 20:24:41 parujain Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSimpleRegexpNode;
import oracle.cep.exceptions.CEPException;

class SimpleRegexpInterp extends NodeInterpreter
{
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) throws CEPException
  {
    String               corrName;
    int                  varId;
    int                  alphIndex;
    SymbolTableCorrEntry corrEntry;

    assert node instanceof CEPSimpleRegexpNode;
    CEPSimpleRegexpNode simpleRegexpNode = (CEPSimpleRegexpNode)node;
    
    try{
    super.interpretNode(node, ctx);
    corrName     = simpleRegexpNode.getName();
    
    //correlation names are pre defined in a different iteration
    corrEntry = ctx.getSymbolTable().lookupCorr(corrName);
    varId     = corrEntry.getVarId();
    alphIndex = corrEntry.getAlphabetIndex();
    
    ctx.setRegExp(new SimpleRegexp(varId, alphIndex));
    }catch(CEPException e)
    {
      e.setStartOffset(simpleRegexpNode.getStartOffset());
      e.setEndOffset(simpleRegexpNode.getEndOffset());
      throw e;
    }
  }
}
