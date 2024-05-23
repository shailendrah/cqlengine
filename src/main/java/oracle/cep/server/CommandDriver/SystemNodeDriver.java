/* $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/SystemNodeDriver.java /main/7 2008/12/10 18:55:58 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/08/08 - change setScheduler to setSchedulerClassName
    hopark      10/10/08 - remove statics
    hopark      10/07/08 - use execContext to remove statics
    parujain    09/18/08 - multiple schema support
    hopark      03/18/08 - reorg config
    sbishnoi    02/21/08 - adding START_CALLOUT
    najain      07/09/07 - remove setThreaded
    hopark      06/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/SystemNodeDriver.java /main/7 2008/12/10 18:55:58 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSystemNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;

public class SystemNodeDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    CEPSystemNode n = (CEPSystemNode) node;
    String[]      callOutEPR;
    
    ConfigManager cfgMgr = ec.getServiceManager().getConfigMgr();
    switch(n.getSystemKind()) {
      case RUNTIME: {
        long runTime = Long.parseLong(n.getValue());
        cfgMgr.setSchedRuntime(runTime);
        break;
      }
      case SCHEDNAME: {
        cfgMgr.setSchedulerClassName(n.getValue());
        break;
      }
      case TIMESLICE: {
        int timeSlice = Integer.parseInt(n.getValue());
        cfgMgr.setSchedTimeSlice(timeSlice);
        break;
      }
      case START_CALLOUT: {
        callOutEPR = CallOutHelper.parseEpr(n.getValue());
        CallOutHelper.startCallOut(callOutEPR);
        break;
      }
      default:
        break;
    }
  }
  
}

