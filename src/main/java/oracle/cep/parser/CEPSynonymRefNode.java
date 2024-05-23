/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSynonymRefNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    11/20/09 - drop synonym
    parujain    11/20/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSynonymRefNode.java /main/1 2010/01/06 20:33:12 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

public class CEPSynonymRefNode implements CEPParseTreeNode {
  private String synonym;

  protected int startOffset;

  protected int endOffset;

  /**
   * Constructor to drop the synonym
   * @param synonym
   *               Synonym name
   */
  public CEPSynonymRefNode(CEPStringTokenNode syn)
  {
    this.synonym = syn.getValue();
    startOffset = syn.getStartOffset();
    endOffset = syn.getEndOffset();
  }

  public String getSynonym()
  {
    return this.synonym;
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

}
