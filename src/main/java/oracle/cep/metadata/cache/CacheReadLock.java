/* $Header: CacheReadLock.java 15-mar-2006.10:37:51 skaluska Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares CacheReadLock in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    skaluska  03/10/06 - Creation
    skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: CacheReadLock.java 15-mar-2006.10:37:51 skaluska Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata.cache;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CacheReadLock
 *
 * @author skaluska
 */
class CacheReadLock extends CacheLock
{
  ReentrantReadWriteLock.ReadLock lock;

  /**
   * Constructor for CacheReadLock
   * @param obj Cache object
   */
  public CacheReadLock(CacheObject obj)
  {
    super(CacheLockType.READ, obj);
    lock = obj.getLock().readLock();
    lock.lock();
  }

  /* (non-Javadoc)
   * @see oracle.cep.metadata.CacheLock#release()
   */
  @Override
  void release()
  {
    lock.unlock();
  }
}
