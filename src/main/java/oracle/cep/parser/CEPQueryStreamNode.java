/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPQueryStreamNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
   Parse tree node for a query whose root operator is a relation to
   stream operator

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    mthatte     10/19/09 - adding difference-using to QCXml
    vikshukl    08/20/09 - ISTREAM (R) DIFFERENCE USING (...)
    mthatte     01/26/09 - adding getQCXML
    parujain    08/15/08 - error offset
    anasrini    02/21/06 - implement CEPQueryNode interface 
    anasrini    12/20/05 - parse tree node for a query whose return type is a 
                           stream 
    anasrini    12/20/05 - parse tree node for a query whose return type is a 
                           stream 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPQueryStreamNode.java /main/4 2009/12/24 20:10:21 vikshukl Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import oracle.cep.common.RelToStrOp;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.exceptions.CEPException;
import oracle.cep.semantic.SemanticException;
import java.util.List;

/**
 * Parse tree node for a query whose root operator is a relation to 
 * stream operator
 *
 * @since 1.0
 */

public class CEPQueryStreamNode extends CEPStreamNode implements CEPQueryNode {

  /** The relation to stream operator type */
  protected RelToStrOp streamOpType;
  
  /** The relation on which the relation to stream operator is applied */
  protected CEPRelationNode relation;

  /** The list of SELECT expressions in the USING clause on which ISTREAM or
   * DSTREAM operator is to be applied. Always null for RSTREAM.
   */
  protected CEPExprNode[] usingClause;
  
  /**
   * Constructor
   * @param streamOpType the relation to stream operator type
   * @param relation the relation on which the relation to stream operator is
   * to be applied
   * @param usingClause specifies list of SELECT expressions to use when 
   *                    computing ISTREAM operator.
   */
  public CEPQueryStreamNode(CEPRelationNode relation, 
                     RelToStrOp streamOpType,
                     List<CEPExprNode> usingClause) 
      throws CEPException 
  {
    this.relation     = relation;
    this.streamOpType = streamOpType;

    setStartOffset(relation.getStartOffset());
    setEndOffset(relation.getEndOffset());

    /* valid only for ISTREAM and DSTREAM */
    if (usingClause != null) {
      /* USING list can't be empty here, that error caught in cql.yy itself */

      this.usingClause = (CEPExprNode[]) 
          (usingClause.toArray(new CEPExprNode[usingClause.size()]));

      setEndOffset(usingClause.get(usingClause.size()-1).getEndOffset());
    }
    else /* ISTREAM/DSTREAM without using clause */
      this.usingClause = null;
  }

  /**
   * Constructor
   * @param streamOpType the relation to stream operator type
   * @param relation the relation on which the relation to stream operator is
   * to be applied
   */
  public CEPQueryStreamNode(CEPRelationNode relation, 
                     RelToStrOp streamOpType)
      throws CEPException 
  {
    this.relation     = relation;
    this.streamOpType = streamOpType; /* called only for RSTREAM, for rest
                                       * caught at parser level itself */
    this.usingClause  = null;         /* never used */

    setStartOffset(relation.getStartOffset());
    setEndOffset(relation.getEndOffset());
  }


  // getter methods
  /**
   * Get the relation to stream operator
   * @return the relation to stream operator
   */
  public RelToStrOp getRelToStrOp() {
    return streamOpType;
  }

  /**
   * Get the relation query on which the relation to stream operator
   * is applied
   * @return the relation query on which the relation to stream operator
   *         is applied
   */
  public CEPRelationNode getRelationNode() {
    return relation;
  }
  
  public CEPExprNode[] getUsingClause() {
    return usingClause;
  }
  
  public String toString()
  {
    StringBuilder myString = new StringBuilder();
    myString.append(" ");
    myString.append(this.streamOpType.toString() + "(");
    myString.append(relation.toString());
    myString.append(")");
    if (usingClause != null) {
      myString.append(usingClause.toString());
    }
    return myString.toString();
  }
  
  private String getUsingClauseString() {
	if(usingClause == null)
		return "";
	  StringBuffer myString = new StringBuffer(24);
	  for(CEPExprNode e: usingClause)
		  myString.append(e.toString() + ",");
	  int lastComma = myString.length() - 1;
	  myString.deleteCharAt(lastComma);
	  return myString.toString();
  }
  @Override
  public int toQCXML(StringBuffer queryXml, int operatorID)
      throws UnsupportedOperationException
  {
    int rootID;
    
    StringBuilder myXml = new StringBuilder(50);
    String cql = streamOpType.toString() + "(" + relation.toString() +")";
    
    if (usingClause != null) 
    {
      cql += getUsingClauseString();
      myXml.append("<difference-using>" + getUsingClauseString() + "</difference-using>");
    }

    rootID = relation.toQCXML(queryXml, operatorID);
    
    String cqlPropertyXml = VisXMLHelper.createCqlPropertyXml(cql);
    String inputsXml = VisXMLHelper.createInputsXml(new int[]{rootID-1});
    
    myXml.append(cqlPropertyXml);
    myXml.append(inputsXml);
    
    String myOperator = VisXMLHelper.createOperatorTag(streamOpType.getOperatorType(), rootID, myXml.toString());
    queryXml.append("\n" + myOperator  + "\n");
    return rootID + 1; 
  }

  /**
   * Returns true if the inner query for this DDL contains logical CQL syntax.
   */
  @Override
  public boolean isLogical()
  {
    if(relation instanceof CEPQueryNode)
      return ((CEPQueryNode)relation).isLogical();
    else 
      return false;
  }
}
