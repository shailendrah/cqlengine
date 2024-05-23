/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/InterfaceManager.java /main/26 2010/11/19 07:47:47 udeshmuk Exp $ */

/* Copyright (c) 2006, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares InputManager in package oracle.cep.interfaces.input.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 udeshmuk  09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                      st_pcbpel_11.1.1.4.0
 udeshmuk  09/22/10 - propagate hb
 sbishnoi  12/09/09 - batching events support
 hopark    12/02/09 - add maxLen in layout
 hopark    02/06/09 - remove fabric driver
 hopark    01/29/09 - parseXMLEpr gets kv
 anasrini  02/12/09 - check for isRegressPushMode
 hopark    10/10/08 - remove statics
 hopark    10/09/08 - remove statics
 sbishnoi  10/14/08 - support for payloadnamespace
 skmishra  09/25/08 - using reflection for FabricDriver
 hopark    04/13/08 - server reorg
 hopark    03/09/08 - associate evictPolicty to queueSource
 sbishnoi  03/10/08 - adding db destination
 udeshmuk  03/07/08 - support for multi-line-field file.
 sbishnoi  02/18/08 - modify parseXMLEpr
 sbishnoi  02/11/08 - error parameterization
 parujain  12/12/07 - logging
 sbishnoi  12/11/07 - support for java/class as interface
 sbishnoi  12/10/07 - support for Class as an epr destination
 hopark    11/16/07 - remove uriStr
 parujain  11/09/07 - External source
 udeshmuk  11/22/07 - set isSystemTimestamped when forming queuesource.
 parujain  11/01/07 - output unsubscription
 parujain  10/17/07 - cep-bam integration
 skmishra  10/10/07 - suppress print introduced by dmitry
 parujain  09/25/07 - support push source
 dlenkov   07/17/07 - XML format for EPR
 anasrini  10/25/06 - support for FABRIC as destination
 najain    08/23/06 - support push source
 anasrini  08/17/06 - support for JMS
 najain    05/11/06 - set output driver 
 najain    04/06/06 - cleanup
 najain    03/31/06 - add getQueryOutput 
 najain    03/28/06 - implementation
 skaluska  03/22/06 - implementation
 skaluska  03/22/06 - Creation
 skaluska  03/22/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/InterfaceManager.java /main/26 2010/11/19 07:47:47 udeshmuk Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.interfaces;

import java.util.HashMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import oracle.cep.common.Datatype;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.execution.queues.IQueue;
import oracle.cep.interfaces.input.QueueSource;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.cep.memmgr.FactoryManager;
import oracle.cep.memmgr.IEvictPolicy;
import oracle.cep.memmgr.IEvictPolicyCallback;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.CEPManager;
import oracle.cep.service.ExecContext;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.dataStructures.external.TupleValue;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.net.URI;

/**
 * The InterfaceManager encapsulates all input/output activity. It keeps track
 * of the drivers associated with various sources. It arranges for tuple
 * input/output for a relation/stream/query.
 * 
 * @author skaluska
 */
public class InterfaceManager
{
  ExecContext   execContext;
  
  /** Mapping from interface type to the driver */
  private HashMap<InterfaceType, InterfaceDriver> drivers;

  /** Mapping from interface name string to the driver */
  private HashMap<String, InterfaceDriver>        iDrivers;

  // TODO: both the mappings may not be needed, the only reason I am adding
  // iDrivers is because I dont see a convenient way to get the InterfaceType
  // from the correspondng string.

  /**
   * Constructor for InterfaceManager. 
   */
  public InterfaceManager(ExecContext ec)
  {
    execContext = ec;
  }

  public void init()
  {
    drivers = new HashMap<InterfaceType, InterfaceDriver>();
    drivers.put(InterfaceType.FILE, new FileDriver(execContext));
    drivers.put(InterfaceType.MLFFILE, new MLFFileDriver(execContext));
    drivers.put(InterfaceType.JAVA, new JavaDriver(execContext));
    drivers.put(InterfaceType.DB, new DBDriver(execContext));
  
    iDrivers = new HashMap<String, InterfaceDriver>();
    iDrivers.put(new String("file"), new FileDriver(execContext));
    iDrivers.put(new String("mlffile"), new MLFFileDriver(execContext));
    iDrivers.put(new String("external"), new ExternalDriver(execContext));
    iDrivers.put(new String("java"), new JavaDriver(execContext));
    iDrivers.put(new String("db"), new DBDriver(execContext));
  }


  private URI parseEpr(String epr, int id) throws CEPException
  {
    DOMParser dp;
    XMLDocument doc;

    // Determine the driver based on the source type
    try
    {
      dp = new DOMParser();
      // create a document from the source
      Reader reader = new StringReader(epr);
      dp.parse(reader);
      doc = dp.getDocument();
    }
    catch (Exception e)
    {
      throw new CEPException(InterfaceError.INVALID_SOURCE, e, epr);
    }

    NodeList nl = doc.getChildrenByTagName(new String("EndPointReference"));

    // Since the document has been validated at insertion time, there should
    // be only 1 such node
    assert nl.getLength() == 1;
    Node n = nl.item(0);

    assert n instanceof XMLElement;
    XMLElement elem = (XMLElement) n;

    NodeList lst = elem.getChildrenByTagName(new String("Address"));
    // Since the document has been validated at insertion time, there should
    // be only 1 such node
    assert lst.getLength() == 1;
    Node addrElem = lst.item(0);

    NodeList childAddrElemLst = addrElem.getChildNodes();
    assert childAddrElemLst.getLength() > 0;
    Node childAddrElem = childAddrElemLst.item(0);

    if (childAddrElem.getNodeType() == Node.ELEMENT_NODE)
      return null;

    assert childAddrElemLst.getLength() == 1;
    String uriStr = childAddrElem.getNodeValue();
        
    // Append Reference Parameters information to URI
    String queryString = null;
    NodeList referenceParamNodeList 
      = elem.getChildrenByTagName(new String("ReferenceParameters"));
    assert referenceParamNodeList.getLength() <= 1;    
    if(referenceParamNodeList.getLength() == 1)
    {
      Node referenceParamNode = referenceParamNodeList.item(0);
      queryString = parseReferenceParams(referenceParamNode);
    }
    
    URI uri;
    
    if(queryString != null && queryString.length() > 0)
    {
      StringBuffer finalURI = new StringBuffer();;
      finalURI.append(uriStr);
      if(uriStr.contains("?"))
        finalURI.append("&" + queryString);
      else
        finalURI.append("?" + queryString);
      uriStr = finalURI.toString();
    }
    LogUtil.info(LoggerType.TRACE, "InterfaceManager: uriStr = " + uriStr);

    

    try
    {
      uri = new URI(uriStr);
    }
    catch (Exception ex)
    {
      uri = null;
    }

    return uri;
  }

  private String parseReferenceParams(Node referenceParamNode)
  {
    StringBuffer queryString = new StringBuffer();
    NodeList referenceParamChildNodeList = referenceParamNode.getChildNodes();
    int numReferenceParamChildNodes = referenceParamChildNodeList.getLength();
    
    for(int i = 0; i < numReferenceParamChildNodes; i++)
    {
      if(i != 0)
        queryString.append("&");
      Node referenceParamChildNode = referenceParamChildNodeList.item(i);
      queryString.append(referenceParamChildNode.getNodeName() + "=");
      queryString.append(
          referenceParamChildNode.getChildNodes().item(0).getNodeValue());
    }
    return queryString.toString();
  }
  
  private InterfaceDriver.KeyValue[] parseXMLEpr(String epr, int id) throws CEPException
  {
    DOMParser dp;
    XMLDocument doc;

    LogUtil.info(LoggerType.TRACE, "InterfaceManager: XMLepr = " + epr);

    // Determine the driver based on the source type
    try
    {
      dp = new DOMParser();
      // create a document from the source
      Reader reader = new StringReader(epr);
      dp.parse(reader);
      doc = dp.getDocument();
    }
    catch (Exception e)
    {
      throw new CEPException(InterfaceError.INVALID_SOURCE, e);
    }

    NodeList nl = doc.getChildrenByTagName(new String("EndPointReference"));

    // Since the document has been validated at insertion time, there should
    // be only 1 such node
    assert nl.getLength() == 1;
    Node n = nl.item(0);

    assert n instanceof XMLElement;
    XMLElement elem = (XMLElement) n;

    NodeList lst = elem.getChildrenByTagName(new String("Address"));
    // Since the document has been validated at insertion time, there should
    // be only 1 such node
    assert lst.getLength() == 1;
    Node addrElem = lst.item(0);

    NodeList childAddrElemLst = addrElem.getChildNodes();
    int elemNum = childAddrElemLst.getLength();
    assert elemNum > 0;
    Node childElem = null;
    InterfaceDriver.KeyValue[] addrVals = new InterfaceDriver.KeyValue[elemNum];

    NodeList valElemLst = null;
    Node valElem = null;
    
    for (int j=0; j < elemNum; j++) {
      childElem = childAddrElemLst.item(j);
      
      valElemLst = childElem.getChildNodes();
      valElem = valElemLst.item(0);
      // In case if epr contains Arguments node; Node list inside Arguments will
      // be processed by JavaDriver or MLFFileDriver or DBDriver
      if(childElem.getNodeName().equalsIgnoreCase("Arguments"))
        addrVals[j] = new InterfaceDriver.KeyValue(childElem.getNodeName(), childElem);
      else
        addrVals[j] = new InterfaceDriver.KeyValue(childElem.getNodeName(), valElem.getNodeValue());
    }
    
    // At lest we need a type 
    if (addrVals.length < 1)
    {
      throw new CEPException(InterfaceError.INVALID_SOURCE, null, epr);
    }
    return addrVals;
  }

  private InterfaceDriverContext createDriverContext(int tableId, String epr)
    throws CEPException
  {
    InterfaceDriverContext ctx;
    InterfaceDriver df;
    
    URI uri = parseEpr(epr, tableId);

    if (uri != null)
    {
      // Get driver
      df = iDrivers.get(uri.getScheme());
      if (df == null)
      {
        throw new CEPException(InterfaceError.INVALID_SOURCE, null, epr);
      }

      ctx = df.CreateDriverContext(uri, null, tableId);
      ctx.setDriver(df);
      return ctx;
    }

    InterfaceDriver.KeyValue[] vals = parseXMLEpr(epr, tableId);
    
    // Get driver
    df = iDrivers.get(vals[0].getValue());
    if (df == null)
    {
      throw new CEPException(InterfaceError.INVALID_SOURCE, null, epr);
    }
    
    // Get the table source
    ctx = df.CreateDriverContext(vals, null, tableId);
    ctx.setDriver(df);
    return ctx;
  }
 
  
  /**
   * Remove a TableSource registered for a given stream/relation
   * @param tableId
   *          Tableid for the stream/relation
   * @throws CEPException
   */
  public void removeTableSource(int tableId) throws CEPException
  {
    // Get the EPR for the table
    String epr = execContext.getTableMgr().getTableSource(tableId);
    removeTableSource(tableId, epr);
  }
  
  /**
   * Remove a TableSource registered for a given stream/relation and epr
   * @param tableId
   *          Tableid for the stream/relation
   * @param epr
   *          EPR for source.
   * @throws CEPException
   */
  public void removeTableSource(int tableId, String epr) throws CEPException

  {
    InterfaceDriverContext ctx;

    if (epr != null)
    {
      ctx = createDriverContext(tableId, epr);
      if (ctx != null)
      {
         // Remove the table source
        InterfaceDriver df = ctx.getDriver();
        df.unsubscribe_source(ctx);
      }
    }
    else
    {
      boolean pushSrc = execContext.getTableMgr().isTablePushSource(tableId);
      if (pushSrc)
      {
        IEvictPolicy curPolicy = execContext.getServiceManager().getEvictPolicy();
        if (curPolicy != null && curPolicy.isUsingCallback())
        {
          //TOD get queue source que and remove call back.
          //curPolicy.removeCallback(q);
        }
      }  
    }
  }

  
  /**
   * Get a TableSource for a given stream/relation
   * @param tableId
   *          Tableid for the stream/relation
   * @return TableSource
   * @throws CEPException
   */
  public TableSource getTableSource(int tableId) throws CEPException
  {
    // Get the EPR for the table
    String epr = execContext.getTableMgr().getTableSource(tableId);
    return getTableSource(tableId, epr);
  }

 /**
   * Get a TableSource for a given stream/relation and epr
 * @param tableId
   *          Tableid for the stream/relation
 * @param epr
   *          EPR for source
   * 
   * @return TableSource
   * @throws CEPException
   */
  public TableSource getTableSource(int tableId, String epr) throws CEPException

  {
    TableSource t = null;
    InterfaceDriverContext ctx;
    InterfaceDriver df;
    boolean isDirectInterop 
      = execContext.getServiceManager().getConfigMgr().getDirectInterop();
    boolean isRegressPushMode 
      = execContext.getServiceManager().getConfigMgr().isRegressPushMode();
    
    if (epr != null)
    {
      ctx = createDriverContext(tableId, epr);
      if (ctx != null)
      {
        df = ctx.getDriver();
        t = df.subscribe_source(ctx);

        if (t == null)
          return t;
        
        if(isRegressPushMode && isDirectInterop && t.supportsPushEmulation())
        {
          QueueSource qs = new QueueSource(execContext, t);
          return qs;
        }
        return t;
      }
      return null;
    }

    boolean pushSrc = execContext.getTableMgr().isTablePushSource(tableId);
    if (pushSrc)
    {
      int numAttrs = execContext.getTableMgr().getNumAttrs(tableId);
      Datatype[] dty   = new Datatype[numAttrs];
      int[] maxlens = new int[numAttrs];
      String[]   names = new String[numAttrs];

      for (int i = 0; i < numAttrs; i++)
      {
        dty[i]   = execContext.getTableMgr().getAttrType(tableId, i);
        maxlens[i] = execContext.getTableMgr().getAttrLen(tableId, i);
        names[i] = execContext.getTableMgr().getAttrName(tableId, i);
      }

      IQueue<TupleValue> qSrcq = null;
      CEPManager cepMgr = execContext.getServiceManager(); 
      ConfigManager cm = cepMgr.getConfigMgr();
      boolean useSpillQ = cm.getUseSpilledQueueSrc();
      if (useSpillQ)
      {
        qSrcq = new oracle.cep.execution.queues.stored.SimpleQueue<TupleValue>(cepMgr, dty, maxlens);
      }
      else
      {
        FactoryManager factoryMgr = cepMgr.getFactoryManager();
        qSrcq = new oracle.cep.execution.queues.SimpleQueue<TupleValue>(factoryMgr);
      }
      QueueSource qSrc = new QueueSource(execContext, numAttrs, dty, names, qSrcq);
      // Set whether Stream or relation
      qSrc.setIsStream(execContext.getTableMgr().isStream(tableId));
      qSrc.setSystemTimeStamped(execContext.getTableMgr().
        isSystemTimestamped(tableId));
      if (qSrcq instanceof IEvictPolicyCallback)
      {
        IEvictPolicy curPolicy = cepMgr.getEvictPolicy();
        if (curPolicy != null && curPolicy.isUsingCallback())
        {
          curPolicy.addCallback((IEvictPolicyCallback) qSrcq);
        }
      }  
      return qSrc;
    }

    return null;
  }
  
  /**
   * Get a QueryOutput for the specified query
   * 
   * @param queryId  Query id
   * @param epr  destination epr string
   * @return QueryOutput
   * @throws CEPException
   */
  public QueryOutput getQueryOutput(int queryId, String epr)
      throws CEPException
  {
    return getQueryOutput(queryId, epr, false);
  }

  /**
   * Get a QueryOutput for the specified query
   * 
   * @param queryId  Query id
   * @param epr  destination epr string
   * @param isBatchOutput flag to check if batching required
   * @return QueryOutput
   * @throws CEPException
   */
  public QueryOutput getQueryOutput(int queryId, String epr, boolean isBatchOutput)
      throws CEPException
  {
    return getQueryOutput(queryId, epr, isBatchOutput, false);
  }
  
  /**
   * Get a QueryOutput for the specified query
   * 
   * @param queryId  Query id
   * @param epr  destination epr string
   * @param isBatchOutput flag to check if batching required
   * @param propagateHb flag to check if hb propagation is required
   * @return QueryOutput
   * @throws CEPException
   */
  public QueryOutput getQueryOutput(int queryId, String epr,
                                    boolean isBatchOutput,
				    boolean propagateHb)
    throws CEPException
  {
    QueryOutput q = null;
    InterfaceDriverContext ctx;
    InterfaceDriver df;

    ctx = createDriverContext(queryId, epr);
    if (ctx != null)
    {
      ctx.setBatchOutput(isBatchOutput);
      ctx.setPropagateHeartbeat(propagateHb);
      df = ctx.getDriver();
      q = df.subscribe_output(ctx);
      return q;
    }
    return null;
  }
 
  /**
   * Remove a QueryOutput for the specified query
   * 
   * @param queryId
   *          Query id
   * @param epr
   *          EndPointReference of the destination
   * @throws CEPException
   */
  public void removeQueryOutput(int queryId, String epr)
      throws CEPException
  {
    InterfaceDriverContext ctx;
    InterfaceDriver df;

    ctx = createDriverContext(queryId, epr);
    if (ctx != null)
    {
      df = ctx.getDriver();
      df.unsubscribe_output(ctx);
      return;
    }
  }
}
