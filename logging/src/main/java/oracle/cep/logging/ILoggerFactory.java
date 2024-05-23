/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILoggerFactory.java /main/1 2009/05/01 16:16:47 hopark Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILoggerFactory.java /main/1 2009/05/01 16:16:47 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging;

import org.apache.commons.logging.Log; 

public interface ILoggerFactory
{
  String getLoggerName(LoggerType loggerType);
  Log getLogger(LoggerType loggerType);
}
