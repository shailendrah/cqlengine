/* $Header: LogOptNowWinFactory.java 30-may-2006.16:07:01 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      05/30/06 - Creation
 */

/**
 *  @version $Header: LogOptNowWinFactory.java 30-may-2006.16:07:01 najain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan.window;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptNowWin;

public class LogOptNowWinFactory extends LogOptFactory {

  /**
   * Constructor for LogOptNowWinFactory
   */
  public LogOptNowWinFactory() {
    super();
  }

  public LogOpt newLogOpt(Object ctx) {
    assert ctx instanceof WindowTypeFactoryContext;
    
    LogOpt op = new LogOptNowWin(((WindowTypeFactoryContext)ctx).input);
    return op;
  }

}

