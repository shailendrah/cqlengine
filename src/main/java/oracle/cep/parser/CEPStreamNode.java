/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/parser/CEPStreamNode.java /main/4 2011/07/14 11:10:28 vikshukl Exp $ */

/* Copyright (c) 2005, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    vikshukl    06/14/11 - subquery support
    hopark      04/21/11 - make public to be reused in cqservice
    mthatte     01/26/09 - adding getQCXML
    anasrini    12/20/05 - parse tree node for a stream 
    anasrini    12/20/05 - parse tree node for a stream 
    anasrini    12/20/05 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/parser/CEPStreamNode.java /main/2 2009/02/19 11:21:29 skmishra Exp $
 *  @author  anasrini
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.parser;

/**
 * Base class for a stream parse tree node
 */
public abstract class CEPStreamNode extends CEPBaseEntityNode {  
  /**
   * whether this stream is derived from an inline subquery
   */
  public boolean isQueryStreamNode()
  {
    return false;
  } 
}
