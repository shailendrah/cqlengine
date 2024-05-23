/* $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/EvalContextInfo.java /main/4 2009/01/21 20:42:03 sbishnoi Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Class used for holding context information during code generation

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    01/21/09 - adding isEqualityPredicate
    hopark      10/10/08 - remove statics
    parujain    05/14/08 - xml-publishing func
    najain      07/07/06 - 
    anasrini    03/14/06 - Creation
    anasrini    03/14/06 - Creation
    anasrini    03/14/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/planmgr/codegen/EvalContextInfo.java /main/4 2009/01/21 20:42:03 sbishnoi Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.internals.TupleSpec;
import oracle.cep.memmgr.FactoryManager;
import oracle.xml.parser.v2.XMLDocument;

/**
 * This class contains context that is built up during code generation.
 * For example, scratch requirements and constants processing to be
 * determined during code generation needs context and this is based of
 * this structure
 *
 * @since 1.0
 */

class EvalContextInfo {

  /** For determining scratch requirements */
  TupleSpec st;

  /** For setting up constants that will be involved during expression
   *  evaluation at run time 
   */
  ConstTupleSpec ct;
  
  /** This is required for xml publishing functions so that one tree should have just one instance 
   * xml document 
   */
  XMLDocument dummydoc;
  
  /**
   * flag to check whether the predicate in external relation join is equality
   * predicate
   * @param factoryMgr
   */
  boolean isEqualityPredicate;

  EvalContextInfo(FactoryManager factoryMgr) {
    st = new TupleSpec(factoryMgr.getNextId());
    ct = new ConstTupleSpec(factoryMgr);
    dummydoc = null;
    isEqualityPredicate = true;
  }
  
  public void createDocInstance()
  {
    dummydoc = new XMLDocument();
  }
  
  public XMLDocument getDummyDoc()
  {
    return dummydoc;
  }
  
  public void setIsEqualityPredicate(boolean paramIsEqualityPredicate)
  {
    isEqualityPredicate = paramIsEqualityPredicate;
  }
  
  public boolean getIsEqualityPredicate()
  {
    return isEqualityPredicate;
  }

}
