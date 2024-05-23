/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/QueryOutputXMLBase.java /main/8 2009/11/09 10:10:59 sborah Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    A base class implementation of the QueryOutput interface for those
    that output in XML format

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sborah      10/12/09 - support for bigdecimal
    hopark      10/10/08 - remove statics
    sbishnoi    10/22/08 - support for old epr destinatoin
    skmishra    10/13/08 - adding payload namespace
    hopark      10/30/07 - remove IQueueElement
    dlenkov     10/10/07 - fixed namespaces
    najain      03/12/07 - bug fix
    anasrini    10/25/06 - Creation
    anasrini    10/25/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/output/QueryOutputXMLBase.java /main/8 2009/11/09 10:10:59 sborah Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.interfaces.output;

import oracle.cep.dataStructures.internal.QueueElement;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.exceptions.CEPException;
import com.oracle.osa.exceptions.ErrorCode;
import oracle.cep.interfaces.TupleValueHelper;
import oracle.cep.service.ExecContext;


public abstract class QueryOutputXMLBase extends QueryOutputBase {

  private ErrorCode ec;
  protected String eventNsp;
  protected String eventName;
  protected String payloadNameSpace;
  protected boolean isNewEPRFormat;
  
  protected QueryOutputXMLBase(ExecContext execCtx, ErrorCode ec) {
    super(execCtx);
    this.ec = ec;
  }

  public void putNext(TupleValue tuple, QueueElement.Kind kind) throws CEPException
  {
    String outputXML = null;
    // If Payload namespace is not mentioned
    // Assumption is that select list contains xml output
    if(payloadNameSpace != null)
    {
      outputXML =
        TupleValueHelper.convertTValueToNspXML(tuple, kind, payloadNameSpace, 
                                               numAttrs, attrMetadata, attrNames,
                                               isNewEPRFormat);
    }
    else
    {
      outputXML = new String(tuple.xValueGet(0));
    }
 
    try {
      putNextInternal(outputXML);
    } 
    catch(CEPException ce) {
      throw ce;
    }
    catch (Exception e) {
      throw new CEPException(ec, e);
    }
  }

  protected abstract void putNextInternal(String xmlData) throws Exception;

}
