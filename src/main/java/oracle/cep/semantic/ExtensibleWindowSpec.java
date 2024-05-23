/* $Header: ExtensibleWindowSpec.java 06-mar-2007.15:33:46 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    03/06/07 - Extensible Window Spec
    parujain    03/06/07 - Creation
 */

/**
 *  @version $Header: ExtensibleWindowSpec.java 06-mar-2007.15:33:46 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.semantic;

import oracle.cep.common.WindowType;

/**
 * Post semantic analysis representation of an extensible window expresssion 
 * using a a generic range and time specification
 *
 */

public class ExtensibleWindowSpec implements WindowSpec {
  
  /** internal identifier of the window */
  private int winId;
  
  /** parameters passed by the user */
  private Expr[] params;
  
  public ExtensibleWindowSpec()
  {
    super();
    params = null;
  }
  
  
  public void setId(int id)
  {
    this.winId = id;
  }
  
  public void setParams(Expr[] exprs)
  {
    this.params = exprs;
  }
  
  public WindowType getWindowType() {
    return WindowType.EXTENSIBLE;
  }
  
  public int getWindowId()
  {
    return winId;
  }
  
  public int getNumParams()
  {
    if(params == null)
      return 0;
    
    return params.length;
  }
  
  public Expr[] getParams()
  {
    return params;
  }
  
   
}

  
