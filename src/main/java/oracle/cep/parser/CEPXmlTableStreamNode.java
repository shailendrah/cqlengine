/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPXmlTableStreamNode.java /main/4 2011/05/19 15:28:46 hopark Exp $ */

/* Copyright (c) 2007, 2011, Oracle and/or its affiliates. 
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
    skmishra    01/26/09 - adding getQCXML
    parujain    08/13/08 - error offset
    mthatte     12/26/07 - 
    najain      12/03/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPXmlTableStreamNode.java /main/3 2009/02/19 11:21:29 skmishra Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Parse tree node for a xmltable stream. 
 * <p> This is obtained as a result of xmltable clause over a stream.
 */

public class CEPXmlTableStreamNode extends CEPStreamNode 
{
  /** The base stream over which the xmltable is specified **/
  protected CEPStreamNode baseStream;

  /** The XMLTable clause **/
  protected CEPXmlTableNode xmlTableDesc;

  /**
   * Constructor
   * @param baseStream the stream over which the pattern is being detected
   * @param patternDesc the pattern being detected
   * @param alias the alias
   */
  public CEPXmlTableStreamNode(CEPStreamNode baseStream, 
			CEPXmlTableNode xmlTableDesc,
			CEPStringTokenNode aliasNode) 
  {
    this.baseStream  = baseStream;
    this.xmlTableDesc = xmlTableDesc;
    setAlias(aliasNode.getValue());
    setStartOffset(baseStream.getStartOffset());
    setEndOffset(aliasNode.getEndOffset());
  }

  public CEPStreamNode getBaseStream() {
    return baseStream;
  }

  public CEPXmlTableNode getXmlTableDesc()
  {
    return xmlTableDesc;
  }

  public int toQCXML(StringBuffer queryXml, int operatorID)
      throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("Not supported for xml clauses");
  }
}
