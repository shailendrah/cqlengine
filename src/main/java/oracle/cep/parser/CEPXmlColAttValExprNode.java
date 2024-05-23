/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXmlColAttValExprNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    08/15/08 - error offset
    parujain    05/29/08 - XMLColAttValExpr
    parujain    05/29/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXmlColAttValExprNode.java /main/4 2009/02/23 00:45:57 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;

public class CEPXmlColAttValExprNode extends CEPExprNode {
  protected CEPExprNode[] colExprs;
  
  public CEPXmlColAttValExprNode(List<CEPExprNode> exprs)
  {
    if(exprs != null)
    {
      colExprs = (CEPExprNode[])exprs.toArray(new CEPExprNode[0]);
      setStartOffset(exprs.get(0).getStartOffset());
      setEndOffset(exprs.get(exprs.size()-1).getEndOffset());
    }
    else
    {
      colExprs = null;
      setStartOffset(0);
      setEndOffset(0);
    }
  }
  
  public CEPExprNode[] getColAttExprs()
  {
    return colExprs;
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(" " + "XMLCOLATTVAL" + "( ");
    if(colExprs != null)
    {
      for(int i=0; i<colExprs.length; i++)
      {
        sb.append(colExprs[i].toString());
        if(i < (colExprs.length -1))
         sb.append(",");
      }
    }
    sb.append(")");
    return null;
  }

  @Override
  public String getExpression()
  {
    return toString();
  }
	
}
