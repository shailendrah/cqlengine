/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPatternNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node representing the pattern string

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    02/02/09 - adding toVisXML
    parujain    08/11/08 - error offset
    anasrini    01/05/07 - Creation
    anasrini    01/05/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPPatternNode.java /main/3 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to the pattern string
 *
 * @since 1.0
 */

public class CEPPatternNode implements CEPParseTreeNode {

  /** The regular expression corresponding to the pattern **/
  protected CEPRegexpNode pattern;

  protected int startOffset = 0;
  
  protected int endOffset = 0;
  
  /**
   * Constructor
   * @pattern the regular expression corresponding to the pattern
   */
  public CEPPatternNode(CEPRegexpNode pattern) {
    this.pattern = pattern;
    setStartOffset(pattern.getStartOffset());
    setEndOffset(pattern.getEndOffset());
  }

  // getter methods

  /**
   * Get the pattern 
   * @return the regular expression corresponding to the pattern
   */
  public CEPRegexpNode getPattern() {
    return pattern;
  }
  
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

  public String toString()
  {
    StringBuilder myString = new StringBuilder(25);
    myString.append(" PATTERN(");
    myString.append(this.pattern.toString());
    myString.append(")");
    return myString.toString();
  }
  /**
   * TODO: verify if complete?
   */
  public String toVisualizerXML()
  {
    StringBuilder xml = new StringBuilder(50);
    xml.append("\t<pattern-list>\n");
      xml.append(pattern.toVisualizerXml());
    xml.append("\t</pattern-list>\n");
    return xml.toString();
  }
}

