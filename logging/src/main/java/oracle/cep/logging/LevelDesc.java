/* $Header: LevelDesc.java 19-jun-2008.18:24:25 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/18/08 - refactor
    hopark      02/05/08 - remove full systemstate dump
    hopark      01/09/08 - add levels for cache
    hopark      12/27/07 - add non-verbose dumps for index, store, queue.
    hopark      12/13/07 - fix typo
    hopark      08/03/07 - add levels for metadata
    hopark      07/03/07 - fix bug
    hopark      06/25/07 - add desc
    hopark      06/11/07 - Creation
 */

/**
 *  @version $Header: LevelDesc.java 19-jun-2008.18:24:25 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging;

public class LevelDesc
  {
    int m_level;
    String m_desc;
    
    public LevelDesc(int level, String desc)
    {
      m_level = level;
      m_desc = desc;
    }
    public int getLevel() {return m_level;}
    public String getDesc() {return m_desc;}
}
