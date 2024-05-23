/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILoggable.java /main/2 2008/12/10 18:55:56 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    ILoggable defines common behavior of logging target.
    The main purpose of this interface is to provide callback from
    a logger to the logging target in order to create a message.
    Some of the message contents is too heavy to construct everytime when
    trace() api is invoked. For example, elements dump.
    For those contents, the logger will call back the target only when
    an appropriate level is triggered.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/02/08 - add getLogLevelManager
    hopark      12/26/07 - handle common event
    hopark      11/07/07 - handle exception
    hopark      06/08/07 - add getInfo
    hopark      05/22/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILoggable.java /main/2 2008/12/10 18:55:56 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging;

public interface ILoggable extends IDumpable
{
  int getTargetType();
  int getTargetId();
  String  getTargetName();
  ILogLevelManager getLogLevelManager();
  
  /**
   * Dumps the information 
   * 
   */
  void trace(IDumpContext dumper, ILogEvent event, int level, Object[] args);
}
