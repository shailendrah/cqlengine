/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPIntervalConstExprNode.java /main/11 2011/09/05 22:47:26 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    07/17/11 - support of interval as per oracle sql standards
 mthatte     02/18/09 - mod toString
 hopark      02/04/09 - fix exception handling
 skmishra    08/21/08 - import, reorg
 parujain    08/14/08 - error offset
 mthatte     04/07/08 - adding toString()
 mthatte     03/28/08 - returning Object in getValue()
 parujain    12/10/07 - fix interval
 sbishnoi    06/20/07 - Make Constructor public
 parujain    10/06/06 - Interval datatype
 parujain    10/06/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPIntervalConstExprNode.java /main/10 2009/02/23 00:45:57 skmishra Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.exceptions.CEPException;

public class CEPIntervalConstExprNode extends CEPConstExprNode
{
  String         value;
  IntervalFormat format;
  
  /**
   * Constructor
   * @param str
   */
  public CEPIntervalConstExprNode(String str)
  {
    this.value  = str;
  }
  
  public CEPIntervalConstExprNode(String str, IntervalFormat format)
  {
    this.value  = str;
    this.format = format;
  }
  
  public String getExpression()
  {
    return toString();
  }
  
  @Override
  public Long getValue() throws CEPException
  {
    return IntervalConverter.parseIntervalString(value, format);
  }
  
  public String getStringValue()
  {
    return value;
  }
  
  /**
   * Get format of given interval value
   * @return
   */
  public IntervalFormat getFormat()
  {
    return format;
  }
  
  public String toString()
  {
    StringBuffer sValue = new StringBuffer("INTERVAL \"");
    sValue.append(value + "\" " + format.toString());
    
    if (alias != null)
      return " " + sValue + " as " + alias;
    else
      return " " + sValue + " ";
  }  
}
