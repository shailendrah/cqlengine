/* $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/SetHeartbeatTimeoutDriver.java /main/5 2008/10/24 15:50:11 hopark Exp $ */

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
    parujain    09/15/08 - multiple schema support
    parujain    09/05/08 - support offset
    sbishnoi    07/25/08 - support for nanosecond; modify conversions
    sbishnoi    12/26/07 - 
    udeshmuk    12/19/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/server/CommandDriver/SetHeartbeatTimeoutDriver.java /main/5 2008/10/24 15:50:11 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.server.CommandDriver;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPSetHeartbeatTimeoutNode;
import oracle.cep.parser.CEPTimeSpecNode;
import oracle.cep.server.Command;
import oracle.cep.server.ICommandDriver;
import oracle.cep.service.ExecContext;

public class SetHeartbeatTimeoutDriver implements ICommandDriver
{
  public void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) 
    throws CEPException
  {
    CEPSetHeartbeatTimeoutNode n = (CEPSetHeartbeatTimeoutNode) node;
    
    CEPTimeSpecNode ts = n.getTimeoutSpec();
    /** Convert duration amount to nanosecond time unit */
    long duration = oracle.cep.common.RangeConverter.interpRange(
                      ts.getAmount(), ts.getTimeUnit());
    //Tablemanager's method to update the timeout
    try {
    ec.getTableMgr().alterHeartbeatTimeout(n.getName(),
                                   schema, n.getIsStream(), duration, true);
    }catch(CEPException e)
    {
      e.setStartOffset(n.getStartOffset());
      e.setEndOffset(n.getEndOffset());
      throw e;
    }
  }
}
