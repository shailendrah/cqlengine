/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSynonymDefnNode.java /main/2 2011/05/19 15:28:46 hopark Exp $ */

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
    parujain    11/20/09 - create synonym
    parujain    11/20/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPSynonymDefnNode.java /main/1 2010/01/06 20:33:12 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.parser;

import java.util.List;

import oracle.cep.metadata.SynonymType;

public class CEPSynonymDefnNode implements CEPParseTreeNode {

  String synonym;

  String actualName;

  SynonymType synonymType;

  protected int startOffset;

  protected int endOffset;

  /**
   * Constructor of the synonym definition
   * @param syn 
   *           Synonym name
   * @param actual
   *              Actual name to be replaced by synonym
   */
  public CEPSynonymDefnNode(CEPStringTokenNode syn, List<CEPStringTokenNode> actual,
                     SynonymType type)
  {
    this.synonym = syn.getValue();
    this.actualName = getStringValue(actual);
    this.synonymType = type;
    startOffset = syn.getStartOffset();
    endOffset = actual.get(actual.size()-1).getEndOffset();
  } 
  
  private String getStringValue(List<CEPStringTokenNode> actual)
  {
    StringBuffer buffer = new StringBuffer();
    boolean firstIdentifier = true;
    
    for (CEPStringTokenNode stringNode : actual)
    {
      if (!firstIdentifier) 
      {
        if (stringNode.isLink())
          buffer.append("@");
        else
          buffer.append(".");
      } 
      else
        firstIdentifier = false;
      
      buffer.append(stringNode.getValue());
    }
    return buffer.toString();
  }

  /**
   * Getter of the synonym
   */
  public String getSynonym()
  {
    return this.synonym;
  }

  /**
   * Getter of the actual name represented by the synonym
   */
  public String getActualName()
  {
    return this.actualName;
  }

  /**
   * Getter of the synonym type i.e. TYPE etc.
   */
  public SynonymType getSynonymType()
  {
    return this.synonymType;
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
