/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRelationStarNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2010, 2011, Oracle and/or its affiliates. 
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
    sborah      07/26/10 - Creation
    sborah      07/26/10 - Creation
 */

package oracle.cep.parser;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRelationStarNode.java /main/1 2010/08/06 02:29:04 sborah Exp $
 *  @author  sborah  
 *  @since   release specific (what release of product did this appear in)
 */

public class CEPRelationStarNode extends CEPExprNode {

  /** Relation/Stream name/alias */
  protected String varName;
  
  public CEPRelationStarNode(CEPStringTokenNode varNameToken)
  {
    this.varName = varNameToken.getValue();
    setStartOffset(varNameToken.getStartOffset());
    setEndOffset(varNameToken.getEndOffset());
  }
 
  /** Returns a string representation of the Expr */
  @Override
  public String toExprXml()
  {
    StringBuilder myXml = new StringBuilder(45);
    return myXml.append("\n\t\t"
        + XMLHelper.buildElement(true, VisXMLHelper.selectExpressionTag,
            getExpression(), null, null)).toString();
  }
  
  @Override
  public String getExpression()
  {
    return this.varName + "." + "*";
  }

  @Override
  public String toString()
  {
    return this.varName + "." + "*";
  }

  public String getVarName()
  {
    return varName;
  }

}
