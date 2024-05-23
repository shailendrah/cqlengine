/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/WindowHelper.java /main/4 2008/10/24 15:50:12 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/07/08 - use execContext to remove statics
    hopark      10/12/07 - support partition window
    parujain    03/23/07 - cleanup
    parujain    03/08/07 - Window evaluator
    parujain    03/08/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/WindowHelper.java /main/4 2008/10/24 15:50:12 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.internals.windows.RowRangeWindow;
import oracle.cep.execution.internals.windows.Window;
import oracle.cep.execution.internals.windows.ExtensibleWindow;
import oracle.cep.execution.internals.windows.RngWindow;
import oracle.cep.phyplan.window.PhyWinSpec;
import oracle.cep.phyplan.window.WinKind;
import oracle.cep.service.ExecContext;

public class WindowHelper {

  
  public static Window instantiateWindow(ExecContext ec, PhyWinSpec winspec)
  {
    WinKind kind = winspec.getWindowKind();
    switch(kind)
    {
      case RANGE:
         return(new RngWindow(winspec));
      case EXTENSIBLE:
         return(new ExtensibleWindow(ec, winspec));
      case PARTITION:
         return(new RowRangeWindow(winspec));
      default : assert false : kind;
    }
    return null;
  }
 
}
