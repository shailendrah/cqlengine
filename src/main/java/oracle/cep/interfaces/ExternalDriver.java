/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/ExternalDriver.java /main/4 2009/02/17 17:42:52 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      01/29/09 - api change
    hopark      10/09/08 - remove statics
    sbishnoi    02/19/08 - modify createDriverContext
    parujain    11/09/07 - External Source
    parujain    11/09/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/ExternalDriver.java /main/4 2009/02/17 17:42:52 hopark Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.interfaces;

import java.net.URI;

import oracle.cep.exceptions.CEPException;
import oracle.cep.interfaces.input.ExtSource;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLDocument;

public class ExternalDriver extends InterfaceDriver {

  public ExternalDriver(ExecContext ec) {
    super(ec, InterfaceType.EXTERNAL);
   
  }

  @Override
  public InterfaceDriverContext CreateDriverContext(URI uri, 
    XMLDocument doc, int tableId) throws CEPException 
  {
    String connectMe = uri.getSchemeSpecificPart();
    return new ExternalDriverContext(execContext, connectMe, tableId);
  }

  @Override
  public InterfaceDriverContext CreateDriverContext(InterfaceDriver.KeyValue[] vals, 
    XMLDocument doc, int tableId) throws CEPException 
  {
    assert vals[0].getValue() instanceof String : vals[0].getValue();
    String connectMe = (String)vals[0].getValue();
    return new ExternalDriverContext(execContext, connectMe, tableId);
  }

  public QueryOutput subscribe_output(InterfaceDriverContext desc) {
    return null;
  }

  public TableSource subscribe_source(InterfaceDriverContext desc) {
    assert desc instanceof ExternalDriverContext;
    ExternalDriverContext ctx = (ExternalDriverContext)desc;
    
    return new ExtSource(desc.getExecContext(), ctx.getConnectString(), ctx.getTableId());
  }

  public void unsubscribe_output(InterfaceDriverContext desc) {

  }

  public void unsubscribe_source(InterfaceDriverContext desc) {

  }


}
