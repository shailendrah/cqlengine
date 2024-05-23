/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPWindowRelationNode.java /main/6 2011/07/14 11:10:28 vikshukl Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    06/14/11 - subquery support
    hopark      04/21/11 - make public to be reused in cqservice
    mthatte     01/26/09 - adding getQCXML
    parujain    08/13/08 - error offset
    anasrini    02/21/06 - add getter methods 
    anasrini    12/20/05 - parse tree node for a relation resulting from 
                           applying a window on a stream 
    anasrini    12/20/05 - parse tree node for a relation resulting from 
                           applying a window on a stream 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPWindowRelationNode.java /main/4 2009/02/27 14:19:31 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node for a query whose root operator is a window operator.
 * A window operator is a stream to relation operator
 */

public class CEPWindowRelationNode extends CEPRelationNode {
  
  /** The stream node on whom the window expression is applied */
  protected CEPStreamNode stream;

  /** The window expression */
  protected CEPWindowExprNode windowExpr;

  /**
   * Constructor
   * @param stream the stream on whom the window expression is to be applied
   * @param windowExpr the window expression
   */
  public CEPWindowRelationNode(CEPStreamNode stream, CEPWindowExprNode windowExpr) {
    this.stream     = stream;
    this.windowExpr = windowExpr;
    setStartOffset(stream.getStartOffset());
    setEndOffset(windowExpr.getEndOffset());
    stream.setCqlProperty(" select * from " + stream.toString());
    if(!windowExpr.isInfinite())
      windowExpr.setCqlProperty(" select * from " + stream.toString() + " " + windowExpr.toString());
  }

  /**
   * Constructor with alias
   * @param stream the stream on whom the window expression is to be applied
   * @param windowExpr the window expression
   * @param alias the alias for the relation
   */
  public CEPWindowRelationNode(CEPStreamNode stream, 
                               CEPWindowExprNode windowExpr,
                               CEPStringTokenNode aliasToken) {
    this.stream     = stream;
    this.windowExpr = windowExpr;
    setAlias(aliasToken.getValue());
    setStartOffset(stream.getStartOffset());
    setEndOffset(aliasToken.getEndOffset());
    stream.setCqlProperty(" select * from " + stream.toString());
    if(!windowExpr.isInfinite())
      windowExpr.setCqlProperty(" select * from " + stream.getName() + " " + 
                                  windowExpr.toString() + " as " + getAlias());
  }

  // getter methods

  @Override
  public boolean isQueryRelationNode() {
    // if the stream on which the window spec is applied is a query,
    // then window relation itself becomes derived from a query.
    return stream.isQueryStreamNode();
  }

  /**
   * Get the stream node
   * @return the stream node
   */
  public CEPStreamNode getStreamNode() {
    return stream;
  }

  /**
   * Get the window expression node
   * @return the window expression node
   */
  public CEPWindowExprNode getWindowExprNode() {
    return windowExpr;
  }

  public String getName() {
    return stream.getName();
  }
  
  public String toString()
  {
    if(windowExpr.isInfinite())
      return stream.toString();
    else
    {
      if(stream.getAlias()!=null)
        return stream.getName() + windowExpr.toString() + " as " + stream.getAlias();
      else 
        return stream.getName() + windowExpr.toString();
    }
  }
  
  public int toQCXML(StringBuffer queryXml, int operatorID) throws UnsupportedOperationException
  {
    int returnID;
    returnID = stream.toQCXML(queryXml, operatorID);
    if(!windowExpr.isInfinite())
      returnID = windowExpr.toQCXML(queryXml,returnID);
    return returnID;
  }
}
