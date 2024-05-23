/* $Header: pcbpel/cep/server/src/oracle/cep/storage/BerkeleyDB/QueryContext.java /main/5 2008/10/24 15:50:24 hopark Exp $ */

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
 hopark      10/23/08 - add toString
 hopark      10/03/08 - add closeQuery
 hopark      09/23/08 - add getNextRecord
 hopark      09/15/08 - implement getNext
 mthatte     11/08/07 - adding type for JDBC
 mthatte     08/16/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/BerkeleyDB/QueryContext.java /main/5 2008/10/24 15:50:24 hopark Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage.BerkeleyDB;

import java.io.Serializable;

import oracle.cep.exceptions.StorageError;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.storage.IStorageContext;
import oracle.cep.storage.StorageException;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;


public class QueryContext implements IStorageContext, Serializable
{
  private static final long serialVersionUID = -2725295902801327338L;

  protected DBEntry  m_dbEntry;
  protected Cursor   m_cursor;
  protected String   m_nameSpace;
  protected String[] m_types;

  QueryContext(DBEntry entry)
  {
    m_dbEntry = entry;
    m_nameSpace = entry.getNamespace();
    m_types = null;
    m_cursor = initCursor(m_dbEntry);
  }
  
  QueryContext(DBEntry entry, String[] _types)
  {
    m_dbEntry = entry;
    m_nameSpace = entry.getNamespace();
    m_types = _types;
    m_cursor = initCursor(m_dbEntry);
  }

  public String toString()
  {
    return m_cursor.toString();
  }
  
  protected Cursor initCursor(DBEntry dbEntry)
    throws StorageException
  {
    Database db = dbEntry.getDb();
    String ns = dbEntry.getNamespace();
    assert (db != null) : "initCursor " + ns + " dbEntry:" + dbEntry.hashCode();
    try
    {
      return db.openCursor(null, null);
    } 
    catch (DatabaseException dbe)
    {
      LogUtil.warning(LoggerType.TRACE, dbe.toString());
      throw new StorageException(StorageError.OBJECT_NOT_READ, dbe, ns);
    }
  }
  
  public String getNameSpace() {return m_nameSpace;}
  public String[] getTypes() {return m_types;}
  
  public void setTypes(String[] _types)
  {
    m_types = _types;
  }
  
  private Object getNext(boolean key)
  {
    Object result = null;
    try
    {
      DatabaseEntry foundKey = new DatabaseEntry();
      DatabaseEntry foundData = new DatabaseEntry();
      if (m_cursor.getNext(foundKey, foundData, LockMode.READ_UNCOMMITTED) ==
          OperationStatus.SUCCESS)
      {
        if (key)
        {
          result = m_dbEntry.entryToKey(foundKey);
        }
        else
        {
          result = m_dbEntry.entryToObj(foundData);
        }
      } else
      {
        close();
      }
    } catch (DatabaseException de)
    {
      LogUtil.warning(LoggerType.TRACE, de.toString());
      throw new StorageException(StorageError.OBJECT_NOT_READ, de, m_nameSpace);
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
