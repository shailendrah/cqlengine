/* $Header: ExecStoreType.java 07-jun-2007.14:10:32 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/07/07 - add name
    rkomurav    06/05/07 - add binding store
    hopark      05/24/07 - Creation
 */

/**
 *  @version $Header: ExecStoreType.java 07-jun-2007.14:10:32 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.stores;

public enum ExecStoreType
{
  LINEAGE("lineage"),
  PARTNWINDOW("partnwin"),
  RELATION("relation"),
  WINDOW("window"),
  BINDING("binding");

  String m_name;
  
  ExecStoreType(String name) 
  {
    m_name = name;
  }

  public String getName() {return m_name;}
}
