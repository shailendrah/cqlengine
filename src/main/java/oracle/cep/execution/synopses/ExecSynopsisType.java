/* $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/ExecSynopsisType.java /main/6 2008/11/13 21:59:38 udeshmuk Exp $ */

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
    udeshmuk    11/04/08 - renaming patternpartnwindow.
    udeshmuk    10/16/08 - add new synopsis type.
    parujain    11/16/07 - External synopsis
    rkomurav    06/05/07 - add pattern syn
    hopark      05/24/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/synopses/ExecSynopsisType.java /main/6 2008/11/13 21:59:38 udeshmuk Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.synopses;

public enum ExecSynopsisType
{
    LINEAGE("lineage"),
    PARTNWINDOW("partnwin"),
    PVTPARTNWINDOW("privatepartnwin"),
    RELATION("relation"),
    WINDOW("window"),
    BINDING("binding"),
    EXTERNAL("external");

    String m_name;
    
    ExecSynopsisType(String name) 
    {
      m_name = name;
    }

    public String getName() {return m_name;}
}

