/* $Header: LogOptExtensibleWinFactory.java 07-mar-2007.13:35:35 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/07/07 - Extensible Window Factory
    parujain    03/07/07 - Creation
 */

/**
 *  @version $Header: LogOptExtensibleWinFactory.java 07-mar-2007.13:35:35 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.window;

import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptExtensibleWin;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;
import oracle.cep.semantic.ExtensibleWindowSpec;
import oracle.cep.semantic.SemQuery;
import oracle.cep.semantic.WindowSpec;

public class LogOptExtensibleWinFactory extends LogOptFactory {

  /**
   * Constructor for LogOptExtensibleWinFactory
   */
  public LogOptExtensibleWinFactory() {
     super();

  }

  public LogOpt newLogOpt(Object ctx)
  {
    assert ctx instanceof WindowTypeFactoryContext;

    WindowTypeFactoryContext wctx = (WindowTypeFactoryContext)ctx;
    WindowSpec wspec              = wctx.win;
    assert wspec instanceof ExtensibleWindowSpec : wspec.getClass().getName();
    ExtensibleWindowSpec espec = (ExtensibleWindowSpec)wspec;
        
    LogOpt op = new LogOptExtensibleWin(wctx.input, wctx.win);
    
    if(espec.getNumParams() == 0)
      return op;
    
    oracle.cep.semantic.Expr[] exprs = espec.getParams();
    SemQuery query = wctx.query;
    SemQueryExprFactoryContext ctxt;
    
    for(int i=0; i < espec.getNumParams(); i++)
    {
      ctxt = new SemQueryExprFactoryContext(exprs[i], query);
      Expr expr = SemQueryExprFactory.getInterpreter(exprs[i], ctxt);
      ((LogOptExtensibleWin)op).addParam(i, expr);
    }
    
    return op;
  }

}
