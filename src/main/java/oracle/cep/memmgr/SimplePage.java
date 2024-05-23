/* $Header: SimplePage.java 16-apr-2008.15:41:31 hopark Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/16/08 - add stat
    hopark      04/16/08 - add stat
    hopark      03/22/08 - Creation
 */

/**
 *  @version $Header: SimplePage.java 16-apr-2008.15:41:31 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Level;

import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.memmgr.SimplePageManager.BaseEntry;

public final class SimplePage implements Externalizable
{
  BaseEntry[] entries;
  short   m_head;
  short   m_tail;
  
  public SimplePage()
  {
    m_head = 0;
    m_tail = 0;
  }
  
  public SimplePage(int cnt)
  {
    entries = new BaseEntry[cnt];
  }
  
  public final void free(int index)
  {
    entries[index] = null;
    m_head = (short)(index + 1);
  }
  
  public final void put(int index, BaseEntry e)
  {
    entries[index] = e;
    m_tail = (short)(index + 1);
  }

  public final int getUsedEntries()
  {
    return m_tail - m_head;
  }
  
  public final BaseEntry get(int index)
  {
    return entries[index];
  }

  public void writeExternal(ObjectOutput stream) throws IOException
  {
    int b = -1;
    int cnt = 0;
    for (int i = 0; i < entries.length; i++)
    {
      if (entries[i] != null)
      {
        if (b < 0) b = i;
        cnt++;
      }
    }
    if (b < 0) 
    {
      b = 0;
      assert (cnt == 0);
    }
    stream.writeShort(m_head);
    stream.writeShort(m_tail);
    stream.writeInt(entries.length);
    stream.writeInt(cnt);
    stream.writeInt(b);
    int pos = b;
    while (cnt > 0)
    {
      stream.writeObject(entries[pos]);
      cnt--;
      pos++;
    }
  }
  
  public void readExternal(ObjectInput stream) throws IOException
  {
    m_head = stream.readShort();
    m_tail = stream.readShort();
    int len = stream.readInt();
    entries = new BaseEntry[len];
    int cnt = stream.readInt();
    int pos = stream.readInt();
    while (cnt > 0)
    {
      try 
      {
        entries[pos] = (BaseEntry) stream.readObject();
      }
      catch(ClassNotFoundException e)
      {
        LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
        assert false;
      }
      cnt--;
      pos++;
    }
  }
}

