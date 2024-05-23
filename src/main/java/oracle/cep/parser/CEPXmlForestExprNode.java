/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXmlForestExprNode.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

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
    skmishra    06/03/09 - correcting toString
    skmishra    02/20/09 - adding toExprXml
    skmishra    02/13/09 - adding alias to toString
    parujain    08/15/08 - error offset
    parujain    05/23/08 - XMLForest expr
    parujain    05/23/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXmlForestExprNode.java /main/5 2009/06/04 17:52:35 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.List;

public class CEPXmlForestExprNode extends CEPExprNode {
  protected CEPExprNode[] forestExprs;
  
  public CEPXmlForestExprNode(List<CEPExprNode> exprs)
  {
    if(exprs != null)
    {
      forestExprs = (CEPExprNode[])exprs.toArray(new CEPExprNode[0]);
      setStartOffset(exprs.get(0).getStartOffset());
      setEndOffset(exprs.get(exprs.size()-1).getEndOffset());
    }
    else
    {
      forestExprs = null;
      setStartOffset(0);
      setEndOffset(0);
    }
  }
  
  public CEPExprNode[] getForestExprs()
  {
    return forestExprs;
  }
  
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(" " + "XMLFOREST" + "( ");
    if(forestExprs != null)
    {
      for(int i=0; i<forestExprs.length; i++)
      {
        sb.append(forestExprs[i].toString());
        if(i < (forestExprs.length -1))
         sb.append(",");
      }
    }
    sb.append(")");
    return sb.toString(); 
  }

  @Override
  public String getExpression()
  {
    throw new UnsupportedOperationException("Not supported for XmlForest expressions");
  }
	
}
