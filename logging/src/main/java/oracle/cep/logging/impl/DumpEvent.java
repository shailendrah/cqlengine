/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/impl/DumpEvent.java /main/2 2009/02/26 21:32:10 hopark Exp $ */

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
    hopark      01/26/09 - add getName
    hopark      06/18/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/impl/DumpEvent.java /main/2 2009/02/26 21:32:10 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging.impl;

import oracle.cep.logging.ILogArea;
import oracle.cep.logging.ILogEvent;

public class DumpEvent implements ILogEvent
{
  public ILogArea getLogArea() {return null;}
  public int getValue() {return 1;}
  public String getName() {return "DUMP";}
  public int getOpDSIndex() {return 9999;}

  public static DumpEvent DUMP = new DumpEvent();
}

