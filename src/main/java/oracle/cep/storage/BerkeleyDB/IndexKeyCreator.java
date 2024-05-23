/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/storage/BerkeleyDB/IndexKeyCreator.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2008, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/03/11 - refactor
    hopark      10/13/08 - Uses ThreadLocal due to bdb deadlock issue.
    hopark      09/12/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/storage/BerkeleyDB/IndexKeyCreator.java /main/2 2008/10/24 15:50:23 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.storage.BerkeleyDB;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;


class IndexKeyCreator implements SecondaryKeyCreator
{
  @SuppressWarnings("unchecked")
  EntryBinding  m_binding = null;
  
  private ThreadLocal<Object> m_indexKey = new ThreadLocal<Object>(){
    @Override protected Object initialValue() 
    {
      return null;
    }
  };
    
  @SuppressWarnings("unchecked")
public IndexKeyCreator(EntryBinding binding)
  {
    m_binding = binding;
  }

  public void setIndexKey(Object indexKey)
  {
    m_indexKey.set(indexKey);
  }
	  
  @SuppressWarnings("unchecked")
  public boolean createSecondaryKey(SecondaryDatabase sdb, 
		  	DatabaseEntry keyEntry, DatabaseEntry dataEntry, DatabaseEntry resultEntry)
  {
    Object ikey = m_indexKey.get();
    if (ikey == null)
    {
	// No index key is associated with the keyEntry..
	return false;
    }
    m_binding.objectToEntry(ikey, resultEntry);
    return true;
  }
}
  