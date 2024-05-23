/* $Header: CacheLock.java 15-mar-2006.10:37:51 skaluska Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
 DESCRIPTION
 Declares CacheLock in package oracle.cep.metadata.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    skaluska  03/10/06 - Creation
    skaluska  03/10/06 - Creation
 */

/**
 *  @version $Header: CacheLock.java 15-mar-2006.10:37:51 skaluska Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.metadata.cache;

/**
 * CacheLock
 *
 * @author skaluska
 */
public abstract class CacheLock
{
  /** type of lock */
  private CacheLockType typ;
  
  /** underlying cache object */
  private CacheObject obj;
  
  /**
   * Constructor for CacheLock
   * @param typ Type of lock
   * @param obj Underlying cache object
   */
  CacheLock(CacheLockType typ, CacheObject obj)
  {
    this.typ = typ;
    this.obj = obj;
  }
  
  /**
   * Getter for obj in CacheLock
   * @return Returns the obj
   */
  public CacheObject getObj()
  {
    return obj;
  }


  /**
   * Setter for obj in CacheLock
   * @param obj The obj to set.
   */
  void setObj(CacheObject obj)
  {
    this.obj = obj;
  }


  /**
   * Getter for typ in CacheLock
   * @return Returns the typ
   */
  CacheLockType getTyp()
  {
    return typ;
  }


  /**
   * Setter for typ in CacheLock
   * @param typ The typ to set.
   */
  void setTyp(CacheLockType typ)
  {
    this.typ = typ;
  }


  /**
   * Release the lock
   */
  abstract void release();
}
