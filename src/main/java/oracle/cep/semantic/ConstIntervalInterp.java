/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ConstIntervalInterp.java /main/2 2011/09/05 22:47:27 sbishnoi Exp $ */

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
    sbishnoi    08/22/11 - support for standards based interval datatype
    parujain    10/09/06 - Interval datatype support
    parujain    10/09/06 - Creation
 */

/**
 *  @version $Header: ConstIntervalInterp.java8580 09-oct-2006.11:53:27 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPIntervalConstExprNode;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.IntervalFormat;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.DataStructuresError;

class ConstIntervalInterp extends NodeInterpreter
{

  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException 
  {
    CEPIntervalConstExprNode intervalNode;
    ConstIntervalExpr intervalExpr;
    
    assert node instanceof CEPIntervalConstExprNode;
    intervalNode = (CEPIntervalConstExprNode)node;
    
    super.interpretNode(node, ctx);
    
    // Interpret the constant interval expression
    
    // 1. Get the string interval value
    String value = intervalNode.getStringValue();
    
    // 2. Get the interval value format
    IntervalFormat format = intervalNode.getFormat();
    
    // 3. Check if the given interval is YearToMonth or DayToSecond Format;
    boolean isYearToMonthInterval = format.isYearToMonthInterval();
    
    // 4. Parse the string interval value according to the given format.
    long intervalValue;
    try
    {
    if(isYearToMonthInterval)
    {
      intervalValue = IntervalConverter.parseYToMIntervalString(value, format);
    }
    else
    {
      intervalValue = IntervalConverter.parseDToSIntervalString(value, format);
    }
    }
    catch(CEPException ce)
    {
      throw ce;
    }
    catch(NumberFormatException e)
    {
      throw new CEPException(DataStructuresError.INVALID_INTERVAL);
    }
    
    intervalExpr = new ConstIntervalExpr(intervalValue, isYearToMonthInterval);
    intervalExpr.setFormat(format);
    intervalExpr.setIsYearToMonthInterval(isYearToMonthInterval);
    ctx.setExpr(intervalExpr);  
  }
}
