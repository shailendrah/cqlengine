/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPGenericSetOpNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

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

import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

public class CEPGenericSetOpNode extends CEPQueryRelationNode 
{
  /* The parser presentation of n-ary set operators looks as follows:
   * identifier CEPSetOpNode[]
   * i.e. it is an identifier followed by at least one or more
   * CEPSetOpNode(s).
   * 
   * Each CEPSetOpNode in turn has the operator type and identifier
   * name. So when expanded it looks as 
   *       identifier <setop> identifier <setop> identifier
   *       
   * SET operators are evaluated from left to right and all have the 
   * same precedence.
   */
  protected String    leftNode;
 
  protected List<CEPSetOpNode>   rightNodes;
  
  public CEPGenericSetOpNode(CEPStringTokenNode    leftNode, 
                             List<CEPSetOpNode> rightNodes)
    throws CEPException
  {
    this.leftNode   = leftNode.getValue();
    this.rightNodes  = rightNodes;
    setStartOffset(leftNode.getStartOffset());
    setEndOffset(rightNodes.get(rightNodes.size()-1).getEndOffset());
  }
  
  /**
   * Get the left relation
   * @return the left relation
   */
  public String getLeftRelation() 
  {
    return leftNode;
  }

  /**
   * Get the List of right relations
   * @return the right relations
   */
  public List<CEPSetOpNode> getRightRelations() 
  {
    return rightNodes;
  }
  
  public String toString() 
  {
    StringBuffer myString = new StringBuffer(50);  
    myString.append(" " + leftNode);
    
    for (CEPSetOpNode node: rightNodes) 
    {
      myString.append(" " + node.getRelSetOp());
      myString.append(" " + node.getRight());
    }
    return myString.toString();
  }
    
  @Override
  // FIXME: don't know how to write this. Need to ask someone who knows
  // Visualizer XML stuff.
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
