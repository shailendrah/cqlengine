/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPMultiStreamNode.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $ */

/* Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       06/26/12 - parser tree node for multiple stream list
    pkali       06/26/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPMultiStreamNode.java /main/1 2012/07/13 02:49:24 sbishnoi Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

import java.util.List;

/**
 * Parse tree node for multiple stream list which might have subquery source 
 * also
 */

public class CEPMultiStreamNode extends CEPStreamNode {
  
  /** The streams over which the pattern is detected **/
  protected CEPStreamNode[] streams;
  
  /** The RECOGNIZE clause for the pattern being detected **/
  protected CEPRecognizeNode patternDesc;
  
  CEPStringTokenNode aliasToken;
  
  /** Generated Query string at semantic level.
   * For multistream node interpretation an alternate query 
   * is generated with union all operator.
   */
  String queryString = "";

  /**
   * Constructor
   * @param baseStream the stream over which the pattern is being detected
   * @param patternDesc the pattern being detected
   * @param alias the alias
   */
  public CEPMultiStreamNode(List<CEPStreamNode> streams, 
                       CEPRecognizeNode patternDesc,
                       CEPStringTokenNode aliasToken) {
    
    this.streams = (CEPStreamNode[])(streams.toArray(
        new CEPStreamNode[streams.size()]));
    this.patternDesc = patternDesc;
    this.aliasToken = aliasToken; 
    setAlias(aliasToken.getValue());
    if(streams.size() > 0)
      setStartOffset(streams.get(0).getStartOffset());
    else
      setStartOffset(aliasToken.getStartOffset());
    setEndOffset(aliasToken.getEndOffset());
  }
  
  public CEPStreamNode[] getStreamNodes()
  {
    return streams;
  }
  
  public CEPRecognizeNode getPatternDesc() {
    return patternDesc;
  }
  
  public CEPStringTokenNode getAliasToken() {
    return aliasToken;
  }
  
  public void setQueryString(String queryString)
  {
    setCqlProperty(queryString);
    this.queryString = queryString;  
  }
  
  public String toString() 
  {
    return queryString;  
  }
  
  public int toQCXML(StringBuffer queryXml, int operatorID)
      throws UnsupportedOperationException
  {
    int rootID;
    String streamCql = " select * from " + queryString;
    patternDesc.setCqlProperty(streamCql);
    if(alias != null)
      patternDesc.setAlias(alias);
    rootID = patternDesc.toQCXML(queryXml, operatorID);
    return rootID;
  }
}