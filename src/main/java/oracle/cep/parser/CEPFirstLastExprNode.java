/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPFirstLastExprNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

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
    skmishra    02/13/09 - adding alias to toString
    mthatte     04/07/08 - adding toString()
    udeshmuk    09/21/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPFirstLastExprNode.java /main/4 2009/02/23 00:45:57 skmishra Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.common.AggrFunction;

/**
* Parse tree node corresponding to FIRST and LAST aggregate functions.
*
*/


public class CEPFirstLastExprNode extends CEPAggrExprNode {

  /**
  *  Constructor for CEPFirstLastExprNode
  *  @param aggrFn the aggregate function
  *  @param arg argument to the aggregate function
  */
  public CEPFirstLastExprNode(AggrFunction aggrFn, CEPExprNode arg) {
    super(aggrFn, arg);
  }
  
  
}
