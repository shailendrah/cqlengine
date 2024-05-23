/* $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPStringConstExprNode.java /main/9 2009/04/13 14:26:25 skmishra Exp $ */

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
    skmishra    04/07/09 - adding isSingleQuote
    skmishra    02/18/09 - remove toString
    skmishra    08/21/08 - import, reorg
    sbishnoi    07/23/08 - modify toString to include quotes in string value
    mthatte     04/07/08 - adding toString()
    mthatte     03/28/08 - returning Object in getValue()
    sbishnoi    06/19/07 - Make constructor public
    anasrini    02/23/06 - add getter methdsods 
    anasrini    12/22/05 - parse tree node for a string constant 
    anasrini    12/22/05 - parse tree node for a string constant 
    anasrini    12/22/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPStringConstExprNode.java /main/9 2009/04/13 14:26:25 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node for a string constant 
 */

public class CEPStringConstExprNode extends CEPConstExprNode {

  /** The constant value */
  private String value;
  private boolean isSingleQuote = false;

  /**
   * Constructor
   * @param value the constant value
   */
  public CEPStringConstExprNode(String value) {
    this.value = value;
  }

  /**
   * Get the String constant value
   * @return the String constant value
   */
  public String getValue() {
    return value;
  }

  /**
   * @return the isSingleQuote
   */
  public boolean isSingleQuote()
  {
    return isSingleQuote;
  }

  /**
   * @param isSingleQuote the isSingleQuote to set
   */
  public void setSingleQuote(boolean isSingleQuote)
  {
    this.isSingleQuote = isSingleQuote;
  }

  public String toString() 
  {
    if(!isSingleQuote)
    {
      if(alias != null)
        myString = "\"" + value + "\" AS " + alias;
      else
        myString = "\"" + value + "\"";
    }
    
    else
    {
      if(alias != null)
        myString = "'" + value + "\' AS " + alias;
      else
        myString = "'" + value + "'";
    }
    
    return myString;
  }

  public String getExpression()
  {
    if(!isSingleQuote)
      myString =  "\"" + value + "\"";
    else
      myString =  "'" + value + "'";
    
    return myString;
  }
}
