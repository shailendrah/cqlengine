/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPFirstLastMultiExprNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

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
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/13/09 - adding alias to toString
    parujain    08/13/08 - error offset
    rkomurav    04/01/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPFirstLastMultiExprNode.java /main/4 2009/02/23 00:45:57 skmishra Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.List;

public class CEPFirstLastMultiExprNode extends CEPFunctionExprNode
{
  /** Constructor
   * 
   */
  public CEPFirstLastMultiExprNode(String name, List<CEPExprNode> paramList)
  {
    super(name, paramList);
  }
}
