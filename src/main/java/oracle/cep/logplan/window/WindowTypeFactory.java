/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/window/WindowTypeFactory.java /main/4 2011/10/01 09:28:39 sbishnoi Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/29/11 - should throw exception
    parujain    07/01/08 - value based windows
    parujain    03/07/07 - Extensible Window
    ayalaman    07/31/06 - add partition window factory
    najain      05/30/06 - support for NOW windows 
    najain      02/28/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/logplan/window/WindowTypeFactory.java /main/3 2008/07/14 22:57:01 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan.window;

import java.util.HashMap;
import oracle.cep.common.WindowType;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogicalPlanException;

/**
 * Factory for the different type of windows (range/row etc.)
 * <p>
 * This is private to the logical operators module.
 *
 * @since 1.0
 */

public class WindowTypeFactory {

  private static HashMap<WindowType, LogOptFactory> interpMap;

  static {
    populateInterpMap();
  }

  static void populateInterpMap() {
    interpMap = new HashMap<WindowType, LogOptFactory>();

    interpMap.put(WindowType.RANGE, new LogOptRngWinFactory());
    interpMap.put(WindowType.ROW, new LogOptRowWinFactory());
    interpMap.put(WindowType.NOW, new LogOptNowWinFactory());
    interpMap.put(WindowType.PARTITION, new LogOptPartnWinFactory());
    interpMap.put(WindowType.EXTENSIBLE, new LogOptExtensibleWinFactory());
    interpMap.put(WindowType.VALUE, new LogOptValueWinFactory());
  }

  public static LogOpt getInterpreter(WindowType winType, 
			       WindowTypeFactoryContext ctx) throws LogicalPlanException {

    LogOptFactory o = interpMap.get(winType);
    assert o != null;

    return o.newLogOpt(ctx);
  }
}


