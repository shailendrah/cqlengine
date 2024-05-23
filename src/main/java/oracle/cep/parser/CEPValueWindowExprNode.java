/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPValueWindowExprNode.java /main/8 2011/10/01 09:28:39 sbishnoi Exp $ */

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
    sbishnoi    09/22/11 - support for slide in value window
    sbishnoi    09/06/11 - support of currenthour and current period vlaue
                           windows
    hopark      04/21/11 - make public to be reused in cqservice
    udeshmuk    04/28/11 - allow time_spec based syntax
    sbishnoi    02/23/11 - setting flag isWindowOnRelationAllowed
    skmishra    02/20/09 - adding toExprXml
    skmishra    01/26/09 - adding toQCXML()
    parujain    08/15/08 - error offset
    parujain    06/23/08 - value based windows
    parujain    06/23/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPValueWindowExprNode.java /main/4 2009/02/23 00:45:57 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import oracle.cep.common.TimeUnit;
import oracle.cep.common.ValueWindowType;

/**
 * Parse tree node for a window expression using a value-based specification
 */

public class CEPValueWindowExprNode extends CEPWindowExprNode {

 /** Constant value */ 
 private CEPConstExprNode constVal;

 /** Column on which value based window will work */
 private CEPExprNode column;

 /** time spec node */
 private CEPTimeSpecNode tSpec;
 
 /** The SLIDE amount. Default is 1 unit*/
 protected long slideAmount = 1;

 /** The SLIDE time unit */
 protected TimeUnit slideUnit = TimeUnit.NOTIMEUNIT;
 
/** type of value windows */
 private ValueWindowType type;

 /** Used only for ValueWindowType.CURRENT_PERIOD; Specifies start time of 
  *  window period*/
 private String currentPeriodStartTime;

 /** Used only for ValueWindowType.CURRENT_PERIOD; Specifies start time of 
  *  window period*/
 private String currentPeriodEndTime;
 
 public CEPValueWindowExprNode(CEPConstExprNode c, CEPExprNode col)
 {
   this(c, col, null);
 }
 
 public CEPValueWindowExprNode(CEPTimeSpecNode tSpec, CEPExprNode col)
 {
   this(tSpec, col, null);
 }
 
 public CEPValueWindowExprNode(CEPConstExprNode c, CEPExprNode col, CEPBigIntTokenNode slide)
 {
   this.constVal = c;
   this.column = col;
   this.tSpec  = null;
   if(slide != null)
   {
     this.slideAmount = slide.getValue();
   } 
   this.type   = ValueWindowType.GENERIC;
   setWindowOverRelationAllowed(true);
   setStartOffset(c.getStartOffset());
   setEndOffset(col.getEndOffset());   
 }
 
 public CEPValueWindowExprNode(CEPTimeSpecNode tSpec, CEPExprNode col, CEPTimeSpecNode slide)
 {
   this.constVal = null;
   this.column = col;
   this.tSpec = tSpec;
   if(slide != null)
   {
     this.slideAmount = slide.getAmount();
     this.slideUnit = slide.getTimeUnit();
   }
   this.type  = ValueWindowType.GENERIC;
   setWindowOverRelationAllowed(true);
   setStartOffset(tSpec.getStartOffset());
   setEndOffset(col.getEndOffset());
 }
 
 /**
  * Constructor for value window expression node (mainly for CurrentHour
  * , CurrentDay and CurrentPeriod
  * @param type type of value window
  * @param col value of timestamp attribute
  * @param currentPeriodStartTime
  * @param currentPeriodEndTime
  */
 public CEPValueWindowExprNode(ValueWindowType type, 
                               CEPExprNode col,
                               CEPStringTokenNode currentPeriodStartTime,
                               CEPStringTokenNode currentPeriodEndTime,
                               CEPTimeSpecNode slide)
 {
   this.type   = type;
   this.column = col;
   setWindowOverRelationAllowed(true);
   if(currentPeriodEndTime != null)
     this.currentPeriodEndTime = currentPeriodEndTime.getValue();
   if(currentPeriodStartTime != null)
     this.currentPeriodStartTime = currentPeriodStartTime.getValue(); 
   if(slide != null)
   {
     this.slideAmount = slide.getAmount();
     this.slideUnit = slide.getTimeUnit();
   }   
   setStartOffset(col.getStartOffset());
   setEndOffset(col.getEndOffset());
 }


 public CEPConstExprNode getConstVal()
 {
   return this.constVal;
 }
 
 public CEPExprNode getColumn()
 {
   return this.column;
 }

 public CEPTimeSpecNode getTimeSpec()
 {
   return this.tSpec;
 }
 
 
   /**
   * @return the type
   */
  public ValueWindowType getType()
  {
    return type;
  }
  
  /**
   * @return the currentPeriodStartTime
   */
  public String getCurrentPeriodStartTime()
  {
    return currentPeriodStartTime;
  }
  
  /**
   * @return the currentPeriodEndTime
   */
  public String getCurrentPeriodEndTime()
  {
    return currentPeriodEndTime;
  }

 
 /**
   * @return the slideAmount
   */
  public long getSlideAmount()
  {
    return slideAmount;
  }

  /**
   * @return the slideUnit
   */
  public TimeUnit getSlideUnit()
  {
    return slideUnit;
  }

public String toString()
 {
   if(this.constVal != null)
     return "[ range " + this.constVal + " on " + this.column +"]";
   else if(this.tSpec != null)
     return "[ range " + this.tSpec.toString() + " on " + this.column +"]";
   else if(this.type == ValueWindowType.CURRENT_HOUR)
     return "[ CurrentHour on " + this.column + "]";
   else if(this.type == ValueWindowType.CURRENT_PERIOD)
     return "[ CurrentPeriod (" + currentPeriodStartTime + "," + 
             currentPeriodEndTime + ") on " + this.column + "]";
   else
     return null;
 }

  public int toQCXML(StringBuffer xml, int operatorID)
      throws UnsupportedOperationException
  {
    int myID = operatorID;
    int inpID = operatorID - 1;
    xml.append("<Operator ID=\"" + myID + "\"" + "type=\"Window\">\n");
      xml.append("\t<cql-property>" + cql_property +"</cqlproperty>\n");
      xml.append("\t<inputs>\n");
        xml.append("\t\t<input>" + inpID + "</input>\n");
      xml.append("\t</inputs>\n");
      xml.append("\t<type>value-based</type>\n");
      xml.append("\t<range-params>\n");
      //FIXME: with tSpec addition does this need a change?
      //       not sure what XML tags to use.
        xml.append("\t\t<constant>" + getConstVal() + "</constant>\n");
        xml.append("\t\t<column>"+getColumn()+"</column>\n");
      xml.append("\t</range-params>\n");
    xml.append("</Operator>");
    int retID = myID + 1;
    return retID;
  }
}
