/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPSelectListNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

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
    hopark      04/21/11 - make constructor public
    skmishra    01/27/09 - adding toQCXML
    parujain    08/11/08 - error offset
    mthatte     10/03/07 - Making constructor public
    sbishnoi    06/07/07 - fix xlint warning
    anasrini    02/23/06 - add getter methods 
    anasrini    12/21/05 - parse tree node corresponding to the select list 
    anasrini    12/21/05 - parse tree node corresponding to the select list 
    anasrini    12/21/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPSelectListNode.java /main/6 2009/02/23 00:45:57 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node corresponding to the select list 
 */

public class CEPSelectListNode implements CEPParseTreeNode {

  /** The select list */
  protected CEPExprNode[] selectList;

  /** Is it a * list */
  protected boolean isStar;

  /** Is it a SELECT DISTINCT */
  protected boolean isDistinct;
  
  protected int startOffset;
  
  protected int endOffset;
  
  protected String cqlProperty;

  /**
   * Constructor for non-star select list
   * @param isDistinct is it a SELECT DISTINCT
   * @param selectList select list
   */
  public CEPSelectListNode(boolean isDistinct, List<CEPExprNode> selectList) {
    this.isDistinct = isDistinct;
    this.isStar     = false;
    this.selectList = (CEPExprNode[])(selectList.toArray(new CEPExprNode[0]));
    setStartOffset(selectList.get(0).getStartOffset());
    setEndOffset(selectList.get(selectList.size()-1).getEndOffset());
  }

  /**
   * Constructor for star as select list
   * @param isDistinct is it a SELECT DISTINCT
   */
  public CEPSelectListNode(boolean isDistinct) {
    this.isDistinct = isDistinct;
    this.isStar     = true;
    this.selectList = null;
  }

  // getter methods

  /**
   * Is the select list a *
   * @return true if and only if the select list is a *
   */
  public boolean isStar() {
    return isStar;
  }

  /**
   * Does the select list contain DISTINCT
   * @return true if and only if the select list contains DISTINCT
   */
  public boolean isDistinct() {
    return isDistinct;
  }

  /**
   * Does the select list contain an Aggregate expression?
   * @return true if and only if the select list contains an AggrExpr
   */
  public boolean containsAggr()
  {
    if (selectList == null)
      return false;
    else
    {
      for (CEPExprNode e : selectList)
      {
        if (e.isAggr())
          return true;
      }
    }
    return false;
  }
  
  /**
   * Get the select list expressions (when select list is not a *)
   * @return the select list expressions
   */
  public CEPExprNode[] getSelectListExprs() {
    return selectList;
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

  public String toString()
  {
    StringBuffer myString = new StringBuffer(24);
    if(isDistinct && isStar)
      myString.append(" distinct * ");
    else if(!isDistinct && isStar)
      myString.append(" * ");
    else 
    {
      if(selectList!=null)
      {
        for(CEPExprNode e: selectList)
          myString.append(e.toString() + ",");
        //remove trailing comma
        myString.deleteCharAt(myString.length() - 1);
      }
    }   
    
    return myString.toString();
  }
  
  protected String toSelectString()
  {
    StringBuffer myString = new StringBuffer(24);
    if(isDistinct && isStar)
      myString.append(" distinct * ");
    else if(!isDistinct && isStar)
      myString.append(" * ");
    else 
    {
      if(selectList!=null)
      {
        for(CEPExprNode e: selectList)
        {
          if(e.isAggr())
            myString.append(e.toSelectString()+",");
          else
            myString.append(e.toString()+",");
        }
          //remove trailing comma
        myString.deleteCharAt(myString.length() - 1);
      }
      else
      {
        return "";
      }
    }   
    
    return myString.toString();
  }
  
  
  protected String getSelectListXml()
  {
    StringBuilder selectListXml = new StringBuilder(40);
    for(CEPExprNode e: selectList)
    {
      selectListXml.append(e.toExprXml());
    }
    
    return XMLHelper.buildElement(true, VisXMLHelper.selectListTag, selectListXml.toString(), null, null);
  }
  
  /**
   * Returns true if select list represented by this node contains
   * any logical CQL syntax.
   */
  public boolean isLogical()
  {
    if(selectList != null)
    {
      for(CEPExprNode e: selectList)
      {
        if(e.isLogical())
          return true;
      }
    }
   return false;
  }
}
