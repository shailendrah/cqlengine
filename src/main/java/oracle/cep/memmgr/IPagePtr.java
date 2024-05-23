/* $Header: IPagePtr.java 18-dec-2007.10:30:54 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/05/07 - cleanup spill
    hopark      10/30/07 - Creation
 */

package oracle.cep.memmgr;

import oracle.cep.execution.ExecException;

public interface IPagePtr
{
  int   getPageId();
  int   getPageManagerId();
  boolean isInFreeList();
  void setInFreeList(boolean b);
  boolean isEmptySlot();
  
  /**
   * eviction related methods
   */
  boolean isDirty();
  void setDirty(boolean b);
  boolean isStored();
  void setStored(boolean b);
  boolean isEvicted();
  IPage peek();

  IPage pin(PageManager pm, int mode) throws ExecException;

  boolean evict(PageManager pm) throws ExecException;
  int getRefCnt(int index);
  void setRefCnt(int index, short v);
  int addRef(int index);
  int release(int index);
}