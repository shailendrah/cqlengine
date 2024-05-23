/* $Header: pcbpel/cep/server/src/oracle/cep/memmgr/IEvictPolicy.java /main/7 2008/12/10 18:55:56 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    IEvictPolicy defines common behavior of the replacement policies.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/04/08 - pass cepMgr
    hopark      12/02/08 - move LogLevelManager to ExecContext
    hopark      10/10/08 - remove statics
    hopark      05/05/08 - is Full spill
    hopark      03/18/08 - add start
    hopark      03/08/08 - add callback
    hopark      10/15/07 - add getStat
    hopark      10/12/07 - add needEviction
    hopark      09/29/07 - add forceEvict
    najain      03/02/07 - 
    hopark      02/05/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/memmgr/IEvictPolicy.java /main/7 2008/12/10 18:55:56 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.memmgr;

import oracle.cep.execution.ExecException;
import oracle.cep.logging.ILoggable;
import oracle.cep.service.CEPManager;

public interface IEvictPolicy extends ILoggable
{
  public enum Source {Factory, Scheduler, Background};
  
  void init(CEPManager cepMgr);
  boolean isFullSpill();
  
  boolean needEviction(Source src);
  void runEvictor(Source src);
  void startEvictor();
  void stopEvictor();
  void forceEvict() throws ExecException;
  boolean isUsingCallback();
  void addCallback(IEvictPolicyCallback cb);
  void removeCallback(IEvictPolicyCallback cb);
  EvictStat getStat();
}
