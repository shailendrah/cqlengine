/* $Header: pcbpel/cep/server/src/oracle/cep/interfaces/DBDriverContext.java /main/4 2009/02/17 17:42:52 hopark Exp $ */

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
    hopark      06/26/08 - use datasource
    sbishnoi    03/10/08 - Creation
 */

package oracle.cep.interfaces;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import oracle.cep.service.ExecContext;
import oracle.xml.parser.v2.XMLElement;

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/interfaces/DBDriverContext.java /main/4 2009/02/17 17:42:52 hopark Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class DBDriverContext extends InterfaceDriverContext {
  
  /** DataSource name used to connect to Database*/
  private String            dataSourceName;
  
  /** Table Name where DBDestination will insert Tuple*/
  private String            tableName;
  
  private static final String DATASOURCE_STRING_TAG = "DataSource";
  private static final String TABLE_TAG= "Table";
  

  /**
   * Constructor Sets required field to create DBDestination
   * @param dbInfo is XMLElement Object which keeps DB Connection 
   * information in XML Format
   * 
   */
  public DBDriverContext(ExecContext ec, Object dbInfo)
  {
    super(ec, InterfaceType.DB);
    assert dbInfo instanceof XMLElement : dbInfo;
    XMLElement argRootElem = (XMLElement)dbInfo;
    
    NodeList childArgList     = null;
    
    childArgList = argRootElem.getChildrenByTagName(DATASOURCE_STRING_TAG);
    this.dataSourceName = parseXMLNode(childArgList);
    
    childArgList = argRootElem.getChildrenByTagName(TABLE_TAG);
    this.tableName = parseXMLNode(childArgList);
  }
  
  public String parseXMLNode(NodeList childArgList)
  {
    Node     childArgElem     = null;
    NodeList childValElemList = null;
    Node     childValElem     = null;
    
    int numArgs  = childArgList.getLength();
    assert numArgs == 1 : numArgs;
    
    childArgElem       = childArgList.item(0);
    childValElemList   = childArgElem.getChildNodes();
    childValElem       = childValElemList.item(0);
    return childValElem.getNodeValue();
  }
  
  /**
   * Get Data Source Name
   * @return dataSourceName used to connect to Database
   */
  public String getDataSourceName()
  {
    return this.dataSourceName;
  }
  
  /**
   * Get Destination TableName
   * @return tableName where DBDestination will insert
   */
  public String getTableName()
  {
    return this.tableName;
  }
  
}