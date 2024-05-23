/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPatternMeasuresNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

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
    skmishra    02/02/09 - adding toVisXML
    parujain    08/11/08 - error offset
    sbishnoi    06/07/07 - fix xlint warning
    rkomurav    05/27/07 - 
    anasrini    05/14/07 - Support for MEASURES sub clause
    anasrini    05/14/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPPatternMeasuresNode.java /main/4 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node corresponding to the MEASURES sub clause of the 
 * MATCH_RECOGNIZE clause (pattern support)
 */

public class CEPPatternMeasuresNode implements CEPParseTreeNode {

  /** The list of measure expressions */
  protected CEPExprNode[] measureList;
  
  protected int startOffset = 0;
  
  protected int endOffset = 0;

  /**
   * Constructor
   * @param measureList measure list
   */
  public CEPPatternMeasuresNode(List<CEPExprNode> measureList) {
    this.measureList =
      (CEPExprNode[])(measureList.toArray(new CEPExprNode[0]));
    if(!measureList.isEmpty())
    {
      setStartOffset(measureList.get(0).getStartOffset());
      setEndOffset(measureList.get(measureList.size()-1).getEndOffset());
    }
  }

  // getter methods

  /**
   * Get the measure list expressions
   * @return the measure list expressions
   */
  public CEPExprNode[] getMeasureListExprs() {
    return measureList;
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
    StringBuilder myString = new StringBuilder(50);
    for(CEPExprNode e : measureList)
      myString.append(e.toString()+",");
    myString.deleteCharAt(myString.length() - 1);
    return myString.toString();
  }
  
  /**
   * TODO: Verify if complete?
   * @return
   */
  public String toVisualizerXML()
  {
    StringBuilder q = new StringBuilder(50);
      for(CEPExprNode c: measureList)
      {
        q.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.measureAttrTag, c.toString().trim(), null, null));
      }
    return XMLHelper.buildElement(true, VisXMLHelper.measuresListTag, q.toString().trim(), null, null);
  }

}
