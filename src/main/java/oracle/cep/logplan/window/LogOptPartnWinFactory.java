/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/window/LogOptPartnWinFactory.java /main/3 2013/06/27 21:07:53 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    06/26/13 - bug 16571604
    sbishnoi    12/04/11 - support of variable duration partition window
    ayalaman    07/31/06 - partition window logical operator
    ayalaman    07/31/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/window/LogOptPartnWinFactory.java /main/3 2013/06/27 21:07:53 sbishnoi Exp $
 *  @author  ayalaman
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.window;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptPrtnWin;
import oracle.cep.logplan.LogOptRngWin;
import oracle.cep.logplan.LogOptSlide;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;
import oracle.cep.semantic.PartnWindowSpec;

public class LogOptPartnWinFactory extends LogOptFactory
{

  /**
   * Constructor for the partition window factory 
   */
  public LogOptPartnWinFactory()
  {
    super(); 
  }

  /**
   * Create new logical operator for the partition window operator 
   *
   * @return new logical operator 
   */
  public LogOpt newLogOpt(Object ctx)
  {
    LogOpt logOpt; 

    assert ctx instanceof WindowTypeFactoryContext; 
    WindowTypeFactoryContext wctx = (WindowTypeFactoryContext)ctx;
    
    logOpt = new LogOptPrtnWin(wctx.input, wctx.win);
    
    PartnWindowSpec winSpec 
      = (PartnWindowSpec) ((WindowTypeFactoryContext)ctx).win;
    
    if(winSpec.isVariableDurationWindow())
    {
      oracle.cep.semantic.Expr semRangeExpr = winSpec.getRangeExpr();  
      
      Expr logRangeExpr = SemQueryExprFactory.getInterpreter(semRangeExpr,
          new SemQueryExprFactoryContext(semRangeExpr, wctx.query));
            
      ((LogOptPrtnWin)logOpt).setRangeExpr(logRangeExpr);
      ((LogOptPrtnWin)logOpt).setRangeUnit(winSpec.getRangeUnit());
      ((LogOptPrtnWin)logOpt).setVariableDurationWindow(true);
    }
    
    // Note: If there is slide expression in the partition window specification:
    // SELECT....... FROM STREAM[PARTITION BY C1 ROWS N RANGE M SLIDE S]
    // then instead of duplicating slide logic in Partition Window operator,
    // we can reuse LogOptSlide which performs the same task.
    // This is why we are adding the slide operator in the query plan on the top 
    // of partition window operator.
    long numSlideNanos = ((LogOptPrtnWin)logOpt).getSlideUnits();
    if(numSlideNanos > 1L)
    {
      LogOptPrtnWin logOptPrtnWin = (LogOptPrtnWin)logOpt;
      // Make the slide value to default in partition window
      logOptPrtnWin.setSlideUnits(1L);
      return new LogOptSlide(logOpt, numSlideNanos);
    }
    else
      return logOpt; 
  }
} 
