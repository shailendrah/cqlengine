/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPTimeWindowExprNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node corresponding to a window expression using a time 
    specification 

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    sbishnoi    03/16/11 - support variable duration range window
    sbishnoi    03/08/11 - supporting variable duration window
    skmishra    02/20/09 - adding toExprXml
    mthatte     01/26/09 - adding toQCXML()
    parujain    08/13/08 - error offset
    sbishnoi    07/24/08 - support for NANOSECOND; makind default SLIDE AMT to
                           1 nanosec
    anasrini    02/21/06 - add getter methods 
    anasrini    12/21/05 - parse tree node corresponding to a window 
                           expression using a time specification 
    anasrini    12/21/05 - parse tree node corresponding to a window 
                           expression using a time specification 
    anasrini    12/21/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPTimeWindowExprNode.java /main/5 2009/02/23 00:45:57 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.common.Constants;
import oracle.cep.common.TimeUnit;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ParserError;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node corresponding to a window expression using a time 
 * specification 
 *
 * @since 1.0
 */

public class CEPTimeWindowExprNode extends CEPWindowExprNode {

  private static final long     DEFAULT_SLIDE_AMOUNT = 1;
  private static final TimeUnit DEFAULT_SLIDE_UNIT   = TimeUnit.NANOSECOND;

  /** The RANGE amount. */
  protected long rangeAmount;
  
  /** the RANGE expression */
  protected CEPExprNode rangeExpr;

  /** The RANGE time unit */
  protected TimeUnit rangeUnit;

  /** The SLIDE amount. Default is 1 NANO SECOND */
  protected long slideAmount;

  /** The SLIDE time unit */
  protected TimeUnit slideUnit;
  
  /** flag to check if the window is a variable duration time window */
  protected boolean isVariableDurationWindow = false;
  
  /**
   * Constructor with both RANGE and SLIDE
   * @param rangeSpec the RANGE specification
   * @param slideSpec the SLIDE specification
   */
  public CEPTimeWindowExprNode(CEPTimeSpecNode rangeSpec, CEPTimeSpecNode slideSpec)
    throws CEPException
  {
    //Note: Slide amount shouldn't be specified as a variable duration
    if(slideSpec.isVariableDuration())
    {
      throw new CEPException(ParserError.INVALID_TIME_SPEC,
                             slideSpec.getStartOffset(),
                             slideSpec.getEndOffset());
    }
    
    // Get the Range Value
    if(rangeSpec.isVariableDuration())
    {
      this.rangeExpr = rangeSpec.getAmountExpr();
      this.isVariableDurationWindow = true;
    }
    else
      this.rangeAmount = rangeSpec.getAmount();
    
    this.rangeUnit   = rangeSpec.getTimeUnit();
    this.slideAmount = slideSpec.getAmount();
    this.slideUnit   = slideSpec.getTimeUnit();
    setStartOffset(rangeSpec.getStartOffset());
    setEndOffset(slideSpec.getEndOffset());
    if(!isVariableDurationWindow && this.rangeAmount == Constants.INFINITE)
      isInfinite = true;
  }

  /**
   * Constructor with RANGE and default SLIDE
   * @param rangeSpec the RANGE specification
   */
  public CEPTimeWindowExprNode(CEPTimeSpecNode rangeSpec) 
  {
    if(rangeSpec.isVariableDuration())
    {
      this.rangeExpr = rangeSpec.getAmountExpr();
      this.isVariableDurationWindow = true;
    }
    else
      this.rangeAmount = rangeSpec.getAmount();
    
    this.rangeUnit   = rangeSpec.getTimeUnit();
    this.slideAmount = DEFAULT_SLIDE_AMOUNT;
    this.slideUnit   = DEFAULT_SLIDE_UNIT;
    setStartOffset(rangeSpec.getStartOffset());
    setEndOffset(rangeSpec.getEndOffset());
    
    if(!isVariableDurationWindow && this.rangeAmount == Constants.INFINITE)
      isInfinite = true;
  }  

  // getter methods
  
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

  /**
   * Get the SLIDE amount
   * @return the SLIDE amount
   */
  public long getSlideAmount() {
    return slideAmount;
  }

  public String toString()
  {
    StringBuffer myString = new StringBuffer(20);
    myString.append("[");
    if(isVariableDurationWindow)
      myString.append(" range " + rangeExpr.toString());
    else if(rangeAmount != 0)
      myString.append(" range " + rangeAmount + " " + rangeUnit.toString()) ;
    else 
      myString.append("now");
    if(hasSlide())
      myString.append(" slide " + slideAmount + " " + slideUnit.toString());
    myString.append("]");
    return myString.toString();
  }
  
  public boolean hasSlide()
  {
    return slideAmount != DEFAULT_SLIDE_AMOUNT || slideUnit != DEFAULT_SLIDE_UNIT;
  }

  /**
   * Get the range expression
   * @return
   */
  public CEPExprNode getRangeExpr()
  {
    return rangeExpr;
  }
  
  /**
   * Is this a variable duration window
   * @return true if the node represents a variable duration window
   */
  public boolean isVariableDurationWindow()
  {
    return isVariableDurationWindow;
  }
  
  public int toQCXML(StringBuffer qXml, int operatorID)
      throws UnsupportedOperationException
  {
    int myID = operatorID;
    int inpID = operatorID - 1;
   
    StringBuilder xml = new StringBuilder(40);
    
    String inputsXml = "\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.inputTag, String.valueOf(inpID), null, null);
    inputsXml = "\n\t" + XMLHelper.buildElement(true, VisXMLHelper.inputsTag, inputsXml, null, null) + "\n";

    xml.append(inputsXml);
    xml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.cqlPropertyTag, cql_property, null, null));
    if(rangeAmount > 0)
      xml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTypeTag, VisXMLHelper.windowTypeRange, null, null));
    else
      xml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTypeTag, VisXMLHelper.windowTypeNow, null, null));
    
    StringBuilder rangeParamXml = new StringBuilder(20);
    rangeParamXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTimeValueTag, String.valueOf(this.rangeAmount), null, null));
    rangeParamXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTimeUnitTag, rangeUnit.toString(), null, null));

    if(hasSlide())
    {
      rangeParamXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowSlideValueTag, String.valueOf(slideAmount), null, null));
      rangeParamXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowSlideUnitTag, slideUnit.toString(), null, null));
    }

    xml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.windowRangeParamsTag, rangeParamXml.toString().trim(), null,null));

    qXml.append("\n" + XMLHelper.buildElement(true, VisXMLHelper.operatorTag, xml.toString().trim(), new String[]{VisXMLHelper.operatorIdAttr,VisXMLHelper.operatorTypeAttr}, new String[]{String.valueOf(myID),VisXMLHelper.windowOperator}));
    
    int returnID = myID + 1;
    return returnID;
  }
}
