/* $Header: WindowTypeFactoryContext.java 07-mar-2007.15:17:47 parujain Exp $ */

/* Copyright (c) 2006, 2007, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 parujain    03/07/07 - support expr for extensible windows
 najain      02/28/06 - Creation
 */

/**
 *  @version $Header: WindowTypeFactoryContext.java 07-mar-2007.15:17:47 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.window;

import oracle.cep.logplan.LogOpt;
import oracle.cep.semantic.WindowSpec;
import oracle.cep.semantic.SemQuery;

/**
 * Context for window Logical operators
 *
 * @author najain
 */
public class WindowTypeFactoryContext {
  public LogOpt     input;

  public WindowSpec win;
  
  public SemQuery query;

  public WindowTypeFactoryContext(LogOpt input, WindowSpec win, SemQuery query) {
    this.input = input;
    this.win = win;
    this.query = query;
  }
}
