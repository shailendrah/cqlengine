/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPExprNode.java /main/9 2009/04/15 16:23:18 hopark Exp $ */

/* Copyright (c) 2005, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/09/09 - remove system.out
    skmishra    03/26/09 - adding myString
    skmishra    02/20/09 - adding toExprString
    skmishra    01/29/09 - adding isAggr
    skmishra    08/21/08 - import, reorg
    parujain    08/11/08 - error offset
    mthatte     04/07/08 - adding toString()
    anasrini    08/29/06 - add alias support
    anasrini    12/20/05 - parse tree node for an expression 
    anasrini    12/20/05 - parse tree node for an expression 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPExprNode.java /main/9 2009/04/15 16:23:18 hopark Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.Collection;
import java.util.List;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for an expression
 */

public abstract class CEPExprNode implements CEPParseTreeNode {

  protected boolean isAggr = false;
  
  protected String alias;
  
  protected int startOffset = 0;
  
  protected int endOffset = 0;
  
  protected String myString;

  /**
   * Set the alias for this expression
   * @param alias the alias for this expression
   */
  public void setAlias(CEPStringTokenNode aliasToken) {
    this.alias = aliasToken.getValue();
    setEndOffset(aliasToken.getEndOffset());
  }

  /**
   * Get the alias for this expression
   * @return the alias for this expression
   */
  public String getAlias() {
    return alias;
  }
  
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }
  
  
  //Methods used by visualizer xml creation code.
  
  /**
   * @return isAggr
   */
  public boolean isAggr()
  {
    return isAggr;
  }
  

  public String toSelectString()
  {
    return toString();
  }
  
  public void setMyString(String arg)
  {
    myString = arg;
  }
  
  /** Returns a string representation of the Expr */
  public String toExprXml()
  {
    StringBuilder myXml = new StringBuilder(45);
    myXml.append("\n\t\t"
        + XMLHelper.buildElement(true, VisXMLHelper.selectExpressionTag,
            getXmlExpression(), null, null));
    if (this.alias != null)
      myXml.append("\n\t\t"
          + XMLHelper.buildElement(true, VisXMLHelper.aliasTag, alias,
              null, null));

    return XMLHelper.buildElement(true, VisXMLHelper.selectAttrTag, myXml
        .toString(), null, null);
  }
  
  /**
   * @return The string expression for this expr, WITHOUT alias
   */
  public abstract String getExpression();
  
  /**
   * Similar to getExpression() but can contain characterset for
   * xml only.
   * Example: Use &gt; for '>'
   * @return The expression for this expr node in xml format.
   */  
  public String getXmlExpression(){
    return getExpression();
  }
  
  /**
   * @return The string expression for this expr, WITH alias
   */
  public abstract String toString();
  
  public String getAliasOrExpression(Collection<CEPExprNode> other)
  {
    CEPExprNode matchingExpr = null;
    for(CEPExprNode node: other)
    {
      if(this.equals(node))
      {
        matchingExpr = node;
        break;
      }
    }
    
    if(matchingExpr != null && matchingExpr.getAlias() != null)
      return matchingExpr.getAlias();
    else
      return this.getExpression();
  }
  
  /**
   * Returns true if expression represented by this node contains
   * any logical CQL syntax.
   * Please override this function if any expression parser node
   * is for logical syntax.<p>
   * Default is false.
   */
  public boolean isLogical()
  {
    return false;
  }
}
  
