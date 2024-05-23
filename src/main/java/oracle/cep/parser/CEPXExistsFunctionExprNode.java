/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXExistsFunctionExprNode.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    03/16/09 - stateless machine
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/13/09 - adding alias to toString
    parujain    08/13/08 - error offset
    mthatte     12/26/07 - 
    najain      11/28/07 - 
    anasrini    11/28/07 - 
    najain      11/21/07 - Creation
 */

package oracle.cep.parser;

import java.util.List;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXExistsFunctionExprNode.java /main/5 2009/03/19 20:24:41 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPXExistsFunctionExprNode extends CEPXQryFunctionExprNode 
{
  public CEPXExistsFunctionExprNode(CEPStringTokenNode xqryStrToken, List<CEPXQryArgExprNode> xqParams) 
  {
    super(new String("XMLEXISTS"), xqryStrToken, xqParams, false);
  }
}
