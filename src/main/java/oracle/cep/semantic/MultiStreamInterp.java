/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/MultiStreamInterp.java /main/3 2013/08/05 03:00:57 pkali Exp $ */

/* Copyright (c) 2012, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    pkali       07/30/13 - added ELEMENT_TIME attribute in the sub query
                           construction
    pkali       08/15/12 - included DynamicType info in query meta
    pkali       06/26/12 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/semantic/MultiStreamInterp.java /main/3 2013/08/05 03:00:57 pkali Exp $
 *  @author  pkali   
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.semantic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import oracle.cep.common.Datatype;
import oracle.cep.common.StreamPseudoColumn;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.SemanticError;
import oracle.cep.extensibility.cartridge.ICartridge;
import oracle.cep.extensibility.cartridge.internal.dynamictype.DynamicDataType;
import oracle.cep.extensibility.cartridge.internal.dynamictype.DynamicTypeCartridge;
import oracle.cep.extensibility.cartridge.internal.dynamictype.DynamicTypeLocator;
import oracle.cep.extensibility.type.IType;
import oracle.cep.extensibility.type.ITypeLocator;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.Query;
import oracle.cep.metadata.QueryManager;
import oracle.cep.parser.CEPBaseStreamNode;
import oracle.cep.parser.CEPMultiStreamNode;
import oracle.cep.parser.CEPParseTreeNode;
import oracle.cep.parser.CEPPatternStreamNode;
import oracle.cep.parser.CEPQueryNode;
import oracle.cep.parser.CEPSetopSubqueryNode;
import oracle.cep.parser.CEPStreamNode;
import oracle.cep.parser.CEPStreamSubqueryNode;
import oracle.cep.parser.CEPStringTokenNode;
import oracle.cep.service.ExecContext;
import oracle.cep.service.ICartridgeLocator;

/**
 * The interpreter that is specific to the CEPMultiStreamNode parse tree 
 * node.
 */

class MultiStreamInterp extends NodeInterpreter 
{
  void interpretNode(CEPParseTreeNode node, SemContext ctx) 
  throws CEPException 
  {
    //In MultiStreamNode case, the common attributes among the participating
    //sources are identified and for each source, a query string is constructed
    //by selecting only the common attributes. These individual queries are
    //concatenated with a UNION ALL operator and wrapped as a SubQueryNode
    
    assert node instanceof CEPMultiStreamNode;
    CEPMultiStreamNode streamNode = (CEPMultiStreamNode)node;
    CEPStreamNode[] streams = streamNode.getStreamNodes();

    ExecContext ec = ctx.getExecContext();

    super.interpretNode(node, ctx);
    
    SemContext newctx = new SemContext(ec);
    newctx.setQueryObj(ctx.getQueryObj());
    newctx.setSchema(ctx.getSchema());
    newctx.setSemQuery(ctx.getSemQuery());
    SymbolTable newsymtab = new SymbolTable();
    newctx.setSymbolTable(newsymtab);
    
    Query queryObj = ctx.getQueryObj();
    assert queryObj != null;
    DynamicTypeLocator dynTypeLocator = null;
    try
    {
      ICartridgeLocator cartridgeLocator = ec.getServiceManager()
          .getConfigMgr().getCartridgeLocator();
      ICartridge cartridge = cartridgeLocator
                       .getInternalCartridge(DynamicTypeCartridge.CARTRIDGE_ID);
      
      assert cartridge instanceof DynamicTypeCartridge;
      
      ITypeLocator typeLocator = ((DynamicTypeCartridge)cartridge)
                                                     .getTypeLocator();
      dynTypeLocator = (DynamicTypeLocator)typeLocator; 
    }
    catch(Exception e)
    {
      LogUtil.fine(LoggerType.TRACE, "Cartridge error : Unable to create " +
      		                        "the dynamic type");
      throw new SemanticException(SemanticError.SEMANTIC_ERROR,
                  streamNode.getStartOffset(), streamNode.getEndOffset());
    }
    
    Set<AttrElement> attrElementSet = new HashSet<AttrElement>();
    Map<String,String> streamTypeMap = new HashMap<String,String>();
    Map<String,String> streamAttrListMap = new HashMap<String,String>();
    queryObj.setDynamicTypeSystem(dynTypeLocator);
    boolean isElementTimeExists = true;
    
    //collect the attributes from the participating sources and
    //find the common attributes
    for(int i = 0; i < streams.length; i++)
    {
      CEPStreamNode stream = streams[i]; 
      NodeInterpreter nodeInterpreter = 
          InterpreterFactory.getInterpreter(stream);
      nodeInterpreter.interpretNode(stream, newctx);
      
      int varId = -1;
      boolean containsElementTime = false;
      if(stream instanceof CEPBaseStreamNode)
      {
        BaseStreamSpec bstrmSpec = newctx.getBaseStreamSpec();
        if(bstrmSpec != null)
          varId = bstrmSpec.getVarId();
        containsElementTime = true;
      }
      else if(stream instanceof CEPStreamSubqueryNode)
      {
        SubquerySpec subquerySpec = newctx.getSubquerySpec();
        if(subquerySpec != null)
          varId = subquerySpec.getVarid();
      }
      else
      {
        throw new SemanticException(SemanticError.INVALID_SOURCE_INPUT,
            stream.getStartOffset(), stream.getEndOffset(),
                new Object[]{stream.getName()});
      }

      if(varId > -1)
      {
        //create dynamic type for each stream
        IType streamType = dynTypeLocator.createType();
        String streamName = null;
        if(stream.getAlias() != null)
          streamName = new String(stream.getAlias());
        else if(stream.getName() != null)
          streamName = new String(stream.getName());
        else
        {
          throw new SemanticException(SemanticError.INVALID_SOURCE_INPUT,
              stream.getStartOffset(), stream.getEndOffset());
        }
        
        queryObj.addDynamicTypeName(streamType.name());
        streamTypeMap.put(streamName, new String(streamType.name()));
        
        DynamicDataType dynStremType = (DynamicDataType)streamType; 
        
        SymbolTableAttrEntry[] attrs = 
            newctx.getSymbolTable().getAllAttrs(ec, varId);
        Set<AttrElement> tempSet = new HashSet<AttrElement>();
        StringBuilder attrList = new StringBuilder();
        
        for(int j = 0; j < attrs.length; j++)
        {
          AttrElement attrElement = 
              new AttrElement(attrs[j].getVarName(), attrs[j].getAttrType());
          tempSet.add(attrElement);
          dynStremType.addField(attrs[j].getVarName(), attrs[j].getAttrType());
          attrList.append(attrElement.name);
          if(j < attrs.length - 1)
            attrList.append(", ");
          
          if(stream instanceof CEPStreamSubqueryNode &&
              attrs[j].getVarName().toLowerCase()
              .equals(StreamPseudoColumn.ELEMENT_TIME.getColumnName()))
            containsElementTime = true;
          
        }
        streamAttrListMap.put(new String(streamName), 
                                    attrList.toString());
        
        //computing the common attributes
        if( i > 0 && attrElementSet.size() > 0)
          attrElementSet = computeIntersection(attrElementSet, tempSet);
        else if(i == 0)
          attrElementSet = tempSet;
      }
      if(!containsElementTime)
        isElementTimeExists = false;
    }

    StringBuilder commonAttrList = new StringBuilder();
    int c = 0;
    int size = attrElementSet.size();
    if(size > 0)
    {
      for(AttrElement attrElement : attrElementSet)
      {
        commonAttrList.append(attrElement.name);
        if(++c < size)
          commonAttrList.append(", ");
      }
    }
    
    StringBuilder query = new StringBuilder();
    for(int i = 0; i < streams.length; i++)
    {
      String q = removeHTMLChars( getQueryString(streams[i], 
                       streams, streamTypeMap, streamAttrListMap,
                       commonAttrList.toString(), isElementTimeExists));
      if(q.isEmpty())
      {
        LogUtil.fine(LoggerType.TRACE, "Unable to construct the query string "
                             + streams[i].toString());
        throw new SemanticException(SemanticError.SEMANTIC_ERROR,
            streamNode.getStartOffset(), streamNode.getEndOffset(),
                new Object[]{streamNode.getName()});
      }
      query.append(q);
      if(i < streams.length - 1)
        query.append(" union all ");
    }
    
    LogUtil.fine(LoggerType.TRACE, "MultiStream UINON ALL query : " + query);
    
    //Parse the generated query and interpret the parser node.
    //The generated query is a 'union all' of all the source streams
    //which will result as a SetopSubqueryNode, this is in turn wrapped as
    //Subquery node and passed to PatternStreamNode 
    QueryManager qryMgr = ec.getQueryMgr();
    CEPParseTreeNode parserNode = qryMgr.getParser()
                                  .parseCommand(ec, query.toString());
    
    CEPSetopSubqueryNode setopNode = (CEPSetopSubqueryNode) parserNode;
    CEPStreamSubqueryNode subQueryNode = new CEPStreamSubqueryNode(
        (CEPQueryNode) setopNode,new CEPStringTokenNode("_SQ_ALIAS0_"));
    
    CEPPatternStreamNode patternNode = new CEPPatternStreamNode(
        subQueryNode, streamNode.getPatternDesc(), streamNode.getAliasToken());
    
    streamNode.setQueryString("(" + query.toString() + ")" 
                        + " AS" + removeHTMLChars(patternNode.toString()));
    NodeInterpreter nodeInterpreter = 
        InterpreterFactory.getInterpreter(patternNode);
    nodeInterpreter.interpretNode(patternNode, ctx);
  }
  
  private String getQueryString(CEPStreamNode streamNode, 
                  CEPStreamNode[] streams, Map<String,String> streamTypeMap, 
                  Map<String,String> streamAttrListMap, String attrList, boolean isElementTimeExists)
  {
    String streamName = null;
    if(streamNode.getAlias() != null)
      streamName = streamNode.getAlias();
    else if(streamNode.getName() != null)
      streamName = streamNode.getName();
    
    String selectClause = getSelectClauseString(streamName,
        streams, streamTypeMap, streamAttrListMap);
    
    if(selectClause.length() == 0)
      return "";
    
    final String STREAM_NAME = "$streamName";
    String streamNameAttr = "\"" + streamName 
                                 + "\" as " + STREAM_NAME + ", ";
    if(attrList.length() > 0)
      attrList = attrList + ",";

    if(isElementTimeExists)
      attrList = attrList + " ELEMENT_TIME, ";
    
    if(streamNode instanceof CEPBaseStreamNode)
    {
      CEPBaseStreamNode node = (CEPBaseStreamNode) streamNode;

      return "select " + streamNameAttr + attrList 
                     + selectClause + " from " + node.getName();
    }
    else if(streamNode instanceof CEPStreamSubqueryNode)
    {
      CEPStreamSubqueryNode node = (CEPStreamSubqueryNode) streamNode;
      return "select " + streamNameAttr + attrList + selectClause + " from (" 
               + node.getQuery().toString() + ") as " + node.getAlias();
    }
    //else ; only Base stream and Subquery node is possible 
    //       - validated by the caller 
    return "";
  }
  
  private String getSelectClauseString(String streamName, 
      CEPStreamNode[] streams, Map<String,String> streamTypeMap, 
      Map<String,String> streamAttrListMap)
  {
    final String DYNAMIC_TYPE = "DynamicType";
    
    StringBuilder select = new StringBuilder();
    for(int i = 0; i < streams.length; i++)
    {
      String strmName = null;
      if(streams[i].getAlias() != null)
        strmName = streams[i].getAlias();
      else if(streams[i].getName() != null)
        strmName = streams[i].getName();
      String typeName = streamTypeMap.get(strmName);
      String attrList = streamAttrListMap.get(strmName);
      
      //empty str will be handled & propagated as exception
      if(typeName == null || attrList == null)
        return "";  
      
      if(streamName.equals(strmName))
      {
        select.append(typeName + "@" + DYNAMIC_TYPE 
            + "(" + attrList + ") as " + strmName);
      }
      else
      {
        //null will not match the data type in union all operation
        //so an empty constructor of same type is added
        select.append(typeName + "@" + DYNAMIC_TYPE 
            + "( ) as " + strmName);
      }
      if(i < streams.length - 1)
        select.append(", ");
    }
    return select.toString();
  }
  
  //XMLHelper.toHTMLString replace > & < chars
  //so only two chars need to be removed as of now
  private String removeHTMLChars(String arg)
  {
    String retString;
    retString = arg.replaceAll("&gt;", ">");
    retString = retString.replaceAll("&lt;", "<");
    return retString;
  }
  
  private Set<AttrElement> computeIntersection(Set<AttrElement> setA, 
                                                      Set<AttrElement> setB)
  {
    Set<AttrElement> setI = new HashSet<AttrElement>();
    for(AttrElement ae : setA)
    {
      if(setB.contains(ae))
        setI.add(ae);
    }
    return setI;
  }
  
  private class AttrElement
  {
    String name;
    Datatype dataType;
    
    public AttrElement(String name, Datatype dataType)
    {
      this.name = name;
      this.dataType = dataType;
    }
    
    public boolean equals(Object obj)
    {
      if(obj instanceof AttrElement)
      {
        AttrElement attrElement = (AttrElement) obj;
        return this.name.equals(attrElement.name)
            && this.dataType.equals(attrElement.dataType);
      }
      return false;
    }
    
    public int hashCode()
    {
      int hashCode = 0;
      if(name != null)
        hashCode += name.hashCode();
      if(dataType != null)
        hashCode += dataType.hashCode();
      return hashCode;
    }
  }
}
