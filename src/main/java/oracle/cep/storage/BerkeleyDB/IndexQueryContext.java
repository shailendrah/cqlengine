/* $Header: pcbpel/cep/server/src/oracle/cep/storage/BerkeleyDB/IndexQueryContext.java /main/3 2008/10/24 15:50:24 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    IndexQueryContext maintains the cursor for the index.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/23/08 - add toString
    hopark      10/03/08 - add closeQuery
    hopark      09/23/08 - add getNextRecord
    hopark      09/15/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/BerkeleyDB/IndexQueryContext.java /main/3 2008/10/24 15:50:24 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage.BerkeleyDB;

import oracle.cep.exceptions.StorageError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.storage.StorageException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;

public class IndexQueryContext extends QueryContext
{
  private static final long serialVersionUID = -6150152023460271559L;
  
  DatabaseEntry m_key;
  boolean       m_first = true;
  
  IndexQueryContext(DBEntry entry, Object key)
  {
    super(entry);
    setKey(entry, key);
  }
  
  IndexQueryContext(DBEntry entry, Object key, String[] _types)
  {
    super(entry, _types);
    setKey(entry, key);
  }
	
  protected Cursor initCursor(DBEntry dbEntry)
    throws StorageException
  {
    SecondaryDatabase db = dbEntry.getIndexDb();
    String ns = dbEntry.getNamespace();
    assert (db != null) : "initCursor " + ns + " dbEntry:" + dbEntry.hashCode();
          
    try
    {
      return db.openSecondaryCursor(null, null);
    } 
    catch (DatabaseException dbe)
    {
      LogUtil.warning(LoggerType.TRACE, dbe.toString());
      throw new StorageException(StorageError.OBJECT_NOT_READ, dbe, ns);
    }
  }
    
  private void setKey(DBEntry entry, Object key)
  {
    m_key = entry.indexKeyToEntry(key);
  }
  
  private Object getNext(boolean key)
  {
    Object result = null;
    try
    {
      DatabaseEntry foundKey = new DatabaseEntry();
      DatabaseEntry foundData = new DatabaseEntry();
      SecondaryCursor cursor = (SecondaryCursor) m_cursor;
      OperationStatus retVal;
      if (m_first)
      {
        retVal = cursor.getSearchKey(m_key, foundKey, foundData, LockMode.DEFAULT);
        m_first = false;
      } 
      else {
        retVal = cursor.getNextDup(m_key, foundKey, foundData,  LockMode.DEFAULT);
      }
      if (retVal == OperationStatus.SUCCESS)
      {
        if (key)
        {
          result = m_dbEntry.entryToKey(foundKey);
        }
        else
        {
          result = m_dbEntry.entryToObj(foundData);
        }
      } 
      else
      {
        close();
      }
    } catch (DatabaseException dbe)
    {
      LogUtil.warning(LoggerType.TRACE, dbe.toString());
      throw new StorageException(StorageError.OBJECT_NOT_READ, dbe, m_nameSpace);
    }
    return result;
  }

  
  public Object getNextKey()
  {
    return getNext(true);
  }
    
  public Object getNextRecord()
  {
    return getNext(false);
  }

  
  public void close()
  {
    try
    {
      if (m_cursor != null)
      {
        m_cursor.close();
        m_cursor = null;
      }
    } catch (DatabaseException de)
    {
      LogUtil.warning(LoggerType.TRACE, de.toString());
      throw new StorageException(StorageError.OBJECT_NOT_READ, de, m_nameSpace);
    }  
  }  
}
