/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSetOpNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node for a query with a N-ary Set Operations

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    vikshukl    03/07/11 - support n-ary set operators
    vikshukl    03/07/11 - Creation
 */

package oracle.cep.parser;

import oracle.cep.common.RelSetOp;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

public class CEPSetOpNode extends CEPQueryRelationNode {

  /** The set operator type */
  protected RelSetOp setopType;

  /** The right operand for this set operator */
  protected String right;
    
  /** Is it a Union All */
  protected boolean isUnionAll;

  /**
   * Constructor
   * @param setopType the set operator type
   * @param right the right operand
   * @param isUnionAll Is operator union all or not
   */
  public CEPSetOpNode(CEPStringTokenNode right,
               RelSetOp           setopType,
               boolean            isUnionAll) 
  { 
    this.setopType    = setopType;
    this.right        = right.getValue();
    this.isUnionAll = isUnionAll;
    setEndOffset(right.getEndOffset());
  }

  /**
   * Get the operator
   * @return the operator
   */
  public RelSetOp getRelSetOp() {
    return setopType;
  }
 

  /**
   * Get the right operand
   * @return the right operand
   */
  public String getRight() {
    return right;
  }
  
  /**
   * Get type of Union operation
   * @return true if operation is 'union all'
   */
  public boolean isUnionAll() {
    return isUnionAll;
  }

  public String toString()
  {
    if (isUnionAll)
      return " " + " UNION ALL " + right + " ";
    else
      return " " + setopType.toString() + " " + right + " ";
  }
  
  // FIXME: don't know how to write this. Need to ask someone who knows
  // Visualizer XML stuff
  public int toQCXML(StringBuffer queryXml, int operatorID)
      throws UnsupportedOperationException
  {
    return 1;
  }
  
  /**
   * Returns true if SetOp Query represented by this node contains
   * any logical CQL syntax.
   */
  @Override
  public boolean isLogical()
  {
    //TODO: In existing scope of implementation, There is no handling of
    // logical syntax in set operations.
    return false;
  }
}
