/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRegexpNode.java /main/7 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Base Parse tree node corresponding to a regular expression

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    04/02/09 - adding myString
    skmishra    02/05/09 - adding toString
    parujain    08/11/08 - error offset
    rkomurav    03/18/08 - change return type
    rkomurav    03/02/08 - add getAllreferencedCorrAttrs
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPRegexpNode.java /main/6 2009/04/03 18:57:07 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

import java.util.List;

/**
 * Base Parse tree node corresponding to a regular expression 
 *
 * @since 1.0
 */

public abstract class CEPRegexpNode implements CEPParseTreeNode {
	
  public int startOffset;
  
  public int endOffset;
  
  protected String myString;
  
  // collect all the referenced corrs and return if any duplicates exist
  public abstract boolean getAllReferencedCorrNames(List<String> corrs);
  
  /**
   * Sets startoffset corresponding to ddl
   */
  public void setStartOffset(int start)
  {
    this.startOffset = start;
  }
  
  /**
   * Gets the start offset
   */
  public int getStartOffset()
  {
    return this.startOffset;
  }
  
  /**
   * Sets the EndOffset corresponding to DDL
   */
  public void setEndOffset(int end)
  {
    this.endOffset = end;
  }
  
  /**
   * Gets the endoffset
   */
  public int getEndOffset()
  {
    return this.endOffset;
  }

  public void setMyString(String arg)
  {
    myString = arg;
  }
  
  public abstract String toString();
  
  public abstract String toVisualizerXml();
}
