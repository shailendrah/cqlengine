/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPOuterJoinRelationNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    12/21/09 - qcxml support
    parujain    05/15/09 - ansi outer join
    parujain    05/15/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPOuterJoinRelationNode.java /main/2 2009/12/29 20:26:09 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

public class CEPOuterJoinRelationNode extends CEPRelationNode 
{

  protected CEPRelationNode    leftNode;
  
  protected List<CEPRightOuterJoinNode>   rightNodes;
  
  public CEPOuterJoinRelationNode(CEPRelationNode    leftNode, 
                                  List<CEPRightOuterJoinNode> rightNodes)
    throws CEPException
  {
    this.leftNode   = leftNode;
    this.rightNodes  = rightNodes;
    setStartOffset(leftNode.getStartOffset());
    setEndOffset(rightNodes.get(rightNodes.size()-1).getEndOffset());
  }
  

  /**
   * Get the left relation
   * @return the left relation
   */
  public CEPRelationNode getLeftRelation() 
  {
    return leftNode;
  }

  /**
   * Get the List of right relations
   * @return the right relations
   */
  public List<CEPRightOuterJoinNode> getRightRelation() 
  {
    return rightNodes;
  }
  
  public String toString() 
  {
    StringBuffer myString = new StringBuffer(50);  
	
    if(leftNode instanceof CEPOuterJoinRelationNode)
      myString.append(" (" + leftNode.toString() + ") ");
    else
      myString.append(" " + leftNode.toString() + " ");
    
    for(CEPRightOuterJoinNode node: rightNodes) 
    {
      myString.append(" " + node.getOuterJoinType().getOuterJoinType() +" ");
      myString.append(node.getRightNode().toString());
      myString.append(" ON " + node.condition.toString());
    }
    
    return myString.toString();
  }
  
  
  public String getCQLProperty(CEPRightOuterJoinNode node) 
  {
    StringBuffer myString = new StringBuffer(40);
    myString.append(" " + node.getOuterJoinType().getOuterJoinType() +" ");
    myString.append(node.getRightNode().toString());
    myString.append(" ON " + node.condition.toString());
    return myString.toString();
  }
  
  @Override
  public int toQCXML(StringBuffer queryXml, int operatorID)
		throws UnsupportedOperationException {
    
    StringBuilder myXmlString = new StringBuilder(50);
    StringBuffer cql = new StringBuffer(30);
    cql.append("SELECT * FROM " + leftNode.toString());
    int rootId = leftNode.toQCXML(queryXml, operatorID);
    int leftRootId = rootId;
    int rightRootId = 0;
    for(CEPRightOuterJoinNode node: rightNodes)
    {
      myXmlString.delete(0, myXmlString.length());
      rootId = node.getRightNode().toQCXML(queryXml, rootId);
      rightRootId = rootId;
      cql.append(getCQLProperty(node));
      myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.outerJoinTypeTag, node.getOuterJoinType().getOuterJoinType(), null, null));
      myXmlString.append("\n\t" + XMLHelper.buildElement(true, VisXMLHelper.onClauseTag, node.condition.toString(), null, null));
      myXmlString.append("\n\t" + VisXMLHelper.createCqlPropertyXml(cql.toString()));
      //myXmlString.append("\n\t" + VisXMLHelper.createInputsXml(new int[]{rootId-1, rootId-2}));
      myXmlString.append("\n\t" + VisXMLHelper.createInputsXml(new int[]{leftRootId-1, rightRootId-1}));
      
      queryXml.append(XMLHelper.buildElement(true, VisXMLHelper.operatorTag, myXmlString.toString().trim(), new String[]{VisXMLHelper.operatorIdAttr, VisXMLHelper.operatorTypeAttr}, new String[]{String.valueOf(rootId), VisXMLHelper.outerJoinOperator}));
      ++rootId;
     leftRootId = rootId; 
    }
    
    return rootId;

   //throw new UnsupportedOperationException("ANSI Outer Joins Not supported ");
 }

 @Override
 public boolean isOuterJoinRelationNode()
 {
   return true;
 }

}
