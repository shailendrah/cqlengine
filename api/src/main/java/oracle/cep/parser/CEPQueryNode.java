/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/parser/CEPQueryNode.java /main/3 2011/07/14 11:10:28 vikshukl Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Marker interface for a parse tree node corresponding to a query

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    06/26/11 - subquery support
    mthatte     02/04/09 - adding toQCXML
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
    anasrini    02/21/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/parser/CEPQueryNode.java /main/2 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

/**
 * Marker interface for a parse tree node corresponding to a query
 *
 * @since 1.0
 */

public interface CEPQueryNode extends CEPParseTreeNode {
  /** Return true if cql query contains one or more than one logical CQL clauses*/
  public boolean isLogical();
  
  public int toQCXML(StringBuffer qXml, int operatorID) 
    throws UnsupportedOperationException;
}
