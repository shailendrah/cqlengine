/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPComplexBooleanExprNode.java /main/11 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make constructor public
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    04/03/09 - adding type to generic LinkedList
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/19/09 - check for null in toString
    parujain    08/15/08 - error offset
    mthatte     04/07/08 - adding toString()
    udeshmuk    02/04/08 - parameterize error.
    sbishnoi    06/07/07 - fix xlint warning
    sbishnoi    05/17/07 - support for in
    parujain    11/16/06 - NOT operator
    parujain    10/31/06 - Complex Boolean Node
    parujain    10/31/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPComplexBooleanExprNode.java /main/10 2009/04/03 18:57:07 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.List;
import java.util.Collection;
import java.util.LinkedList;
import oracle.cep.common.CompOp;
import oracle.cep.common.LogicalOp;
import oracle.cep.common.OuterJoinType;
import oracle.cep.common.UnaryOp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.semantic.SemanticException;
import oracle.cep.util.XMLHelper;


/**
 * Parse tree node for a Complex Logical boolean expression
 *
 * @since 1.0
 */


/** This class is used mainly for Logical operators */

public class CEPComplexBooleanExprNode extends CEPBooleanExprNode {
  /** Logical operator */
  protected LogicalOp logicalOp;
	
  /** The left operand of logical operator */
  protected CEPExprNode leftOperand;

  /** The right operand of logical operator */
  protected CEPExprNode rightOperand;
  
  /**
   * Constructor in case of a Logical boolean operator
   * @param logOp the logical boolean operator
   * @param leftOperand the left operand
   * @param rightOperand the right operand
   */
  public CEPComplexBooleanExprNode(LogicalOp logOp, CEPExprNode leftOperand,
                     CEPExprNode rightOperand) {
    this.logicalOp    = logOp;
    this.leftOperand  = leftOperand;
    this.rightOperand = rightOperand;
    setStartOffset(leftOperand.getStartOffset());
    setEndOffset(rightOperand.getEndOffset());
  }

 
 /**
   * Constructor in case of a Logical boolean operator
   * @param logOp the logical boolean operator
   * @param leftOperand the left operand
   */
  public CEPComplexBooleanExprNode(LogicalOp logOp, CEPExprNode leftOperand)
  {
    this.logicalOp    = logOp;
    this.leftOperand  = leftOperand;
    this.rightOperand = null;
    setStartOffset(leftOperand.getStartOffset());
    setEndOffset(leftOperand.getEndOffset());
  }
  
  /**
   * Constructor in case of IN/NOT IN simple condition
   * @param arithExpr expression which will be checked for membership in given list exprList
   * @param exprList list of values
   * @param isNotInFlag check operator is IN or NOT IN
   */
  
  public CEPComplexBooleanExprNode(CEPExprNode arithExpr, List<CEPExprNode> exprList, boolean isNotInFlag)
    throws CEPException {
    
    LinkedList<CEPExprNode> arithExprList = (LinkedList<CEPExprNode>)exprList;
    setStartOffset(arithExpr.getStartOffset());
    setEndOffset(exprList.get(exprList.size()-1).getEndOffset());
    assert (arithExprList.size() != 0);
    if(isNotInFlag)
    {
      myString = arithExpr.toString() + " NOT IN " + listToString(arithExprList);
      this.logicalOp = LogicalOp.AND;
      if(arithExprList.size() == 1)
        this.rightOperand = new CEPComplexBooleanExprNode(LogicalOp.NOT, new CEPBaseBooleanExprNode(UnaryOp.IS_NULL, (CEPExprNode)arithExprList.getFirst()));
      else
        this.rightOperand = new CEPComplexBooleanExprNode(LogicalOp.NOT, makeExpressionTree(new LinkedList<CEPExprNode>(arithExprList)));
      this.leftOperand = new CEPComplexBooleanExprNode(LogicalOp.NOT, new CEPComplexBooleanExprNode(arithExpr, exprList, false));
    }
    else
    {
      myString = arithExpr.toString() + " IN " + listToString(arithExprList);
      this.logicalOp = LogicalOp.OR;
      if(arithExprList.size() == 1) 
      {
        this.leftOperand  = new CEPBaseBooleanExprNode(CompOp.EQ, arithExpr, (CEPExprNode)arithExprList.getFirst());
        this.rightOperand = new CEPBaseBooleanExprNode(CompOp.EQ, arithExpr, (CEPExprNode)arithExprList.getFirst());
      }
      else                      
      {
        this.leftOperand = new CEPBaseBooleanExprNode(CompOp.EQ, arithExpr, (CEPExprNode)arithExprList.getFirst());
        arithExprList.removeFirst();
        this.rightOperand = makeExpressionTree(arithExpr, arithExprList);
      }
      
    }
  }
  
  /**
   * Constructor in case of IN/NOT IN complex condition
   * @param logOp the logical boolean operator; logOp is always LogicalOp.AND
   * @param exprList expression list which will be checked for membership in given set of lists exprListSet
   * @param exprListSet set of lists
   */
  public CEPComplexBooleanExprNode(List<CEPExprNode> exprList, List<LinkedList<CEPExprNode>> exprListSet, boolean isNotInFlag)
  throws CEPException {
    
    LinkedList<CEPExprNode> arithExprList    = (LinkedList<CEPExprNode>)exprList;
    LinkedList<LinkedList<CEPExprNode>> arithExprListSet = (LinkedList<LinkedList<CEPExprNode>>)exprListSet;
    LinkedList arithExprListValues;
    setStartOffset(exprList.get(0).getStartOffset());
    
    if(isNotInFlag)
    {
      myString = listToString(arithExprList) + " NOT IN " + listOflistsToString(arithExprListSet);
      this.logicalOp    = LogicalOp.NOT;
      this.leftOperand  = new CEPComplexBooleanExprNode(exprList, exprListSet, false);
      this.rightOperand = null;
      setEndOffset(this.leftOperand.getEndOffset());
    }
    else
    {
      myString = listToString(arithExprList) + " IN " + listOflistsToString(arithExprListSet);
      if(arithExprListSet.size() == 1)
      {
        arithExprListValues = (LinkedList)arithExprListSet.getFirst();
        
        if(arithExprListValues.size() != arithExprList.size())
          throw new SemanticException(SemanticError.INVALID_NUM_EXPRESSIONS,startOffset,
                     ((CEPExprNode)arithExprListValues.getFirst()).getEndOffset(),
                    new Object[]{arithExprList.size(), arithExprListValues.size()});
        
        this.logicalOp      = LogicalOp.AND;
        this.leftOperand    = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)arithExprList.getFirst(), (CEPExprNode)arithExprListValues.getFirst());
        
        if(arithExprList.size() == 1)
        {
          this.rightOperand = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)arithExprList.getFirst(), (CEPExprNode)arithExprListValues.getFirst());
          setEndOffset(this.rightOperand.getEndOffset());
        }
        else
        {
          arithExprList.removeFirst();
          arithExprListValues.removeFirst();
          this.rightOperand = makeExpressionTree(arithExprList,arithExprListValues);
          setEndOffset(this.rightOperand.getEndOffset());
        }
      
      }
      else
      {
        this.logicalOp              = LogicalOp.OR;
        LinkedList<LinkedList<CEPExprNode>> singleElementSet = new LinkedList<LinkedList<CEPExprNode>>();
        LinkedList<CEPExprNode> tempExprList     = new LinkedList<CEPExprNode>(arithExprList);
        singleElementSet.add((LinkedList<CEPExprNode>)arithExprListSet.getFirst());
        this.leftOperand = new CEPComplexBooleanExprNode(tempExprList, singleElementSet, false);
        arithExprListSet.removeFirst();
        this.rightOperand = new CEPComplexBooleanExprNode(arithExprList, arithExprListSet, false);
        setEndOffset(this.rightOperand.getEndOffset());
      }
    }
    
  }
  

  /**
   * Get the logical operator
   * @return Logical Operator
   */
  public LogicalOp getLogicalOp() {
    return logicalOp;
  }
  
  /**
   * Get the left operand
   * @return the left operand
   */
  public CEPExprNode getLeftOperand() {
    return leftOperand;
  }

  /**
   * Get the right operand
   * @return the right operand
   */
  public CEPExprNode getRightOperand() {
    return rightOperand;
  }
  
  /**
   * Generates a Tree of CEPExprNode nodes from list of expressions
   * @param arithExpr 
   * @param arithExprList
   * @return
   */
  private CEPExprNode makeExpressionTree(CEPExprNode arithExpr, LinkedList arithExprList)
  {
    CEPExprNode node;
    
    node = new CEPBaseBooleanExprNode(CompOp.EQ, arithExpr, (CEPExprNode)arithExprList.getFirst());
    if(arithExprList.size() == 1)
    {
      return node;
    }
    else
    {
      arithExprList.removeFirst();
      return new CEPComplexBooleanExprNode(LogicalOp.OR, node, makeExpressionTree(arithExpr, arithExprList));
    }
  }
  
  private CEPExprNode makeExpressionTree(LinkedList arithExprList, LinkedList arithExprListValues)
  {
    CEPExprNode node;
    
    node = new CEPBaseBooleanExprNode(CompOp.EQ, (CEPExprNode)arithExprList.getFirst(), (CEPExprNode)arithExprListValues.getFirst());
    if(arithExprList.size() == 1)
      return node;
    else
    {
      arithExprList.removeFirst();
      arithExprListValues.removeFirst();
      return new CEPComplexBooleanExprNode(LogicalOp.AND, node, makeExpressionTree(arithExprList, arithExprListValues));
    }
    
  }
  
  private CEPExprNode makeExpressionTree(LinkedList arithExprList)
  {
    CEPExprNode leftOperand;
    assert (arithExprList.size() != 0);
    if(arithExprList.size() == 1)
      return new CEPBaseBooleanExprNode(UnaryOp.IS_NULL, (CEPExprNode)arithExprList.getFirst());
    else
    {
      leftOperand = (CEPExprNode)arithExprList.getFirst();
      arithExprList.removeFirst();
      return new CEPComplexBooleanExprNode(LogicalOp.OR, new CEPBaseBooleanExprNode(UnaryOp.IS_NULL, leftOperand), makeExpressionTree(arithExprList));
    }
  }
  
  private String listOflistsToString(LinkedList<LinkedList<CEPExprNode>> exprListSet)
  {
    StringBuffer myString = new StringBuffer(50);
    myString.append("(");
    for(LinkedList<CEPExprNode> exprList : exprListSet)
    {
      myString.append("(");
      for(CEPExprNode c : exprList)
        myString.append(c.toString());
      myString.append("),");
    }
    myString.deleteCharAt(myString.length() - 1);
    myString.append(")");
    return myString.toString();
  }
  
  private String listToString(LinkedList<CEPExprNode> exprList)
  {
    StringBuffer myString = new StringBuffer(25);
    myString.append("(");
    for (CEPExprNode e : exprList)
      myString.append(e.toString() + ",");
    myString.deleteCharAt(myString.length() - 1);
    myString.append(")");
    return myString.toString();
  }

  public String toString()
  {
    if (myString != null)
    {
      if (alias != null)
        return myString + " AS " + alias;
      else
        return myString;
    }

    else
    {
      if (rightOperand != null)
      {
        if (alias == null)
          myString = XMLHelper.toHTMLString(leftOperand.toString()
              + logicalOp.getSymbol() + rightOperand.toString());
        else
          myString = XMLHelper.toHTMLString(leftOperand.toString()
              + logicalOp.getSymbol() + rightOperand.toString() + " AS "
              + alias);
      }

      else
      {
        if (alias == null)
          myString = XMLHelper.toHTMLString(" " + logicalOp.getSymbol() 
              + leftOperand.toString() + " ");
        else
          myString = XMLHelper.toHTMLString(" " + logicalOp.getSymbol()
              + leftOperand.toString() + " AS " + alias + " ");
      }
    }
    
    return myString;
  }


  @Override
  public String getExpression()
  {
    if(myString != null)
      return myString;
    else
    {
      if (rightOperand != null)
      {
        myString = XMLHelper.toHTMLString("  (" + leftOperand.toString() + ") "
            + logicalOp.getSymbol() + " (" + rightOperand.toString() + ") ");
      }

      else
      {
        myString = XMLHelper.toHTMLString(" " + logicalOp.getSymbol() + "("
            + leftOperand.toString() + ") ");
      }
    }
    
    return myString;
  }
  
  @Override
  public String getAliasOrExpression(Collection<CEPExprNode> other)
  {
    String myString = null;
    
    if (rightOperand != null)
    {
        myString = "  (" + leftOperand.getAliasOrExpression(other) + ") "
            + logicalOp.getSymbol() + " (" + rightOperand.getAliasOrExpression(other) + ") ";
    }
    else
    {
      myString = " " + logicalOp.getSymbol() + "("
            + leftOperand.getAliasOrExpression(other) + ") ";
    }
    return myString;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CEPComplexBooleanExprNode other = (CEPComplexBooleanExprNode) obj;
    
    if (leftOperand == null)
    {
      if (other.leftOperand != null)
        return false;
    } 
    else if (!leftOperand.equals(other.leftOperand))
      return false;
    
    if (logicalOp != other.logicalOp)
      return false;
    if (rightOperand == null)
    {
      if (other.rightOperand != null)
        return false;
    }
    else if (!rightOperand.equals(other.rightOperand))
      return false;
    
    return true;
  }
	
}
