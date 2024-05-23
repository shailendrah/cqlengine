/* $Header: LogOptRowWinFactory.java 22-feb-2007.06:47:19 rkomurav Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    02/22/07 - cleanup code for creation of window operator
    dlenkov     05/22/06 - Fixed it
    najain      03/22/06 - Creation
 */

/**
 *  @version $Header: LogOptRowWinFactory.java 22-feb-2007.06:47:19 rkomurav Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.window;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptRowWin;
import oracle.cep.semantic.RowWindowSpec;
import oracle.cep.semantic.WindowSpec;

public class LogOptRowWinFactory extends LogOptFactory {

  /**
   * Constructor for SelectFactory
   */
  public LogOptRowWinFactory() {
    // TODO Auto-generated constructor stub
    super();

  }

  public LogOpt newLogOpt(Object ctx)
  {
    assert ctx instanceof WindowTypeFactoryContext;
    
    WindowTypeFactoryContext wctx = (WindowTypeFactoryContext)ctx;
    WindowSpec wspec              = wctx.win;
    
    assert wspec instanceof RowWindowSpec : wspec.getClass().getName();

    LogOpt op = new LogOptRowWin(wctx.input, wctx.win);

    return op;
  }

}

