/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/DBDriver.java /main/4 2009/02/17 17:42:52 hopark Exp $ */

/* Copyright (c) 2008, 2009, Oracle and/or its affiliates.
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
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    hopark      06/26/08 - use datasource
    sbishnoi    03/10/08 - Creation
 */

package oracle.cep.interfaces;

import java.net.URI;

import oracle.cep.exceptions.CEPException;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.interfaces.output.DBDestination;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLDocument;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/DBDriver.java /main/4 2009/02/17 17:42:52 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class DBDriver extends InterfaceDriver{
  
  public DBDriver(ExecContext ec){
    super(ec, InterfaceType.DB);
  }
  
  @Override
  public InterfaceDriverContext CreateDriverContext(URI uri, 
    XMLDocument doc, int tableId) throws CEPException 
  {
    return null;
  }

  @Override
  public InterfaceDriverContext CreateDriverContext(InterfaceDriver.KeyValue[] vals, 
    XMLDocument doc, int tableId) throws CEPException 
  {
    assert vals.length == 2;
    return new DBDriverContext(execContext, vals[1].getValue());
  }
  
  /**
   * Subscribes Output of query to Database
   * Returns an instance of DBDestination
   */
  public QueryOutput subscribe_output(InterfaceDriverContext desc) {
    assert desc instanceof DBDriverContext;
    DBDriverContext ctx = (DBDriverContext)desc;
    return new DBDestination(ctx.getExecContext(),
                             ctx.getDataSourceName(), ctx.getTableName());
    
  }

  /**
   * Currently, we support DB as a destination only
   */
  public TableSource subscribe_source(InterfaceDriverContext desc) {
    return null;
  }
  
  /**
   * unsubscribe output
   */
  public void unsubscribe_output(InterfaceDriverContext desc) {
  }

  /**
   * Currently we support DB as a destination only
   */
  public void unsubscribe_source(InterfaceDriverContext desc) {
  }

}
