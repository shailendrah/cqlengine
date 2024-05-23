/* $Header: CacheWriteLock.java 15-mar-2006.10:37:51 skaluska Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares CacheWriteLock in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    skaluska  03/10/06 - Creation
    skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: CacheWriteLock.java 15-mar-2006.10:37:51 skaluska Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.metadata.cache;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CacheWriteLock
 *
 * @author skaluska
 */
class CacheWriteLock extends CacheLock
{
  /** lock */
  ReentrantReadWriteLock.WriteLock lock;

  /**
   * Constructor for CacheWriteLock
   * @param obj Underlying cache object
   */
  CacheWriteLock(CacheObject obj)
  {
    super(CacheLockType.WRITE, obj);
    lock = obj.getLock().writeLock();
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
