/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPRelOrStreamRefNode.java /main/3 2011/05/19 15:28:46 hopark Exp $ */

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
    dlenkov     09/05/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/src/oracle/cep/parser/CEPRelOrStreamRefNode.java /main/2 2008/08/25 19:27:24 parujain Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node corresponding to a drop DDL for streams & relations
 *
 * @since 1.0
 */
public class CEPRelOrStreamRefNode implements CEPParseTreeNode {

  /** The name of the stream or relation */
  protected String name;

  protected boolean isStream;
  
  protected int startOffset;
  
  protected int endOffset;

  /**
   * Constructor 
   *
   * @param name - name of the relation or stream
   * @param isStream - whether it is a stream reference
   */
  public CEPRelOrStreamRefNode( CEPStringTokenNode nameToken, boolean isStream) {
    this.name     = nameToken.getValue();
    this.isStream = isStream;
    setStartOffset(nameToken.getStartOffset());
    setEndOffset(nameToken.getEndOffset());
  }

  /**
   * @return Returns the name.
   */
  public String getName()
  {
    return name;
  }

  /**
   * @return Returns whether it is a stream reference.
   */
  public boolean getIsStream()
  {
    return isStream;
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
