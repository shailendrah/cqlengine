/* $Header: IEvictPolicyCallback.java 27-mar-2008.11:29:38 hopark Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/08/08 - Creation
 */

/**
 *  @version $Header: IEvictPolicyCallback.java 27-mar-2008.11:29:38 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.memmgr;

import oracle.cep.memmgr.IEvictPolicy.Source;

public interface IEvictPolicyCallback
{
  enum SpillCmd
  {
    SET_NORMAL, SET_ASYNCSPILL, FORCE_EVICT, SET_SYNCSPILL
  }
  boolean evictionTriggered(Source src, IEvictPolicy policy, SpillCmd cmd, Object arg);
  void stop();
  int getEvictableCount();
}

