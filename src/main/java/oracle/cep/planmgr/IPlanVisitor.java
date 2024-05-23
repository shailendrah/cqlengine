/* $Header: IPlanVisitor.java 15-jun-2007.09:48:37 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    IPlanVisitor interface is to apply visitor design pattern for plan manager.
    The visitor design pattern is a way of separating an algorithm from an 
    object structure. It provies the ability to add new operations to exisiting
    object structures without modifying those structures.

    It has a visit() method for each element class.
    The accept() method of an element class calls back the visit()
    method for its class.
    Separate concrete visitor class can then be written that perform
    some particular operations.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      06/05/07 - Creation
 */

/**
 *  @version $Header: IPlanVisitor.java 15-jun-2007.09:48:37 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.planmgr;

import oracle.cep.phyplan.PhyIndex;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyQueue;
import oracle.cep.phyplan.PhyStore;
import oracle.cep.phyplan.PhySynopsis;

public interface IPlanVisitor 
{
  enum ObjType {INPUT_OPERATOR, SUB_SYNOPSIS, SUB_STORE, SUB_QUEUE, SUB_INDEX};
  boolean canVisit(ObjType which);
  void visit(PhyOpt opt);
  void visit(PhySynopsis syn);
  void visit(PhyStore store);
  void visit(PhyQueue queue);
  void visit(PhyIndex index);
}