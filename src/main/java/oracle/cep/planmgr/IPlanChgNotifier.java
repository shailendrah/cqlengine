/* $Header: IPlanChgNotifier.java 05-jun-2007.18:01:26 hopark   Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved. */

/*
   DESCRIPTION
    IPlanChgNotifier interface is to notify clients of plan change.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/05/07 - Creation
 */

/**
 *  @version $Header: IPlanChgNotifier.java 05-jun-2007.18:01:26 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr;

import oracle.cep.phyplan.PhyOpt;

public interface IPlanChgNotifier
{
  void addQueryRoot(int queryId, PhyOpt opt);
  void removeQueryRoot(int queryId, PhyOpt opt);
}
