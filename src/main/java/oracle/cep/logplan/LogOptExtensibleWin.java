/* $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptExtensibleWin.java /main/2 2009/02/23 06:47:35 sborah Exp $ */

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
    parujain    03/07/07 - Extensible Window
    parujain    03/07/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptExtensibleWin.java /main/2 2009/02/23 06:47:35 sborah Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import oracle.cep.logplan.expr.Expr;
import oracle.cep.semantic.ExtensibleWindowSpec;
import oracle.cep.semantic.WindowSpec;

/**
 * Logical Operator form Extensible Windows
 * @author parujain
 *
 */
public class LogOptExtensibleWin extends LogOpt implements Cloneable
{

  private int winId;
  
  private Expr[] params;

  public LogOptExtensibleWin()
  {
     super();
  }

  public LogOptExtensibleWin(LogOpt input, WindowSpec win)
  {
    super(LogOptKind.LO_EXTENSIBLE_WIN);
    assert input != null;
    assert input.getIsStream() == true;
    assert win instanceof ExtensibleWindowSpec;

    copy(input);

    setNumInputs(1);
    setInput(0, input);
    setIsStream(false);
    
    ExtensibleWindowSpec espec = (ExtensibleWindowSpec)win;
    winId = espec.getWindowId();
    if(espec.getNumParams() > 0)
      params = new Expr[espec.getNumParams()];
    else
      params = null;

    input.setOutput(this);
  }

  public void setWindowId(int id)
  {
    this.winId = id;
  }

  public int getWindowId()
  {
    return winId;
  }

  public void setParams(Expr[] exprs)
  {
    this.params = exprs;
  }
  
  public void addParam(int pos, Expr expr)
  {
    assert pos <= params.length;
    params[pos] = expr;
  }
  
  
  public Expr[] getParams()
  {
    return params;
  }
  
  public int getNumParams()
  {
    if(params == null)
      return 0;
      
    return params.length;
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public LogOptExtensibleWin clone() throws CloneNotSupportedException
  {
    LogOptExtensibleWin op = (LogOptExtensibleWin) super.clone();
    return op;
  }

  public void updateSchemaStreamCross()
  {
    numOutAttrs = 0;
    for (int i = 0; i < getNumInputs(); i++)
    {
      LogOpt inp = getInputs().get(i);
      assert (inp != null);

      for (int a = 0; a < inp.getNumOutAttrs(); a++)
      {
        setOutAttr(numOutAttrs, inp.getOutAttrs().get(a));
        numOutAttrs++;
      }
    }
  }

  // toString method override
  public String toString()
  {

    StringBuilder sb = new StringBuilder();

    sb.append("<ExtensibleWindowLogicalOperator>");

    // Dump the common fields
    sb.append(super.toString());

    if(params != null)
    {
      for(int i=0; i<params.length; i++)
      {
        sb.append("Parameter no: " +i +"is" +params[i].toString());
      }
    }
    
    sb.append("</ExtensibleWindowLogicalOperator>");

    return sb.toString();
  }
}

