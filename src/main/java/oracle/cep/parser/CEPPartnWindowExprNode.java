/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPartnWindowExprNode.java /main/13 2013/05/13 06:00:34 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    05/11/13 - bug 16748056
    sbishnoi    11/27/11 - support of variable duration partition window
    hopark      04/21/11 - make constructor public
    hopark      04/21/11 - make public to be reused in cqservice
    sbishnoi    03/15/11 - check if the timespec is specified as constant
    skmishra    02/20/09 - adding toExprXml
    mthatte     01/26/09 - adding toQCXML()
    parujain    08/12/08 - error offset
    sbishnoi    07/25/08 - support for NANOSECOND; makind default SLIDE AMT to
                           1 nanosec
    sbishnoi    06/07/07 - fix xlint warning
    hopark      12/15/06 - add range
    ayalaman    07/29/06 - Partition window specification
    ayalaman    07/29/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPartnWindowExprNode.java /main/13 2013/05/13 06:00:34 sbishnoi Exp $
 *  @author  ayalaman
 *  @since   release specific 1.0
 */

package oracle.cep.parser; 


import java.util.List;

import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ParserError;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for the partition specification
 *    PARTITION BY attr1, attr2 ROWS n
 *
 * @author ayalaman
 * @since  1.0
 *
 */
public class CEPPartnWindowExprNode extends CEPWindowExprNode
{
  private static final long     DEFAULT_SLIDE_AMOUNT = 1;
  private static final TimeUnit DEFAULT_SLIDE_UNIT   = TimeUnit.NANOSECOND;

  /** Partition by clause for the Partition specification */ 	
  protected CEPAttrNode[] partByClause = null; 
  
  /** The number of rows in each partition */ 
  protected int           numRows; 

  /** The RANGE amount. */
  protected long rangeAmount = -1;

  /** The RANGE time unit */
  protected TimeUnit rangeUnit;

  /** The SLIDE amount. Default is 1 NANO SECOND */
  protected long slideAmount;

  /** The SLIDE time unit */
  protected TimeUnit slideUnit;
  
  /** the RANGE expression */
  protected CEPExprNode rangeExpr;
  
  /** flag to check if the variable range window*/
  protected boolean isVariableDuration;
  
  public CEPPartnWindowExprNode (List<CEPAttrNode> partByCls, CEPIntTokenNode numRows)
  {
    this.numRows = numRows.getValue();
    setPartBy(partByCls);
    if(partByCls != null)
      setStartOffset(partByCls.get(0).getStartOffset());
    else
      setStartOffset(numRows.getStartOffset());
    setEndOffset(numRows.getEndOffset());
  }

  public CEPPartnWindowExprNode (List<CEPAttrNode> partByCls, CEPIntTokenNode numRows, 
    CEPTimeSpecNode rangeSpec) throws CEPException
  {
    this.numRows = numRows.getValue();
    // Get the range expression if the range is variable
    if(rangeSpec.isVariableDuration())
    {
      this.isVariableDuration = true;
      this.rangeExpr = rangeSpec.getAmountExpr(); 
    }
    else
    {
      this.isVariableDuration = false;
      this.rangeAmount = rangeSpec.getAmount(); 
    }    
    this.rangeUnit   = rangeSpec.getTimeUnit();
    this.slideAmount = DEFAULT_SLIDE_AMOUNT;
    this.slideUnit   = DEFAULT_SLIDE_UNIT;
    
    setPartBy(partByCls);
    if(partByCls != null)
      setStartOffset(partByCls.get(0).getStartOffset());
    else
      setStartOffset(numRows.getStartOffset());
    setEndOffset(rangeSpec.getEndOffset());
  }

  public CEPPartnWindowExprNode (List<CEPAttrNode> partByCls, CEPIntTokenNode numRows,
    CEPTimeSpecNode rangeSpec, CEPTimeSpecNode slideSpec) throws CEPException
  {
    this.numRows = numRows.getValue();
    
    // Get the range expression if the range is variable
    if(rangeSpec.isVariableDuration())
    {
      this.isVariableDuration = true;
      this.rangeExpr = rangeSpec.getAmountExpr(); 
    }
    else
    {
      this.isVariableDuration = false;
      this.rangeAmount = rangeSpec.getAmount(); 
    }    
    
    //Remember: Slide shouldn't be a variable
    if(slideSpec.isVariableDuration())
    {
      throw new CEPException(ParserError.INVALID_TIME_SPEC,
                             slideSpec.getStartOffset(),
                             slideSpec.getEndOffset());
    }    
    
    this.rangeUnit   = rangeSpec.getTimeUnit();
    this.slideAmount = slideSpec.getAmount();
    this.slideUnit   = slideSpec.getTimeUnit();
    setPartBy(partByCls);
    if(partByCls != null)
      setStartOffset(partByCls.get(0).getStartOffset());
    else
      setStartOffset(numRows.getStartOffset());
    setEndOffset(slideSpec.getEndOffset());
  }

  private void setPartBy(List<CEPAttrNode> partByCls)
  {
    if (partByCls != null)
    {
      int noOfAttrs = partByCls.size();  // no of attrs in the part by cls.  
  
      this.partByClause = (CEPAttrNode[])(partByCls.toArray(
                                    new CEPAttrNode[noOfAttrs])); 
    }
  }
  /**
   * Get the partition by clause
   *
   * @return  the partition by clause 
   */
  public CEPAttrNode[] getPartByClause()
  {
    return partByClause; 
  }
  
  /**
   * Get the number of rows in each partition 
   *
   * @return  the number of rows in each partition 
   */
  public int getNumRows()
  {
    return numRows;   
  }
  
  /**
     * Returns if RANGE clause is given
     * @return
     */
  public boolean hasRange()
  {
      return rangeAmount >= 0 || rangeExpr != null;
  }

  /**
   * Get the RANGE unit
   * @return the RANGE time unit
   */
  public TimeUnit getRangeUnit() {
    return rangeUnit;
  }

  /**
   * Get the RANGE amount
   * @return the RANGE amount
   */
  public long getRangeAmount() {
    return rangeAmount;
  }

  /**
   * Get the SLIDE unit
   * @return the SLIDE time unit
   */
  public TimeUnit getSlideUnit() {
    return slideUnit;
  }

  public boolean hasSlide()
  {
    return slideAmount != DEFAULT_SLIDE_AMOUNT;
  }
  /**
   * Get the SLIDE amount
   * @return the SLIDE amount
   */
  public long getSlideAmount() {
    return slideAmount;
  }
  
  private String getPartBySymbols()
  {
    StringBuffer myString = new StringBuffer(20);
    for(CEPAttrNode partSymbol: this.partByClause)
    {
      myString.append(partSymbol.toString() + ",");
    }
    //remove trailing ','
    myString.deleteCharAt(myString.length() - 1);
    return myString.toString();
  }
  
  
  /**
   * @return the rangeExpr
   */
  public CEPExprNode getRangeExpr()
  {
    return rangeExpr;
  }

  /**
   * @return the isVariableDuration
   */
  public boolean isVariableDuration()
  {
    return isVariableDuration;
  }

  public String toString()
  {
    StringBuffer myString = new StringBuffer(50);
    myString.append("[");
    myString.append("partition by " + getPartBySymbols());
    myString.append(" rows " + numRows + " ");
    if(hasRange())
    {
      if(isVariableDuration())
      {
        myString.append(" variable range " + getRangeExpr() + " " + getRangeUnit().toString()); 
      }
      else
      {
        myString.append(" fixed range " + getRangeAmount() + " " + getRangeUnit().toString());  
      }      
      if(hasSlide())
      {
        myString.append(" slide " + slideAmount + " " + slideUnit.toString() + " ");
      }
    }
    myString.append("]");
    return myString.toString();
  }

  public int toQCXML(StringBuffer qXml, int operatorID)
      throws UnsupportedOperationException
  {
    int myID = operatorID;
    int inpID = operatorID - 1;
    StringBuilder xml = new StringBuilder(100);
    
    
    String inputsXml = "\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.inputTag, String.valueOf(inpID), null, null);
    inputsXml = "\n\t" + XMLHelper.buildElement(true, VisXMLHelper.inputsTag, inputsXml, null, null) + "\n";

    xml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.cqlPropertyTag, cql_property, null, null));
    xml.append(inputsXml);
    xml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.partitionByTag, getPartBySymbols().trim(), null, null));

    StringBuilder myRangeXml = new StringBuilder(20);
    myRangeXml.append(XMLHelper.buildElement(true, VisXMLHelper.windowRowsValueTag, String.valueOf(numRows), null, null));
    if(hasRange())
    {
      // TODO: Fix toQCXML() to include variable duration range window 
      xml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTypeTag, VisXMLHelper.windowTypeRowTime, null, null));
      myRangeXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTimeValueTag, String.valueOf(getRangeAmount()), null, null));
      myRangeXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTimeUnitTag, getRangeUnit().toString(), null, null));
      if(hasSlide())
      {
        myRangeXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowSlideValueTag, String.valueOf(slideAmount), null, null));
        myRangeXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowSlideUnitTag, slideUnit.toString(), null, null));
      }
    }
    
    else
    {
      xml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTypeTag, VisXMLHelper.windowTypeRows, null, null));
    }
    
    xml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.windowRangeParamsTag, myRangeXml.toString().trim(), null, null));
    
    qXml.append(XMLHelper.buildElement(true, VisXMLHelper.operatorTag, xml.toString().trim(), new String[]{VisXMLHelper.operatorIdAttr, VisXMLHelper.operatorTypeAttr}, new String[] {String.valueOf(myID),VisXMLHelper.windowOperator}));
    ++myID;
    return myID;
  }
}
