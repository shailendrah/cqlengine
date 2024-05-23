/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSFWQueryNode.java /main/14 2012/05/02 03:06:00 pkali Exp $ */

/* Copyright (c) 2005, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    11/07/11 - group by expr
    hopark      04/21/11 - make public to be reused in cqservice
    hopark      05/21/09 - fix assertion
    sborah      03/18/09 - resolving compilation error
    skmishra    03/13/09 - refactor
    skmishra    02/19/09 - aggregatexml bug
    sbishnoi    02/09/09 - support of ordered window
    skmishra    01/26/09 - adding toQCXML()
    parujain    08/15/08 - error offset
    parujain    06/22/07 - order by support
    sbishnoi    06/07/07 - fix xlint warning
    sbishnoi    04/24/07 - support for having
    anasrini    02/21/06 - add getter methods 
    anasrini    12/20/05 - A select-from-where query node, a query without set 
                           operations 
    anasrini    12/20/05 - A select-from-where query node, a query without set 
                           operations 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSFWQueryNode.java /main/14 2012/05/02 03:06:00 pkali Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.List;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.ServerError;
import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

/**
 * Parse tree node for a select-from-where query, a query without set
 * operations
 */

public class CEPSFWQueryNode extends CEPQueryRelationNode {

  /** The select clause */
  protected CEPSelectListNode selectClause;

  /** The from clause */
  protected CEPRelationNode[] fromClause;

  /** The where clause (optional) */
  protected CEPBooleanExprNode whereClause;
  
  /** The group by clause (optional) */
  protected CEPExprNode[] groupByClause;
  
  /** The having clause (optional) */
  protected CEPBooleanExprNode havingClause;
  
  /** Order by clause (optional) */
  protected CEPOrderByNode orderByClause;

  private String cqlProperty;
  
  /**
   * Constructor
   * @param selectClause  the select Clause
   * @param fromClause    the from Clause
   * @param whereClause   the optional where clause. Null if no where clause
   * @param groupByClause the optional group by clause. Null if no
   *                      group by clause
   * @param havingClause  the optional having clause. Null if no having clause
   * @param orderByClause the optional order by clause. Null if no order by 
   *                      clause
   */
  public CEPSFWQueryNode(CEPSelectListNode selectClause, 
                         List<CEPRelationNode> fromClause, 
                         CEPBooleanExprNode whereClause,
                         List<CEPExprNode>     groupByClause,
                         CEPBooleanExprNode havingClause,
                         CEPOrderByNode orderByClause) 
  {
    this.selectClause  = selectClause;
    cqlProperty = selectClause.toString();
    int fromLength = fromClause.size();
    this.fromClause    = 
      (CEPRelationNode[])(fromClause.toArray(new CEPRelationNode[fromLength]));
    cqlProperty = cqlProperty.concat(fromClause.toString());
    
    this.whereClause   = whereClause;
    
    if(whereClause != null) 
    {
      setEndOffset(whereClause.getEndOffset());
      cqlProperty = cqlProperty.concat(whereClause.toString());
    }
    else
      setEndOffset(fromClause.get(fromLength-1).getEndOffset());

    if (groupByClause != null) {
      int groupByLength = groupByClause.size();
      
      this.groupByClause = 
        (CEPExprNode[])(groupByClause.toArray(new CEPExprNode[groupByLength]));
      
      setEndOffset(groupByClause.get(groupByLength-1).getEndOffset());
    }
    this.havingClause = havingClause;
    
    if(orderByClause != null)
    {
      this.orderByClause = orderByClause;
      setEndOffset(orderByClause.getEndOffset());
    } 
     
    setStartOffset(selectClause.getStartOffset());
  }

  // getter methods

  /**
   * Get the from clause relations
   * @return the from clause relations
   */
  public CEPRelationNode[] getFromClauseRelations() {
    return fromClause;
  }

  /**
   * Get the where clause
   * @return the where clause
   */
  public CEPBooleanExprNode getWhereClause() {
    return whereClause;
  }

  /**
   * Get the group by clause
   * @return the group by clause
   */
  public CEPExprNode[] getGroupByClause() {
    return groupByClause;
  }

  /**
   * Get the select list
   * @return the select list
   */
  public CEPSelectListNode getSelectClause() {
    return selectClause;
  }
  
  /**
   * Get the having clause
   * @return the having clause
   */
  public CEPBooleanExprNode getHavingClause() {
    return havingClause;
  }
  
  /**
   * Get the order by clause
   * @return the order by clause
   */
  public CEPOrderByNode getOrderByClause() {
    return this.orderByClause;
  }
  
  //cql-property for joins
  private String createJoinCql()
  {
    assert fromClause.length >= 1;
    
    StringBuilder cqlProperty = new StringBuilder(25);
    cqlProperty.append("select * from ");
    for(CEPRelationNode source : fromClause)
      cqlProperty.append(source.toString() + ",");
    cqlProperty.deleteCharAt(cqlProperty.length() - 1);
    if(whereClause != null)
    {
      cqlProperty.append(" WHERE ");
      cqlProperty.append(XMLHelper.toHTMLString(whereClause.toString()));
    }
    return cqlProperty.toString();
  }
  
  private int createJoinXML(StringBuffer qXml,int[] inputIDList, int operatorID)
  {
    int myID = operatorID;
    int retID = myID + 1;

    String cqlProperty = VisXMLHelper.createCqlPropertyXml(createJoinCql());
    String inputsXml = VisXMLHelper.createInputsXml(inputIDList);
    StringBuilder xml = new StringBuilder(50);
    xml.append(inputsXml);
    xml.append(cqlProperty);
    
    if(whereClause != null)
    {
      String predicateXml = "\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.predicateTag, XMLHelper.toHTMLString(whereClause.toString()), null,null); 
      String predicates = "\n\t" + XMLHelper.buildElement(true, VisXMLHelper.predicatesTag, predicateXml, null, null);
      xml.append(predicates);
    }
    String joinOperator = VisXMLHelper.createOperatorTag(VisXMLHelper.joinOperator, myID, xml.toString().trim());
    qXml.append(joinOperator);

    return retID;
  }
  
  //cql-property for filter operator
  private String createFilterCql()
  {
    assert fromClause.length == 1;
    return createJoinCql();
  }
  
  //TODO: Incomplete. Walk the boolean expression tree to separate out predicates.
  private int createFilterXML(StringBuffer qXml, int operatorID)
  {
    assert fromClause.length == 1;
    int myID = operatorID;
    int retID = myID + 1;
    StringBuilder myXml = new StringBuilder(30);
    String inputsXml = VisXMLHelper.createInputsXml(new int[]{operatorID-1});
    String cqlPropertyXml = VisXMLHelper.createCqlPropertyXml(createFilterCql());
    
    myXml.append(inputsXml);
    myXml.append(cqlPropertyXml);
    
    if(whereClause != null)
    {
      String predicate = "\n\t\t" + XMLHelper.buildElement(true, VisXMLHelper.predicateTag, whereClause.toString(), null, null);
      String predicates = "\n\t" + XMLHelper.buildElement(true, VisXMLHelper.predicatesTag, predicate, null, null);
      myXml.append(predicates);
    }
    String filterOperator = VisXMLHelper.createOperatorTag(VisXMLHelper.filterOperator, myID, myXml.toString().trim());
    qXml.append(filterOperator);
    return retID;
  }
  
  //cql-property for aggr operator
  private String createCQL()
  {
    StringBuilder aggrCql = new StringBuilder();
    aggrCql.append(" select ");
    aggrCql.append(selectClause.toString());
    aggrCql.append(" FROM ");
    for(CEPRelationNode source:fromClause)
      aggrCql.append(source.toString() + ",");
    aggrCql.deleteCharAt(aggrCql.length() - 1);
    if(whereClause != null) 
    {
      aggrCql.append(" WHERE ");
      aggrCql.append(XMLHelper.toHTMLString(whereClause.toString()));
    }
    
    if(groupByClause != null)
    {
      aggrCql.append(" GROUP BY ");
      for(CEPExprNode a : groupByClause)
        aggrCql.append(a.toString() + ",");
      aggrCql.deleteCharAt(aggrCql.length() - 1);
    }
    
    if(havingClause != null)
    {
      aggrCql.append(" HAVING ");
      aggrCql.append(XMLHelper.toHTMLString(havingClause.toString()));
    }
    
    if(orderByClause != null)
    {
      aggrCql.append(" ORDER BY ");
      aggrCql.append(orderByClause.toString());
    }
    return aggrCql.toString();
  }
  
  public String toString()
  {
    return createCQL();
  }
  
  private int createSelectXML(StringBuffer qXml, int operatorID) throws CEPException
  {
    int aggrID = operatorID;
    StringBuilder aggrXml = new StringBuilder(100);
    String inputs = VisXMLHelper.createInputsXml(new int[]{aggrID -1});
    String cqlPropertyXml = VisXMLHelper.createCqlPropertyXml(createCQL());
    StringBuilder tempBuilder;
    aggrXml.append("\n\t" + inputs);
    aggrXml.append("\n\t" + cqlPropertyXml);
    aggrXml.append("\n\t" + selectClause.getSelectListXml());

    if (groupByClause != null)
    {
      tempBuilder = new StringBuilder(20);
      for (CEPExprNode a : groupByClause)
        tempBuilder.append("\n\t\t"
            + XMLHelper.buildElement(true, VisXMLHelper.groupByTag, a
                .toString(), null, null));
      aggrXml.append("\n\t"
          + XMLHelper.buildElement(true, VisXMLHelper.groupListTag, tempBuilder
              .toString().trim(), null, null));
    }

    if (havingClause != null)
    {
      aggrXml.append("\n\t"
          + XMLHelper.buildElement(true, VisXMLHelper.havingClauseTag,
              XMLHelper.toHTMLString(havingClause.toString()), null, null));
    }

    if (orderByClause != null)
    {
      if(orderByClause.getOrderByTopExpr() != null)
        throw new CEPException(ServerError.UNSUPPORTED_QCXML_OPERATION);
      
      tempBuilder = new StringBuilder(40);

      for (CEPOrderByExprNode oe : orderByClause.getOrderByClause())
      {
        tempBuilder.append(oe.toVisualizerXML());
      }
      //Note: include support for ordered windows also
      
      aggrXml.append("\n\t"
          + XMLHelper.buildElement(true, VisXMLHelper.orderByListTag,
              tempBuilder.toString().trim(), null, null));
    }

    qXml.append(VisXMLHelper.createOperatorTag(VisXMLHelper.selectOperator,
        aggrID, aggrXml.toString().trim()));
    return aggrID + 1;
  }
  
  /**
   * Returns an xml representation of this node
   * @param queryXml The buffer containing qXml so far
   */
  public int toQCXML(StringBuffer queryXml, int operatorID) throws UnsupportedOperationException
  {
    int rootID = operatorID;    
    int[] joinInputList = new int[fromClause.length];
    int countJoinInps = 0;
    
    //create stream/window operators
    //recursively assigns a root id. rootid will eventually be assigned to the
    //output operator.
    for(CEPRelationNode source: fromClause)
    {
      rootID = source.toQCXML(queryXml, rootID);
      joinInputList[countJoinInps] = rootID -1; //rootID - 1 is the input to the join
      ++countJoinInps;
    }
    
    //create join operator if multiple tables
    if(fromClause.length > 1)
    {
      rootID = createJoinXML(queryXml,joinInputList, rootID);
    }
    
    //else, if where clause exists, create filter operator
    else if(whereClause != null)
    {
      rootID = createFilterXML(queryXml,rootID);
    }
    
    
    //create project, aggregate operators
    if(!selectClause.isStar() || selectClause.isDistinct())
    {
      try
      {
       rootID = createSelectXML(queryXml,rootID);
      }
      catch(Exception e)
      {
        throw new UnsupportedOperationException(e.getMessage());
      }
    }

    return rootID;
  }

  /**
   * Returns true if SFW Query represented by this node contains
   * any logical CQL syntax.
   */
  @Override
  public boolean isLogical()
  {
    //TODO: As of now, I am adding logical syntax in SELECT list
    // so only checking selectClause
    return selectClause.isLogical();
  }
}
