/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPAddTableSourceNode.java /main/5 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
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
    parujain    08/11/08 - error offset
    dlenkov     09/06/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPAddTableSourceNode.java /main/4 2008/08/25 19:27:24 parujain Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to an add source DDL for streams & relations
 *
 * @since 1.0
 */
public class CEPAddTableSourceNode implements CEPParseTreeNode {

  /** The name of the stream or relation */
  protected String name;

  protected String source;

  protected boolean isStream;

  protected boolean isPush;
  
  protected int startOffset;
  
  protected int endOffset;

  /**
   * Constructor 
   *
   * @param name - name of the relation or stream
   * @param isStream - whether it is a stream reference
   */
  public CEPAddTableSourceNode( CEPStringTokenNode nameToken, CEPStringTokenNode sourceToken, boolean isStream) {
    this.name     = nameToken.getValue();
    this.source   = sourceToken.getValue();
    this.isStream = isStream;
    if (source == null)
      isPush = true;
    else
      isPush = false;
    setStartOffset(nameToken.getStartOffset());
    this.endOffset = (source != null) ? sourceToken.getEndOffset() : nameToken.getEndOffset();
      
  }

  public CEPAddTableSourceNode( CEPStringTokenNode nameToken, CEPStringTokenNode sourceToken,
			 boolean isStream, boolean isPush) {
    this.name     = nameToken.getValue();
    this.source   = sourceToken.getValue();
    this.isStream = isStream;
    this.isPush = isPush;
    setStartOffset(nameToken.getStartOffset());
    this.endOffset = (source != null) ? sourceToken.getEndOffset() : nameToken.getEndOffset();
  }

  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @return Returns the source
   */
  public String getSource()
  {
    return source;
  }

  /**
   * @return Returns whether it is a stream reference.
   */
  public boolean getIsStream()
  {
    return isStream;
  }

  /**
   * @return Returns whether its source is a push source.
   */
  public boolean getIsPush()
  {
    return isPush;
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
