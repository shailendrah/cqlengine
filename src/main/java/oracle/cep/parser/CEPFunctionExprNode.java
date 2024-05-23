/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPFunctionExprNode.java /main/10 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node for a function expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    02/20/09 - adding getExpression
    skmishra    02/13/09 - adding alias to toString
    parujain    08/13/08 - error offset
    udeshmuk    04/24/08 - support for aggr distinct
    mthatte     04/07/08 - adding toString
    sbishnoi    06/07/07 - fix xlint warning
    rkomurav    01/13/07 - cleanup
    anasrini    06/12/06 - Creation
    anasrini    06/12/06 - Creation
    anasrini    06/12/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPFunctionExprNode.java /main/9 2009/08/31 10:57:24 alealves Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.exceptions.SyntaxError;


/**
 * Parse tree node for a function expression
 *
 * @since 1.0
 */

public class CEPFunctionExprNode extends CEPExprNode {

  /** The function name */
  protected String name;

  /** The function parameters */
  protected CEPExprNode[] params;

  /** Is it Aggregate Distinct e.g. AGGR_FN(distinct EXPR) */
  protected boolean isDistinctAggr = false;

  /** Cartridge name, or null if managed by engine */
  private CEPStringTokenNode linkNode;

  /**
   * Constructor for a function with at least one parameter
   * @param name name of the function
   * @param paramList list of parameters. Each parameter is an 
   *                  expression
   */
  public CEPFunctionExprNode(CEPStringTokenNode nameToken, List<CEPExprNode> paramList) {
    this.name   = nameToken.getValue();
    this.params = (CEPExprNode[])paramList.toArray(new CEPExprNode[0]);
    this.isDistinctAggr = false;
    setStartOffset(nameToken.getStartOffset());
    if(!paramList.isEmpty())
      setEndOffset(paramList.get(paramList.size()-1).getEndOffset());
    else
      setEndOffset(nameToken.getEndOffset());
  }
  
  public CEPFunctionExprNode(List<CEPStringTokenNode> nameTokenList, 
      List<CEPExprNode> paramList, boolean isDistinctAggr) throws SyntaxException {
    this(nameTokenList.get(0), paramList, isDistinctAggr);
    
    if (nameTokenList.size() > 1) 
    {
      CEPStringTokenNode secondId = nameTokenList.get(1);
      if (secondId.isLink()) 
      {
        linkNode = secondId;
      }
      else 
      {
        throw new SyntaxException(SyntaxError.FUNCTION_ID_ERROR, nameTokenList.get(1).getStartOffset(), 
            nameTokenList.get(nameTokenList.size()-1).getEndOffset(), new Object[0]);
      }
    }
  }

  /**
   * Constructor for a function with zero parameters
   * @param name name of the function
   */
  public CEPFunctionExprNode(CEPStringTokenNode name) {
    this(name, new ArrayList<CEPExprNode>());
  }

  public CEPFunctionExprNode(CEPStringTokenNode name,
                      List<CEPExprNode> paramList,
                      boolean isDistinctAggr)
  {
    this(name, paramList);
    this.isDistinctAggr = isDistinctAggr;
  }

  /**
   * Constructor for a function with at least one parameter
   * @param name name of the function
   * @param paramList list of parameters. Each parameter is an 
   *                  expression
   */
  CEPFunctionExprNode(String name, List<CEPExprNode> paramList) {
    this.name   = name;
    this.params = (CEPExprNode[])paramList.toArray(new CEPExprNode[0]);
    this.isDistinctAggr = false;
    if(!paramList.isEmpty())
    {
      setStartOffset(paramList.get(0).getStartOffset());
      setEndOffset(paramList.get(paramList.size()-1).getEndOffset());
    }
    else
    {
      setStartOffset(0);
      setEndOffset(0);
    }
  }

  /**
   * Constructor for a function with zero parameters
   * @param name name of the function
   */
  CEPFunctionExprNode(String name) {
    this(name, new ArrayList<CEPExprNode>());
  }

  /**
   * Constructor for a function with link identifier
   * 
   * @param name
   * @param paramList
   * @param linkNode
   */
  public CEPFunctionExprNode(CEPStringTokenNode name,
      List<CEPExprNode> paramList, CEPStringTokenNode linkNode)
  {
    this(name, paramList);
    this.linkNode = linkNode;
  }

  // Getter methods
  

  /**
   * Get the name of the function
   * @return the name of the function
   */
  public String getName() {
    return name;
  }

  /**
   * Get the array of parameters
   * @return the array of parameters
   */
  public CEPExprNode[] getParams() {
    return params;
  }
  
  public boolean getIsDistinctAggr() {
    return this.isDistinctAggr;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" ");
    sb.append(this.name+"(");
    if(this.getIsDistinctAggr())
      sb.append(" distinct ");
    if(params.length > 0)
    {
      for(CEPExprNode param: params)
      {
        sb.append(param+",");
      }
      
      sb.deleteCharAt(sb.length() - 1);
    }
    sb.append(")");
    
    if(alias != null)
      sb.append(" AS " + alias);

    return sb.toString();
  }

  public String getExpression()
  {
    StringBuffer sb = new StringBuffer();
    sb.append(" ");
    sb.append(this.name + "(");
    if(this.getIsDistinctAggr())
      sb.append(" distinct ");
    if (params.length > 0)
    {
      for (CEPExprNode param : params)
      {
        sb.append(param + ",");
      }

      sb.deleteCharAt(sb.length() - 1);
    }
    sb.append(")");

    return sb.toString();
  }

  public CEPStringTokenNode getLink()
  {
    return linkNode;
  }
}

