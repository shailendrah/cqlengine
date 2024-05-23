/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/ValueWindowInterp.java /main/8 2011/10/01 09:28:39 sbishnoi Exp $ */

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
    sbishnoi    09/06/11 - support of CurrentHour and CurrentPeriod
    sbishnoi    08/28/11 - support for interval year to month based operations
    sbishnoi    08/27/11 - adding support for interval year to month
    sbishnoi    04/28/11 - archived relation support; adding timespec in
                           valuewindow semantics
    parujain    03/12/09 - make interpreters stateless
    hopark      02/05/09 - fix ambiguous attribute
    parujain    08/26/08 - semantic exception offset
    parujain    07/01/08 - value based windows
    parujain    06/25/08 - value-based window interp
    parujain    06/25/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/semantic/ValueWindowInterp.java /main/4 2009/03/19 20:24:41 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.common.Datatype;
import oracle.cep.common.IntervalConverter;
import oracle.cep.common.RangeConverter;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.common.TimeUnit;
import oracle.cep.common.ValueWindowType;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.MetadataError;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPTimeSpecNode;
import oracle.cep.parser.CEPValueWindowExprNode;


class ValueWindowInterp extends NodeInterpreter 
{
  // NOTE: This class should be stateless in order to run DDLS in parallel 
  // Ref bug.8290135  
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
    throws CEPException {
		
    ValueWindowSpec spec = null;
    CEPValueWindowExprNode valueNode;
   
    NodeInterpreter        columnExprInterp;
    SymbolTableAttrEntry   attrs[] = null;
    assert node instanceof CEPValueWindowExprNode;
    valueNode = (CEPValueWindowExprNode)node;
    
    // Create a new symbol table for this new scope
    SymbolTable oldSymbolTable = ctx.getSymbolTable();
    String winName = ctx.getWindowRelName();
    String srcName = ctx.getWindowRelAlias();
    if (srcName == null)
      srcName = winName;
    SymbolTableSourceEntry entry = oldSymbolTable.lookupSource(srcName);
    // make sure that the local varId is same as global varId 
    // Otherwise, the attribute will not match.
    SymbolTable newSymbolTable = new SymbolTable(entry.getVarId());    
    if (entry.getSourceType() != SymbolTableSourceType.PERSISTENT) {
      // we need to copy all attributes that belong to the subquery / view
      // in the new symbol table
      attrs = ctx.getSymbolTable().getAllAttrs(ctx.getExecContext(), 
                                               entry.getVarId());      
    }
    
    ctx.setSymbolTable(newSymbolTable);
            
    try {
      // unaliased subqueries are not allowed so this assert is fine for now.
      assert (winName != null);
      
      // S [range on X.c1] X is not allowed.
      if (entry.getSourceType() == SymbolTableSourceType.PERSISTENT ) 
      {
        newSymbolTable.addPersistentSourceEntry(ctx.getExecContext(), winName,
                                                winName, ctx.getSchema());
      }
      else
      {
        newSymbolTable.addInlineSourceEntry(winName, winName, false);
        assert attrs != null;
        for (int i = 0; i < attrs.length; i++) {
          newSymbolTable.addAttrEntry(attrs[i].getVarName(), winName, 
                                      attrs[i].getAttrId(), 
                                      attrs[i].getAttrType(), 
                                      attrs[i].getAttrLen());
        }        
      }
    }
    catch(CEPException ce)
    {
      ce.setStartOffset(valueNode.getStartOffset());
      ce.setEndOffset(valueNode.getEndOffset());
      throw ce;
    }
    
    super.interpretNode(node, ctx);
    
    // Get the interpreter for column value
    columnExprInterp = InterpreterFactory.getInterpreter(valueNode.getColumn());
    
    // Flag to check if window is defined on relation and value attribute is element_time pseudo column.
    // Default is false.
    boolean isWindowOnElementTime = false;
    
    // Interpret column node where column node is C in {RANGE 5 on C};
    try
    {
      columnExprInterp.interpretNode(valueNode.getColumn(), ctx);
    }
    catch(CEPException e)
    {
      // In case if value window is defined on relation as [RANGE X on ELEMENT_TIME], then
      // ELEMENT_TIME psuedo column is not part of stream tuple spec.
      // We will set the flag to true so that ValueWindow operator can add the column in the
      // input tuple and compute windows using this column value.
      if(e.getErrorCode() == MetadataError.ATTRIBUTE_NOT_FOUND)
      {
        if(valueNode.getColumn().getExpression().trim().equals(StreamPseudoColumn.ELEMENT_TIME.getColumnName()))
          isWindowOnElementTime = true;
        else
          throw e;
      }
    }
    AttrExpr val = (AttrExpr)ctx.getExpr();
    Datatype valType = isWindowOnElementTime ? StreamPseudoColumn.ELEMENT_TIME.getColumnType() : val.getReturnType();
    
    // Column Expression should evaluate to a numeric value
    if(!((valType == Datatype.INT) 
        || (valType == Datatype.BIGINT)
        || (valType == Datatype.TIMESTAMP)
        || (valType == Datatype.FLOAT)
        || (valType == Datatype.DOUBLE)))
    {
      throw new SemanticException(
          SemanticError.INVALID_DATATYPE_FOR_VALUE_BASED_WINDOWS,
    		  valueNode.getColumn().getStartOffset(),
    		  valueNode.getColumn().getEndOffset(),
    		  new Object[]{valType});
    }
    
    ValueWindowType type = valueNode.getType();
   
    switch(type)
    {
    case GENERIC:
      Expr constVal = null;
      // perform semantic analysis of constValueNode
      if(valueNode.getConstVal() != null)
      {
        constVal = interpretConstValueNode(valueNode.getConstVal(), ctx);
        validate(valType, constVal, valueNode);
      }
      else if(valueNode.getTimeSpec() != null)
      {
        constVal = interpretTimeSpecNode(valueNode.getTimeSpec(), ctx, 
            valType);
        validate(valType, constVal, valueNode);       
      }
      else 
        assert false;
      
      spec = new ValueWindowSpec(constVal, val, isWindowOnElementTime);
      break;
      
    case CURRENT_HOUR:
      // Column Expression should evaluate to a fixed point numeric value
      // or timestamp
      if(!((valType == Datatype.INT) 
          || (valType == Datatype.BIGINT)
          || (valType == Datatype.TIMESTAMP)))
      {
        throw new SemanticException(
            SemanticError.INVALID_DATATYPE_FOR_VALUE_BASED_WINDOWS,
            valueNode.getColumn().getStartOffset(),
            valueNode.getColumn().getEndOffset(),
            new Object[]{valType});
      }
      // Note: Window size will be equal to 1 HOUR = 3600 * 1000 * 1000 * 1000
      // nanoseconds
      long winSize = IntervalConverter.HOUR * 
                     1000000000l;
      
      spec = new ValueWindowSpec(ValueWindowType.CURRENT_HOUR, 
                                 val, 
                                 null, 
                                 winSize,
                                 isWindowOnElementTime);

      break;
      
    case CURRENT_PERIOD:      
      // Column Expression should evaluate to a fixed point numeric value
      // or timestamp
      if(!((valType == Datatype.INT) 
          || (valType == Datatype.BIGINT)
          || (valType == Datatype.TIMESTAMP)))
      {
        throw new SemanticException(
            SemanticError.INVALID_DATATYPE_FOR_VALUE_BASED_WINDOWS,
            valueNode.getColumn().getStartOffset(),
            valueNode.getColumn().getEndOffset(),
            new Object[]{valType});
      }

      long currentPeriodStartTime 
        = getNanos(valueNode.getCurrentPeriodStartTime(), valueNode);
      long currentPeriodEndTime
        = getNanos(valueNode.getCurrentPeriodEndTime(), valueNode);
      
      long winsize = currentPeriodEndTime - currentPeriodStartTime;
      
      spec = new ValueWindowSpec(ValueWindowType.CURRENT_PERIOD, 
                                 val, 
                                 currentPeriodStartTime,
                                 winsize,
                                 isWindowOnElementTime); 
      break;
    default:
      assert false;
    }
    
    // Process Slide Node (if exist)
    long slideAmount   = valueNode.getSlideAmount();
    TimeUnit slideUnit = valueNode.getSlideUnit();
    
    if(slideAmount < 1)
    {
      throw new CEPException(SemanticError.INVALID_VALUE_WINDOW_SLIDE);
    }
    
    if(slideAmount > 1)
    {
      if(!((valType == Datatype.INT) 
          || (valType == Datatype.BIGINT)
          || (valType == Datatype.TIMESTAMP)))
      {
        throw new SemanticException(
            SemanticError.INVALID_VALUE_WINDOW_SLIDE,
            valueNode.getColumn().getStartOffset(),
            valueNode.getColumn().getEndOffset(),
            new Object[]{valType});
      }
    }
    
    if(slideUnit != TimeUnit.NOTIMEUNIT)
    {
      /** Convert Input Slide Amount to nanosecond timeunit */
      long slideNumNanoSeconds 
        = oracle.cep.common.RangeConverter.interpRange(slideAmount, slideUnit);
      
      slideAmount = slideNumNanoSeconds;
    }
    
    spec.setSlideAmount(slideAmount);
    
    ctx.setWindowSpec(spec);

    // Now restore the old symbol table
    ctx.setSymbolTable(oldSymbolTable);
    ctx.getSemQuery().symTable = oldSymbolTable;
    
  }
  
  private Expr interpretConstValueNode(CEPParseTreeNode valueNode,
                                       SemContext ctx)
  throws CEPException
  {
    NodeInterpreter valueExprInterp;
    valueExprInterp = InterpreterFactory.getInterpreter(valueNode);
    valueExprInterp.interpretNode(valueNode, ctx);
    Expr constVal = ctx.getExpr();
    // TODO: if constValNode represents an interval value then constVal is 
    // calculated in unit of nanoseconds, 
    // As timestamp attribute value will be calculated in millis, so 
    // it is correct to use millisecond for comparisons
    if(constVal.getReturnType() == Datatype.INTERVAL )
    {
      ConstIntervalExpr expr = (ConstIntervalExpr)constVal;
      long numNanos = expr.getValue();
      //long numMillis = numNanos / 1000000l;
      constVal = new ConstIntervalExpr(numNanos, false);  
    }
    else if(constVal.getReturnType() == Datatype.INTERVALYM)
    {
      ConstIntervalExpr expr = (ConstIntervalExpr)constVal;
      long numMonths = expr.getValue();
      long numMillis = numMonths * 30l * 24l * 3600l * 1000l;
      long numNanos = numMillis * 1000000l;
      constVal = new ConstIntervalExpr(numNanos, false);   
    }        
    return constVal;
  }
  
  private Expr interpretTimeSpecNode(CEPTimeSpecNode timeSpec, SemContext ctx,
      Datatype colType) throws CEPException
  {

    // Value window's value is specified as a constant with time unit
    // We will convert that duration into NANOSECONDS
    long valueWinIntervalNanos = RangeConverter.interpRange(
        timeSpec.getAmount(), timeSpec.getTimeUnit());

    Expr constVal = null;
    // Convert timespec to INTERVAL expression if the column is timestamp
    // else convert timespec to BIGINT
    if (colType == Datatype.TIMESTAMP)
    {
      // Create constant interval expression representing number of nanoseconds
      constVal = new ConstIntervalExpr(valueWinIntervalNanos, false);
    } 
    else
    {
      constVal = new ConstBigintExpr(valueWinIntervalNanos);
    }

    return constVal;
  }
  
  private void validate(AttrExpr val, 
                        Expr constVal,
                        CEPValueWindowExprNode valueNode) 
    throws CEPException
  {
    validate(val.getReturnType(),constVal,valueNode);
  }
  
  private void validate(Datatype valType, 
                        Expr constVal, 
                        CEPValueWindowExprNode valueNode) 
    throws CEPException
  {
    // Validation Check # 1. Constant value should be of numeric type
    if(!((constVal.getReturnType() == Datatype.INT) 
        || (constVal.getReturnType() == Datatype.BIGINT)
        || (constVal.getReturnType() == Datatype.INTERVAL)
        || (constVal.getReturnType() == Datatype.INTERVALYM)
        || (constVal.getReturnType() == Datatype.FLOAT)
        || (constVal.getReturnType() == Datatype.DOUBLE)))
    {
     throw new SemanticException(
       SemanticError.INVALID_DATATYPE_FOR_VALUE_BASED_WINDOWS,
       valueNode.getStartOffset(),
       valueNode.getEndOffset(),
       new Object[]{constVal.getReturnType()});
    }
  
    // Validation check # 2. If column is of type INT/BIGINT then the constant
    // value should also be either INT OR BIGINT
    if((valType == Datatype.INT) ||
       (valType == Datatype.BIGINT))
    {
      if(!((constVal.getReturnType() == Datatype.INT) ||
          (constVal.getReturnType() == Datatype.BIGINT)))
     {
       throw new SemanticException(
         SemanticError.INCONSISTENT_DATATYPES_IN_VALUE_BASED_WINDOWS,
         valueNode.getColumn().getStartOffset(),
         valueNode.getEndOffset(),
         new Object[]{valType, constVal.getReturnType()});
     }
    }
    else if (valType == Datatype.TIMESTAMP) 
    {
      // Validation Check # 3. If column is of type TIMESTAMP then the constant
      // value should be an INTERVAL OR INTERVALYM type value
      if(constVal.getReturnType() != Datatype.INTERVAL &&
         constVal.getReturnType() != Datatype.INTERVALYM)
      {
        throw new SemanticException(
          SemanticError.INCONSISTENT_DATATYPES_IN_VALUE_BASED_WINDOWS,
          valueNode.getColumn().getStartOffset(),
          valueNode.getEndOffset(),
          new Object[]{valType, constVal.getReturnType()});
      }      
    }
    else if ((valType == Datatype.FLOAT) ||
            (valType == Datatype.DOUBLE)) 
    {
      // Validation check #4. If column is of type FLOAT/DOUBLE the constant
      // value should be of type FLOAT OR DOUBLE
      if(!((constVal.getReturnType() == Datatype.FLOAT) ||
           (constVal.getReturnType() == Datatype.DOUBLE)))
      {
         throw new SemanticException(
               SemanticError.INCONSISTENT_DATATYPES_IN_VALUE_BASED_WINDOWS,
               valueNode.getColumn().getStartOffset(),valueNode.getEndOffset(),
                new Object[]{valType, constVal.getReturnType()});
       }
  
    }  
  }

  
  private long getNanos(String clockTime, CEPParseTreeNode node) 
    throws CEPException
  {
    boolean isError = false;
    
    int numHours = 0;
    int numMins  = 0;
    
    if(clockTime.length() != 4)
    {       
      isError = true;
      LogUtil.fine(LoggerType.TRACE, "In currentPeriod window, " +
          "length of either start time or endtime is not equal to four");
    }
    try
    {
      numHours = Integer.parseInt(clockTime.substring(0, 2));
      numMins  = Integer.parseInt(clockTime.substring(2));
      
      if(numHours < 0 || numHours > 23 || numMins < 0 || numMins > 59)
      {
        isError = true;
        LogUtil.fine(LoggerType.TRACE, "In CurrentPeriod Window, period is" +
        		"specified beyond range. valid range is [0000] to [2359]");
      }
        
    }
    catch(NumberFormatException e)
    {
      isError = true;
      LogUtil.fine(LoggerType.TRACE, "Either startTime or endTime in" +
          " CurrentPeriod window contains non-numeric characters");
    }
    finally
    {
      if(isError)
      {
        throw new SemanticException(
            SemanticError.INVALID_VALUE_WINDOW_PERIOD,
            node.getStartOffset(), 
            node.getEndOffset());
      }
    }
    
    long numSeconds = numHours * IntervalConverter.HOUR + 
                      numMins * IntervalConverter.MINUTE;
    
    return numSeconds * 1000000000l;
  }
    
}
