/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/Predicate.java /main/5 2011/06/02 13:25:39 mjames Exp $ */

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
    sbishnoi    04/08/10 - adding toString
    sbishnoi    01/14/09 - adding list of attributes
    sbishnoi    01/02/09 - Creation
 */

package oracle.cep.extensibility.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import oracle.cep.extensibility.expr.BooleanExpression;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/Predicate.java /main/4 2010/06/24 06:26:52 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class Predicate {

  /** String represents WHERE clause of CQL Query*/
  private String predicateClause;
  
  /** boolean value to check whether it is an equality predicate*/
  private boolean isEqualityPredicate;
  
  /** A Map which will represent an equality predicate
   *  Each entry will be a base boolean expression with equality operators only
   *  i.e. =, != 
   */
  private Map<String, Object> attrNameValueMap;
  
  /** A list of DataSource's Comparison Attributes*/
  private List<String> attrNameList;

  /**
   * Where condition boolean expressions that must be ANDed together
   */
  private List<BooleanExpression> conditions;
  
  /**
   * Constructs a Predicate object which will represent WHERE clause
   */
  public Predicate()
  {
    predicateClause = null;
    attrNameValueMap = new HashMap<String, Object>();
  }
  
  /**
   * Return a String representation of WHERE clause
   * @return WHERE clause of cql query
   */
  public String getPredicateClause()
  {
    return predicateClause;
  }
  
  /**
   * Sets WHERE clause with given String value
   * @param paramPredicateClause Given Predicate in form of Java String
   */
  public void setPredicateClause(String paramPredicateClause)
  {
    predicateClause = paramPredicateClause;
  }

  /**
   * Returns the list of boolean expressions that must be ANDed together 
   * to form the where condition. The internal list is returned as is since
   * this is not external facing. The list must not be modified by the
   * caller.
   *
   * @return the list of boolean expressions that must be ANDed together 
   * to form the where condition
   */
  public List<BooleanExpression> getConditions()
  {
      return conditions;
  }

  /**
   * Use this method to set the boolean expressions that must be ANDed
   * together to form the where clause
   *
   * @param inConditions boolean expressions that must be ANDed
   * together to form the where clause
   */
  public void setConditions(List<BooleanExpression> inConditions)
  {
      conditions = (inConditions==null) ? null :
                      new ArrayList<BooleanExpression>(inConditions);
  }
  
  /**
   * Sets whether the Predicate Object is an equality predicate
   * @param paramIsEqualityPredicate
   */
  public void setIsEqualityPredicate(boolean paramIsEqualityPredicate)
  {
    isEqualityPredicate = paramIsEqualityPredicate;
  }
  
  /**
   * Get Whether Predicate is an equality predicate
   * @return true if it is an EqualityPredicate, false otherwise
   */
  public boolean isEqualityPredicate()
  {
    return isEqualityPredicate;
  }
  
  
  /**
   * Get Attribute's Name-Value Map 
   * @return
   */
  public Map<String, Object> getAttrNameValueMap()
  {
      return attrNameValueMap;
  }
  
  /**
   * Get Attribute's Name List
   * @return
   */
  public List<String> getAttrNameList()
  {
      return attrNameList;
  }
  

  /**
   * add an attribute to existing list of comparison attributes
   * @param attrName attributes name to be added
   * @param attrVal  attribute value to be added
   */
  public void addEqualityPredicate(String attrName)
  {
    attrNameList.add(attrName);
  }
  
  public void setAttrNameList(LinkedList<String> paramAttrNameList)
  {
    attrNameList = paramAttrNameList;
  }
  
  public String toString()
  {
    StringBuilder predicate = new StringBuilder();
    
    predicate.append("<Predicate>");
    predicate.append("<Clause>" + this.predicateClause + "</Clause>");
    predicate.append("<IsEquality>" + this.isEqualityPredicate + "</IsEquality>");
    predicate.append("<ComparisonAttributes>");
    for(int i =0; attrNameList != null && i < attrNameList.size(); i++)
      predicate.append(attrNameList.get(i));
    predicate.append("</ComparisonAttributes>");
    predicate.append("</Predicate>");
    
    return predicate.toString();
  }
}
