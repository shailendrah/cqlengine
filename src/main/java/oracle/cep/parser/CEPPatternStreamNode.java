/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPPatternStreamNode.java /main/8 2011/09/23 11:16:36 vikshukl Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    Parse tree node for a pattern stream. This is obtained as a result of
    detecting patterns over a stream.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    09/12/11 - subquery support
    hopark      04/21/11 - make public to be reused in cqservice
    skmishra    01/26/09 - adding getQCXML
    parujain    08/13/08 - error offset
    anasrini    01/09/07 - Creation
    anasrini    01/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPPatternStreamNode.java /main/6 2009/02/27 14:19:31 skmishra Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.parser;

/**
 * Parse tree node for a pattern stream. 
 * <p> This is obtained as a result of detecting patterns over a stream.
 */

public class CEPPatternStreamNode extends CEPStreamNode {

  /** The base stream over which the pattern is detected **/
  protected CEPStreamNode stream;

  /** The RECOGNIZE clause for the pattern being detected **/
  protected CEPRecognizeNode patternDesc;

  /**
   * Constructor
   * @param baseStream the stream over which the pattern is being detected
   * @param patternDesc the pattern being detected
   * @param alias the alias
   */
  public CEPPatternStreamNode(CEPStreamNode stream, 
                       CEPRecognizeNode patternDesc,
                       CEPStringTokenNode aliasToken) {
    
    this.stream  = stream;
    this.patternDesc = patternDesc;
    setAlias(aliasToken.getValue());
    setStartOffset(stream.getStartOffset());
    setEndOffset(aliasToken.getEndOffset());
  }

  public boolean isQueryStreamNode() 
  {
    return stream.isQueryStreamNode();
  }
  
  // getter methods

  /**
   * Get the stream over which the pattern is being detected
   * @return the stream over which the pattern is being detected
   */
  public CEPStreamNode getStream() {
    return stream;
  }

  /**
   * Get the RECOGNIZE clause describing the pattern being detected
   * @return the RECOGNIZE clause describing the pattern being detected
   */
  public CEPRecognizeNode getPatternDesc() {
    return patternDesc;
  }

  public String getName() {
    return stream.getName();
  }

  public String toString()
  {
    StringBuilder s = new StringBuilder(50);
    s.append(" " + getName()); //stream name
    s.append(" MATCH_RECOGNIZE("); //match recognize clause
    s.append(this.patternDesc.toString());
    s.append(")");
    if(this.alias != null)
      s.append(" AS " + alias);
    return s.toString();
  }
  
  public int toQCXML(StringBuffer queryXml, int operatorID)
      throws UnsupportedOperationException
  {
    int rootID;
    
    String streamCql = " select * from ";
    stream.setCqlProperty(streamCql.concat(stream.toString()));
    patternDesc.setCqlProperty(streamCql.concat(this.toString()));
    rootID = stream.toQCXML(queryXml, operatorID);
    if(alias != null)
      patternDesc.setAlias(alias);
    rootID = patternDesc.toQCXML(queryXml, rootID);
    return rootID;
  }
}
