/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptXmlTable.java /main/3 2009/02/23 06:47:35 sborah Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      12/16/08 - handle constants
    rkomurav    02/28/08 - parameterize errors
    mthatte     12/26/07 - 
    najain      12/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptXmlTable.java /main/3 2009/02/23 06:47:35 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logplan;


import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.logplan.expr.ExprXQryFunc;
import oracle.cep.logplan.expr.factory.SemQueryExprFactory;
import oracle.cep.logplan.expr.factory.SemQueryExprFactoryContext;


public class LogOptXmlTable extends LogOpt implements Cloneable
{
  private ExprXQryFunc expr;

  public LogOptXmlTable(LogOpt input)
  {
    super(LogOptKind.LO_XMLTABLE);
    assert input != null;
    
    setNumInputs(1);
    setInput(0, input);
    input.setOutput(this);

    //set that output of this operator is a stream
    setIsStream(true);
  }

  public void addAttr(AttrNamed attr) throws LogicalPlanException 
  {
    setOutAttr(numOutAttrs, attr);
    numOutAttrs++;  
  }

  public ExprXQryFunc getXQryExpr()
  {
    return expr;
  }

  public void setXQryExpr(oracle.cep.semantic.XQryFuncExpr expr)
  {
    this.expr = 
      (ExprXQryFunc)SemQueryExprFactory.getInterpreter(expr, new SemQueryExprFactoryContext(expr, null));
  }
}
