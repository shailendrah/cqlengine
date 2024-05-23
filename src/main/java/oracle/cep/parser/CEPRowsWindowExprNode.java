/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRowsWindowExprNode.java /main/6 2011/05/19 15:28:46 hopark Exp $ */

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
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    02/20/09 - adding toExprXml
    skmishra    01/26/09 - adding toQCXML()
    parujain    08/12/08 - error offset
    parujain    06/17/08 - slide support
    anasrini    02/21/06 - add getter methods 
    anasrini    12/22/05 - parse tree node corresponding to a window 
                           expression using a rows specification 
    anasrini    12/22/05 - parse tree node corresponding to a window 
                           expression using a rows specification 
    anasrini    12/22/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPRowsWindowExprNode.java /main/5 2009/02/23 00:45:57 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for a window expression using a rows specification
 */

public class CEPRowsWindowExprNode extends CEPWindowExprNode {
  
  /** The number of rows */
  protected int numRows;

 /** slide size DEFAULT is 1*/
  protected int slide;

  protected final int DEFAULT_SLIDE_AMOUNT = 1;
  /**
   * Constructor
   * @param numRows the window size expressed in number of rows
   */
  public CEPRowsWindowExprNode(CEPIntTokenNode numRows) {
    this.numRows = numRows.getValue();
    this.slide = DEFAULT_SLIDE_AMOUNT;
    setStartOffset(numRows.getStartOffset());
    setEndOffset(numRows.getEndOffset());
  }

  /**
   * Constructor
   * @param numRows the window size expressed in number of rows
   * 
   * @param slide the slide size 
   */
  public CEPRowsWindowExprNode(CEPIntTokenNode numRows, CEPIntTokenNode slide) {
    this.numRows = numRows.getValue();
    this.slide = slide.getValue();
    setStartOffset(numRows.getStartOffset());
    setEndOffset(slide.getEndOffset());
  }


  // getter methods

  /**
   * Get the number of rows
   * @return the number of rows
   */
  public int getNumRows() {
    return numRows;
  }

  /**
   * Get the slide
   * @return slide
   */
  public int getSlide() {
    return slide;
  }

  public String toString()
  {
    StringBuffer myString = new StringBuffer(20);
    myString.append("[");
    myString.append(" rows " + numRows + " ");
    if(slide != 1)
      myString.append(" slide " + slide + " ");
    myString.append("]");
    return myString.toString();
  }
  
  public boolean hasSlide()
  {
    return slide != DEFAULT_SLIDE_AMOUNT;
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
    xml.append("\nt\t" + XMLHelper.buildElement(true, VisXMLHelper.windowTypeTag, VisXMLHelper.windowTypeRows, null, null));
    
    StringBuilder rangeParamXml = new StringBuilder(20);
    rangeParamXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowRowsValueTag, String.valueOf(this.numRows), null, null));
    if(hasSlide())
    {
      rangeParamXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowSlideValueTag, String.valueOf(slide), null, null));
      rangeParamXml.append("\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.windowSlideUnitTag, "rows", null, null));
    }

    xml.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.windowRangeParamsTag, rangeParamXml.toString().trim(), null,null));

    qXml.append("\n" + XMLHelper.buildElement(true, VisXMLHelper.operatorTag, xml.toString().trim(), new String[]{VisXMLHelper.operatorIdAttr,VisXMLHelper.operatorTypeAttr}, new String[]{String.valueOf(myID),VisXMLHelper.windowOperator}));
    int returnID = myID + 1;
    return returnID;
  }
}
