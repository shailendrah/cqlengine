/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/MLFFileDriver.java /main/5 2009/02/17 17:42:52 hopark Exp $ */

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
 hopark      10/09/08 - remove statics
 sbishnoi    08/05/08 - adding support for convertTs in EPR
 mthatte     04/02/08 - setting isderivedTS
 udeshmuk    03/07/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/MLFFileDriver.java /main/5 2009/02/17 17:42:52 hopark Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces;

import java.net.URI;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.interfaces.input.MultiLineFileSource;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.TableManager;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLDocument;

/**
 * Source driver for files that possibly contain multi-line-fields All fields
 * are separated by a field separator (default ',') The multi-line-fields are
 * encapsulated by encapsulator character (default '"')
 */

public class MLFFileDriver extends InterfaceDriver
{

  public MLFFileDriver(ExecContext ec)
  {
    super(ec, InterfaceType.MLFFILE);
  }

  @Override
  public InterfaceDriverContext CreateDriverContext(URI uri, XMLDocument doc,
      int tableId) throws CEPException
  {
    String path = uri.getPath();
    String query = uri.getQuery();
    char fieldSeparator;
    char encapsulator;
    MLFFileDriverContext mlfDriverContext = null;
    boolean convertTs = true;
    // Note: Suppport for convertTS is a temporary support;
    // This put a limitation that you cannot have both fieldSeparator-encapsulator    
    // and convertTs in same query
    if (query != null && query.indexOf('&') != -1)
    {
      String[] temp = query.split("&"); // FS and EC will be separated by '&'
      fieldSeparator = temp[0].charAt(temp[0].length() - 1); // get FS
      encapsulator = temp[1].charAt(temp[1].length() - 1); // get EC
      mlfDriverContext = new MLFFileDriverContext(execContext, path, tableId, fieldSeparator,
          encapsulator);
    }
    else if(query != null && query.indexOf("convertTs=") != -1)
    {
      mlfDriverContext = new MLFFileDriverContext(execContext, path, tableId);
      convertTs = query.substring("convertTs=".length()).equals("true");
    }
    else
      mlfDriverContext = new MLFFileDriverContext(execContext, path, tableId);
    mlfDriverContext.setConvertTs(convertTs);
    return mlfDriverContext;
  }

  @Override
  public InterfaceDriverContext CreateDriverContext(InterfaceDriver.KeyValue[] vals,
      XMLDocument doc, int tableId) throws CEPException
  {
    return new MLFFileDriverContext(execContext, vals, tableId);
  }

  public QueryOutput subscribe_output(InterfaceDriverContext desc)
      throws CEPException
  {
    assert false;
    return null;
  }

  public TableSource subscribe_source(InterfaceDriverContext desc)
      throws CEPException
  {
    MultiLineFileSource s = null;
    String file = null;
    int id = 0;
    char fs, ec;
    assert desc instanceof MLFFileDriverContext;
    MLFFileDriverContext ctx = (MLFFileDriverContext) desc;

    file = ctx.getObject_name();
    id = ctx.getId();
    fs = ctx.getFS();
    ec = ctx.getEC();
    // System.out.println("SubscribeSource: FS = "+fs+" EC = "+ec);
    s = new MultiLineFileSource(execContext, file, fs, ec);
    try
    {
      boolean isSysTs = execContext.getTableMgr().isSystemTimestamped(id);
      s.setSystemTimeStamped(isSysTs);
      boolean isSilent = execContext.getTableMgr().getTable(id)
          .getIsSilent();
      s.setSilent(isSilent);
      boolean isDerivedTS = execContext.getTableMgr().getTable(id)
          .isDerivedTs();
      s.setDerivedTimeStamped(isDerivedTS);
      boolean isStream = execContext.getTableMgr().getTable(id)
          .isBStream();
      s.setIsStream(isStream);
    } catch (oracle.cep.metadata.MetadataException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    s.setConvertTs(ctx.getConvertTs());
    return s;
  }

  public void unsubscribe_output(InterfaceDriverContext desc)
      throws CEPException
  {
    assert false;
  }

  public void unsubscribe_source(InterfaceDriverContext desc)
      throws CEPException
  {
    // TODO Auto-generated method stub

  }
}
