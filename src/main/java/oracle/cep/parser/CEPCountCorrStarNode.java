/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPCountCorrStarNode.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

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
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/19/09 - removing toaggrxml, inherits from super
    skmishra    02/05/09 - adding toVisXML
    parujain    08/15/08 - error offset
    mthatte     04/07/08 - adding toString()
    udeshmuk    09/27/07 - removing parameter in the constructor.
    udeshmuk    09/21/07 - Creation.
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPCountCorrStarNode.java /main/5 2009/02/23 00:45:57 skmishra Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.AggrFunction;

/**
* Parse tree node corresponding to COUNT_CORR_STAR aggregate function.
*
*/

public class CEPCountCorrStarNode extends CEPAggrExprNode {

  /**
  *  Constructor for CEPCountCorrStarNode
  *  @param arg argument to the aggregate function
  */
  public CEPCountCorrStarNode(CEPExprNode arg) {
    super(AggrFunction.COUNT_CORR_STAR, arg);
  }
  
  public String getExpression()
  {
    return " count (" + this.arg.toString() + ") ";
  }
  
  public String toString()
  {
    if(alias == null)
      return " count (" + this.arg.toString() + ") ";
    else 
      return " count (" + this.arg.toString() + ") AS " + alias + " ";
  }
}
