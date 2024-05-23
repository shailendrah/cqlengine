/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/window/LogOptValueWinFactory.java /main/2 2011/10/01 09:28:39 sbishnoi Exp $ */

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
    sbishnoi    09/23/11 - support for slide in value window
    sbishnoi    09/06/11 - support for currenthour and currentperiod based
                           value window
    parujain    07/01/08 - value window factory
    parujain    07/01/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/logplan/window/LogOptValueWinFactory.java /main/1 2008/07/14 22:57:01 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.window;

import oracle.cep.common.ValueWindowType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptValueWin;
import oracle.cep.logplan.LogicalPlanException;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.ExprAttr;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;
import oracle.cep.semantic.ValueWindowSpec;
import oracle.cep.semantic.WindowSpec;

public class LogOptValueWinFactory extends LogOptFactory {
 
  /**
   * Constructor for LogOptValueWinFactory
   */
  public LogOptValueWinFactory() {
    super();
  }

  @Override
  public LogOpt newLogOpt(Object ctx) throws LogicalPlanException {
    assert ctx instanceof WindowTypeFactoryContext;

    WindowTypeFactoryContext wctx = (WindowTypeFactoryContext)ctx;
    WindowSpec wspec              = wctx.win;
    assert wspec instanceof ValueWindowSpec : wspec.getClass().getName();
    
    ValueWindowSpec vspec = (ValueWindowSpec)wspec;
    
    ExprAttr col = null;
    Expr val = null;
    
    // Interpret Column Attribute
    // Please note that column can be null if window is applied on relation 
    // using ELEMENT_TIME pseudo column.
    if(vspec.getColumn() != null)
      col = (ExprAttr) SemQueryExprFactory.getInterpreter(vspec.getColumn(),
                            new SemQueryExprFactoryContext(vspec.getColumn(),
                                           wctx.query));
    
    // constVal will be null if valueWindow is not GENERIC
    if(vspec.getType() == ValueWindowType.GENERIC)
    {
      val = SemQueryExprFactory.getInterpreter(vspec.getConstVal(), 
          new SemQueryExprFactoryContext(vspec.getConstVal(),
                         wctx.query));
    }
        
    LogOptValueWin vWin = new LogOptValueWin(wctx.input, col, val);
    
    // Set CURRENT_HOUR & CURRENT_PERIOD specific attributes
    if(vspec.getType() == ValueWindowType.CURRENT_HOUR ||
        vspec.getType() == ValueWindowType.CURRENT_PERIOD)
    {
      vWin.setType(vspec.getType());
      // currentPeriodStartTime will be null in case of CURRENT_HOUR
      vWin.setCurrentPeriodStartTime(vspec.getCurrentPeriodStartTime());
      vWin.setWinSize(vspec.getWinSize());
    }
    // Set the slide amount, Default is 1
    vWin.setSlideAmount(vspec.getSlideAmount());
    
    // Set the flag whether window column is ELEMENT_TIME
    vWin.setWindowOnElementTime(vspec.isWindowOnElementTime());
	
    return vWin;
  }

}
