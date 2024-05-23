/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/windows/ExtensibleWindow.java /main/8 2011/10/03 01:51:59 sbishnoi Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                           st_pcbpel_11.1.1.4.0
    sbishnoi    09/13/11 - cleanup
    hopark      10/09/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    sbishnoi    08/01/08 - support for nanosecond timestamp
    hopark      05/16/07 - remove printStackTrace
    parujain    03/23/07 - cleanup
    parujain    03/08/07 - Extensible Window Specification
    parujain    03/08/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/internals/windows/ExtensibleWindow.java /main/6 2008/10/24 15:50:11 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.internals.windows;

import java.io.IOException;

import java.util.logging.Level;

import oracle.cep.common.EventTimestamp;
import oracle.cep.exceptions.CEPException;
import oracle.cep.extensibility.expr.ExprKind;
import oracle.cep.extensibility.windows.GenericTimeWindow;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.phyplan.expr.Expr;
import oracle.cep.phyplan.window.PhyExtensibleWinSpec;
import oracle.cep.phyplan.window.PhyWinSpec;
import oracle.cep.service.ExecContext;

public class ExtensibleWindow extends Window
{
  GenericTimeWindow gtw;
  
  public ExtensibleWindow(ExecContext ec, PhyWinSpec spec)
  {
    assert spec instanceof PhyExtensibleWinSpec;
    PhyExtensibleWinSpec winspec = (PhyExtensibleWinSpec)spec;
    int winId = winspec.getWindowId();
    try {
      gtw = ec.getWindowMgr().getWindowInstance(winId);
      int num = winspec.getNumParams();
      if(num  > 0)
      {
        Expr[] params = winspec.getParams();
        Object[] obj = new Object[num];
        for(int i=0; i<num; i++)
        {
          assert params[i].getKind() == ExprKind.CONST_VAL;
          obj[i] = params[i].getObject();
        }
        gtw.setInputParams(obj);
      }      
      
    }catch(CEPException ce)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ce);
      gtw = null;
    } catch(IOException ie)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.FINE, ie);
    }
    
  }

  @Override
  public boolean visibleW(EventTimestamp ts, EventTimestamp visTs) {
    return gtw.visibleW(ts, visTs);
  }

  @Override
  public boolean expiredW(EventTimestamp ts, EventTimestamp expTs) {
    return gtw.expiredW(ts, expTs);
  }
}
