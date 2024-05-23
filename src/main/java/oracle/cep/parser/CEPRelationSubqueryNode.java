/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRelationSubqueryNode.java /main/1 2011/07/14 11:10:28 vikshukl Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    07/11/11 - subquery support - relation is derived from a
                           subquery
    vikshukl    07/11/11 - Creation
 */

/**
 *  @version $Header: CEPRelationSubqueryNode.java 11-jul-2011.10:07:13 vikshukl Exp $
 *  @author  vikshukl
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import oracle.cep.util.VisXMLHelper;
import oracle.cep.util.XMLHelper;

public class CEPRelationSubqueryNode extends CEPRelationNode

{
  private CEPQueryNode query;
  
  /**
   * Constructor with the alias
   * @param query
   * @param aliasToken
   */
  public CEPRelationSubqueryNode(CEPQueryNode query,
                                 CEPStringTokenNode aliasToken)
  {
    this.query = query;
    setName(aliasToken.getValue());
    setAlias(aliasToken.getValue());
    setStartOffset(query.getStartOffset());
    setEndOffset(aliasToken.getEndOffset());  
    setCqlProperty("SELECT * FROM (" + query.toString() + ") AS " +
                   aliasToken.getValue());
  }
  
  /**
   * Constructor without the alias
   * @param query
   */
  public CEPRelationSubqueryNode(CEPQueryNode query)
  {
    this.query = query;
    setStartOffset(query.getStartOffset());
    setEndOffset(query.getEndOffset());
    setCqlProperty("SELECT * FROM (" + query.toString() + ")");
  }

  @Override
  public String toString() {
    if (alias != null) 
      return query.toString() + " AS " + alias;
    else 
      return query.toString();
  }

  @Override
  public boolean isQueryRelationNode() {
    return true;
  }

  /**
   * Returns the query from which this relation is derived.  
   * @return
   */
  public CEPQueryNode getQuery() 
  {
    return query;
  }
  
  @Override
  public int toQCXML(StringBuffer queryXml, int operatorID)
  throws UnsupportedOperationException 
  {
    int id = operatorID;
    StringBuilder myXmlString = new StringBuilder(30);
    
    myXmlString.append("\n\t" + 
                      XMLHelper.buildElement(true, VisXMLHelper.cqlPropertyTag, 
                                             cqlProperty, null, null));
    if(alias != null)
      myXmlString.append("\n\t" + 
                    XMLHelper.buildElement(true, VisXMLHelper.aliasTag, alias, 
                    null, null));
    
    myXmlString.append("\n\t" + 
        XMLHelper.buildElement(true, VisXMLHelper.sourceNameTag, this.name, 
                               null, null));
    
    queryXml.append(XMLHelper.buildElement(true, 
        VisXMLHelper.operatorTag, myXmlString.toString().trim(), 
        new String[]{VisXMLHelper.operatorIdAttr, 
                     VisXMLHelper.operatorTypeAttr}, 
                     new String[]{String.valueOf(operatorID), 
                                  VisXMLHelper.sourceOperator}));
    ++id;
    return id;
  }  
}
