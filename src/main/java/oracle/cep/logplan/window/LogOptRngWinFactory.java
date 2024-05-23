/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/window/LogOptRngWinFactory.java /main/3 2011/05/09 23:12:07 sbishnoi Exp $ */

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
    sbishnoi    03/16/11 - support for variable duration range window
    rkomurav    02/22/07 - cleanup code for window operator creation
    najain      03/22/06 - Creation
 */

/**
 *  @version $Header: LogOptRngWinFactory.java 22-feb-2007.06:33:29 rkomurav Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.window;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptRngWin;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;
import oracle.cep.semantic.TimeWindowSpec;
import oracle.cep.semantic.WindowSpec;

public class LogOptRngWinFactory extends LogOptFactory {

  /**
   * Constructor for LogOptRngWinFactory
   */
  public LogOptRngWinFactory() {
    // TODO Auto-generated constructor stub
    super();

  }

  public LogOpt newLogOpt(Object ctx)
  {
    assert ctx instanceof WindowTypeFactoryContext;

    WindowTypeFactoryContext wctx = (WindowTypeFactoryContext)ctx;
    WindowSpec wspec              = wctx.win;
    assert wspec instanceof TimeWindowSpec : wspec.getClass().getName();
    TimeWindowSpec twspec = (TimeWindowSpec)wspec;
    
    // If Window is an UNBOUNDED Range window, then no window operator
    // is needed since we have a uniform internal respresentation for
    // relations and streams (as a stream of updates).
    if (twspec.isUnboundedSpec())
      return wctx.input;
    
    LogOpt op = new LogOptRngWin(wctx.input,wctx.win);
    
    // Interpret the range expr if this is a variable duration range window
    if(twspec.isVariableDurationWindow())
    {
      oracle.cep.semantic.Expr semRangeExpr = twspec.getRangeExpr();  
            
      Expr logRangeExpr = SemQueryExprFactory.getInterpreter(semRangeExpr,
          new SemQueryExprFactoryContext(semRangeExpr, wctx.query));
            
      ((LogOptRngWin)op).setRangeExpr(logRangeExpr);
      
      ((LogOptRngWin)op).setRangeUnit(twspec.getRangeUnit());
    }
    
    return op;
  }

}


