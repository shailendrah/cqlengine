/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/FileDriver.java /main/16 2011/07/02 22:24:14 anasrini Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares FileDriver in package oracle.cep.interfaces.input.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 anasrini  06/30/11 - XbranchMerge anasrini_bug-12675151_ps5 from
                      st_pcbpel_11.1.1.4.0
 anasrini  06/19/11 - support for partition parallel tests
 anasrini  04/25/11 - XbranchMerge anasrini_bug-11905834_ps5 from main
 anasrini  03/23/11 - partition parallelism
 hopark    02/22/10 - add initialDelay
 hopark    01/29/09 - api change
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 sbishnoi  08/04/08 - support for nanosecond; added convertTs flag in EPR
 skmishra  05/19/08 - fix flushing
 mthatte   04/02/08 - setting isderivedTS
 sbishnoi  02/20/08 - modify CreateDriverContext
 sbishnoi  02/11/08 - error parameterization
 udeshmuk  11/22/07 - set isSystemTimestamped appropriately after creating
                      source.
 parujain  11/01/07 - output unsubscription
 parujain  09/28/07 - change unsubscribe
 dlenkov   07/17/07 - XML format for EPR
 rkomurav  03/27/07 - add delayscale
 najain    03/29/06 - implementation
 skaluska  03/27/06 - implementation
 skaluska  03/24/06 - Creation
 skaluska  03/24/06 - Creation
 skaluska  03/22/06 - Creation
 skaluska  03/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/FileDriver.java /main/14 2010/02/23 07:04:39 hopark Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces;

import java.net.URI;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.interfaces.input.FileSource;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.output.FileDestination;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.TableManager;
import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLDocument;

/**
 * Sharing criteria for FileSource
 *
 * @author skaluska
 */
class SharingCriteria {
  /** file name */
  String file;

  /** table Id */
  int    tableId;

  /**
   * Constructor for SharingCriteria
   * @param file
   * @param tableId
   */
  SharingCriteria(String file, int tableId) {
    // TODO Auto-generated constructor stub

    this.file = file;
    this.tableId = tableId;
  }
  
  public boolean equals(Object obj)
  {
    assert obj instanceof SharingCriteria;
    SharingCriteria crit = (SharingCriteria)obj;
    return(this.file.equals(crit.file) && (this.tableId == crit.tableId));
  }
}

/**
 * SourceDriver that manages reading tuples from files.
 *
 * @author skaluska
 */
public class FileDriver extends InterfaceDriver {
  /** table of mapping from file names to sources */
  private HashMap<SharingCriteria, FileSource> sources;

  /** table of mapping from file names to outputs */
  private HashMap<String, QueryOutput>         destinations;

  /**
   * Constructor for FileDriver
   */
  public FileDriver(ExecContext ec) {
    super(ec, InterfaceType.FILE);
    sources = new HashMap<SharingCriteria, FileSource>();
    destinations = new HashMap<String, QueryOutput>();
  }

  @Override
  public InterfaceDriverContext CreateDriverContext(URI uri, 
    XMLDocument doc, int id) throws CEPException
  {
    String path    = uri.getPath();
    String query   = uri.getQuery();
    int delayScale = -1;
    int startDelay = 0;
    /** flag to check whether we have delay query in EPR*/
    int quryFlag = 0;
    /** flag to check whether we should convert tuple's time-stamp unit
     *  default is true;
     */
    boolean convertTs = true;
    if(query != null)
    {
      //Note: Support for convertTs is temporary;
      // This puts a limitation that in a query you cannot specify
      // both delay and convertTs
      if(query.indexOf("delay=") != -1)
      {
        query = query.substring("delay=".length());
        quryFlag  = 0;
      }
      else if(query.indexOf("convertTs=") != -1)
      {
        query = query.substring("convertTs=".length());        
        quryFlag  = 1;
      }
      else if(query.indexOf("startDelay=") != -1)
      {
        query = query.substring("startDelay=".length());        
        quryFlag  = 2;
      }
      else
        throw new CEPException(InterfaceError.INVALID_EPR_QUERY, query);
      
      try
      {
        switch(quryFlag)
        {
        case 0 :   delayScale = Integer.parseInt(query); break;
        case 1 :   convertTs =  query.equalsIgnoreCase("true"); break;
        case 2 :   startDelay = Integer.parseInt(query); break;
        }  
      }
      catch(NumberFormatException e)
      {
        throw new CEPException(InterfaceError.INVALID_EPR_QUERY, query);
      }
    }
    
    FileDriverContext ctx = new FileDriverContext(execContext, path, id, delayScale);
    ctx.setConvertTs(convertTs);
    ctx.setStartDelay(startDelay);
    return ctx;
  }

  @Override
  public InterfaceDriverContext CreateDriverContext(InterfaceDriver.KeyValue[] vals,
		XMLDocument doc, int id) throws CEPException
  {
    assert vals != null && vals.length == 2;
    assert vals[1].getValue() instanceof String : vals[1].getValue();
    
    String fileName = (String)vals[1].getValue();

    FileDriverContext ctx = new FileDriverContext(execContext, fileName, id, -1);
    return ctx;
  }

  /* (non-Javadoc)
   * @see oracle.cep.interfaces.input.SourceSubscription#subscribe(oracle.cep.interfaces.input.InterfaceDriverContext)
   */
  public TableSource subscribe_source(InterfaceDriverContext desc) {
    FileSource s;
    String file;
    SharingCriteria c;
    int id;
    assert desc instanceof FileDriverContext;
    FileDriverContext ctx = (FileDriverContext) desc;

    // Get subscription info
    file = ctx.getObject_name();
    id = ctx.getId();
    c = new SharingCriteria(file, id);

    // Lookup existing sources
    s = sources.get(c);

    // Do we need to instantiate a new one?
    if (s == null) {
      int delay = ctx.getDelayScale();
      if(delay == -1)
        s = new FileSource(desc.getExecContext(), file);
      else
        s = new FileSource(desc.getExecContext(), file, delay);
      s.setConvertTs(ctx.getConvertTs());
      s.setStartDelay(ctx.getStartDelay());
      sources.put(c, s);
    }
    try 
    {     
      boolean isSysTs = execContext.getTableMgr().isSystemTimestamped(id);
      s.setSystemTimeStamped(isSysTs);
      boolean isSilent = execContext.getTableMgr().getTable(id).getIsSilent();
      s.setSilent(isSilent);
      boolean isDerivedTS = execContext.getTableMgr().getTable(id).isDerivedTs();
      s.setDerivedTimeStamped(isDerivedTS);
    }
    catch(oracle.cep.metadata.MetadataException e)
    {
      LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, e);
    }
    return s;
  }

  /* (non-Javadoc)
   * @see oracle.cep.interfaces.input.SourceSubscription#unsubscribe(oracle.cep.interfaces.input.TableSource)
   */
  public void unsubscribe_source(InterfaceDriverContext desc) {
    String file;
    SharingCriteria c;
    int id;
    assert desc instanceof FileDriverContext;
    FileDriverContext ctx = (FileDriverContext) desc;

    // Get subscription info
    file = ctx.getObject_name();
    id = ctx.getId();
    c = new SharingCriteria(file, id);
    
    Set<SharingCriteria> keySet = sources.keySet();
    for(SharingCriteria key: keySet)
    {
      if(key.equals(c))
      {
        Object a = sources.remove(key);
        return;
      }
    }

  }

  /* (non-Javadoc)
   * @see oracle.cep.interfaces.Subscription#subscribe_output(oracle.cep.interfaces.InterfaceDriverContext)
   */
  public QueryOutput subscribe_output(InterfaceDriverContext desc) {
    QueryOutput s;
    String file;
    assert desc instanceof FileDriverContext;
    FileDriverContext ctx = (FileDriverContext) desc;

    // Get subscription info
    file = ctx.getObject_name();

    if (execContext != null)
    {
      String prefix = execContext.getPartitionParallelContext();
      if (prefix != null)
      {
        int    lastSlash = file.lastIndexOf('/');
        String part1     = file.substring(0, lastSlash+1);
        String part2     = file.substring(lastSlash+1);

        file = part1 + "f" + prefix + "_" + part2;
      }
    }

    // Lookup existing sources
    s = destinations.get(file);

    // Do we need to instantiate a new one?
    if (s == null) {
      s = new FileDestination(execContext, file);
      s.setConvertTs(ctx.getConvertTs());
      destinations.put(file, s);
    }

    return s;
  }

  /* (non-Javadoc)
   * @see oracle.cep.interfaces.Subscription#unsubscribe_output(oracle.cep.interfaces.output.QueryOutput)
   */
  public void unsubscribe_output(InterfaceDriverContext desc) {
    String fileName;
    assert desc instanceof FileDriverContext;
    FileDriverContext ctx = (FileDriverContext)desc;
    
    fileName = ctx.getObject_name();
    
    QueryOutput s = destinations.get(fileName);
    
    if (s != null)
    {
      try {
        s.end();
      }catch(CEPException e)
      {
      }
      destinations.remove(fileName);
    }
  }
}
