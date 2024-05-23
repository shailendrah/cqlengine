/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPDecodeExprNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    02/20/09 - adding getExpression
    skmishra    02/13/09 - adding alias to toString
    parujain    08/21/08 - 
    mthatte     04/07/08 - adding toString()
    udeshmuk    01/13/08 - accomodate changed constructor of
                           CEPSearchedCaseExprNode and CEPSimpleCaseExprNode.
    sbishnoi    06/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPDecodeExprNode.java /main/6 2009/02/23 00:45:57 skmishra Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.UnaryOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.semantic.SemanticException;
//import oracle.cep.exceptions.ParserError;
import oracle.cep.exceptions.SemanticError;
import java.util.List;
import java.util.LinkedList;

public class CEPDecodeExprNode extends CEPExprNode {

  protected CEPExprNode[]                        decodeArgs;
  protected int                                  decodeArgsLength;
  protected CEPSearchedCaseExprNode              decodeCaseExpr;
  protected LinkedList<CEPCaseConditionExprNode> isNullCondition;
   
  public CEPDecodeExprNode(List<CEPExprNode> decodeArgsList) throws CEPException
  {
    // minimum number of expressions required is three; 
    // DECODE(expr, search1, result1)
	if(!decodeArgsList.isEmpty())
	{
	  setStartOffset(decodeArgsList.get(0).getStartOffset());
	  setEndOffset(decodeArgsList.get(decodeArgsList.size()-1).getEndOffset());
	}
    
    if(decodeArgsList.size() < 3)
     throw new SemanticException(SemanticError.NOT_ENOUGH_ARG_IN_DECODE, startOffset, endOffset);
    
    this.decodeArgs       = 
      (CEPExprNode[])decodeArgsList.toArray(new CEPExprNode[0]);

    this.decodeArgsLength = decodeArgs.length;
    convertDecodeToCaseExpr();
  }
  
  public CEPSearchedCaseExprNode getDecodeCaseExpr()
  {
    return decodeCaseExpr;
  }
  
  /**
   * This function will convert Decode Expression 
   * DECODE(expr, if1, then1, ..., ifn, thenn, [else]) to
   * a main SearchCase Expression 
   * CASE WHEN expr is null 
   *           THEN CASE WHEN if1 is null THEN then1 ... [ELSE else] END
   *           ELSE CASE expr WHEN if1 THEN then1 ... [ELSE else] END
   * END                   
   */
  private void convertDecodeToCaseExpr()
  {
    isNullCondition    = new LinkedList<CEPCaseConditionExprNode>();
    CEPCaseConditionExprNode caseConditionExpr;
    
    caseConditionExpr = new CEPCaseConditionExprNode(
      new CEPBaseBooleanExprNode(UnaryOp.IS_NULL, decodeArgs[0]), 
      getIsNullDecodeExpr());
    
    isNullCondition.add(caseConditionExpr);
    decodeCaseExpr = 
      new CEPSearchedCaseExprNode(isNullCondition, getIsNotNullDecodeExpr());
  }
  
  /**
   * This function will calculate ELSE part of main SearchCase Expression
   * @return a Simple Case Expression
   */
  private CEPSimpleCaseExprNode getIsNotNullDecodeExpr() 
  {
    LinkedList<CEPCaseComparisonExprNode> searchResultList;
    searchResultList = new LinkedList<CEPCaseComparisonExprNode>();
    int i = 1;
    
    while(i < decodeArgsLength-1) 
    {
      searchResultList.add(
        new CEPCaseComparisonExprNode(decodeArgs[i] , decodeArgs[i+1]));
      i = i + 2;
    }
    
    if(decodeArgsLength % 2 == 0)
      return new CEPSimpleCaseExprNode(decodeArgs[0], searchResultList, 
                                       decodeArgs[decodeArgsLength-1]);
    else
      return new CEPSimpleCaseExprNode(decodeArgs[0], searchResultList,
                                       new CEPNullConstExprNode());
    
  }
  
  /**
   * This function will calculate IF Part of main SearchCase Expression
   * @return a SearchCase Expression
   */
  private CEPSearchedCaseExprNode getIsNullDecodeExpr() {
    
    LinkedList<CEPCaseConditionExprNode> searchResultList;
    searchResultList = new LinkedList<CEPCaseConditionExprNode>();
    int i = 1;
    
    while(i < decodeArgsLength-1) {
      searchResultList.add(
        new CEPCaseConditionExprNode(
          new CEPBaseBooleanExprNode(UnaryOp.IS_NULL, decodeArgs[i]),
          decodeArgs[i+1]));
      i = i + 2;
    }
    
    if(decodeArgsLength % 2 == 0)
      return new CEPSearchedCaseExprNode(searchResultList , 
        decodeArgs[decodeArgsLength-1]);
    else
      return new CEPSearchedCaseExprNode(searchResultList ,
        new CEPNullConstExprNode());
  }
  
  
  //TODO: Implement
  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" decode(");
    for(CEPExprNode expr: decodeArgs)
      sb.append(expr);
    sb.append(") ");
    if(alias != null)
      sb.append(" as " + alias + " ");
    return sb.toString();
  }

  @Override
  public String getExpression()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" decode(");
    for(CEPExprNode expr: decodeArgs)
      sb.append(expr);
    sb.append(") ");
    return sb.toString();
  }
}
