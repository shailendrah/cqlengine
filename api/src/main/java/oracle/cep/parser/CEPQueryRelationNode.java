/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPQueryRelationNode.java /main/3 2012/08/09 00:10:57 sbishnoi Exp $ */

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
    sbishnoi    07/26/12 - adding evaluate expression
    vikshukl    06/20/11 - subquery support
    anasrini    02/21/06 - implement CEPQueryNode interface 
    anasrini    12/20/05 - parse tree node for a query whose return type is a 
                           relation 
    anasrini    12/20/05 - parse tree node for a query whose return type is a 
                           relation 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPQueryRelationNode.java /main/3 2012/08/09 00:10:57 sbishnoi Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node for a query whose root operator is a relation to 
 * relation operator
 */

public abstract class CEPQueryRelationNode extends CEPRelationNode 
  implements CEPQueryNode 
{
  /** Evaluate Expression */
  private CEPParseTreeNode evaluateClause;
  
  /**
   * @return the evaluateClause
   */
  public CEPParseTreeNode getEvaluateClause()
  {
    return evaluateClause;
  }
  /**
   * @param evaluateClause the evaluateClause to set
   */
  public void setEvaluateClause(CEPParseTreeNode evaluateClause)
  {
    this.evaluateClause = evaluateClause;
  }
  /**
   *  This relation is derived from a (sub)query.
   */
  public boolean isQueryRelationNode() 
  {
    return true;
  }
}
