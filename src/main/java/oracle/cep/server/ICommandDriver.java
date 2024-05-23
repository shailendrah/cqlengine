/* $Header: pcbpel/cep/server/src/oracle/cep/server/ICommandDriver.java /main/3 2008/10/24 15:50:11 hopark Exp $ */

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
    parujain    09/18/08 - multiple schema support
    hopark      06/06/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/server/ICommandDriver.java /main/3 2008/10/24 15:50:11 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.server;

import oracle.cep.exceptions.CEPException;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.service.ExecContext;

public interface ICommandDriver
{
  void execute(ExecContext ec, CEPParseTreeNode node, Command c, String schema) throws CEPException;
}
  
