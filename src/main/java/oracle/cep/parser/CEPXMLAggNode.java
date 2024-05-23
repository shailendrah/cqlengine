/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXMLAggNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 hopark     04/21/11 - make public to be reused in cqservice
 skmishra   02/13/09 - adding alias to toString
 sbishnoi   02/09/09 - changing constructor
 parujain   08/15/08 - error offset
 udeshmuk   05/30/08 - xmlagg support.
 mthatte    05/21/08 - Creation
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.common.AggrFunction;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXMLAggNode.java /main/4 2009/03/02 23:20:27 sbishnoi Exp $
 *  @author  mthatte
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPXMLAggNode extends CEPAggrExprNode {

  /** The exprs in the order by clause */
  CEPOrderByExprNode[] orderByExprs;

  /**
   * Constructor for CEPXMLAggNode
   * @param xmlTypeNode the expr having datatype as xmltype
   * @param orderByExprs the exprs in the order by clause of the function
   */
  public CEPXMLAggNode(CEPExprNode xmlTypeNode, CEPOrderByNode orderByNode) {
    super(AggrFunction.XML_AGG, xmlTypeNode);
    // the superclass constructor sets the 'arg' appropriately    
    setStartOffset(xmlTypeNode.getStartOffset());
    if(orderByNode != null)
    {
      this.orderByExprs = orderByNode.getOrderByClause();
      int numOrderByExprs = orderByExprs.length;
      setEndOffset(orderByExprs[numOrderByExprs - 1].getEndOffset());
    }
    else
    {
      this.orderByExprs = null;
      setEndOffset(xmlTypeNode.getEndOffset());
    }
  }

  public CEPExprNode getXmlTypeNode() {
    return this.arg;
  }

  public CEPOrderByExprNode[] getOrderByExprs() {
    return this.orderByExprs;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(" xmlagg( ");
    sb.append(arg.toString());
    if(orderByExprs != null) {
      sb.append(" order by ");
      for(int i=0; i < orderByExprs.length; i++)
        sb.append(orderByExprs[i].getOrderByExpr().toString()+" ");
    }
    sb.append(")");
    
    if(alias != null)
      sb.append(" AS " + alias + " ");
    return sb.toString();
  }
}
