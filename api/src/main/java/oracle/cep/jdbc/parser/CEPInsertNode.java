/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/jdbc/parser/CEPInsertNode.java /main/17 2012/01/20 11:47:14 sbishnoi Exp $ */

/* Copyright (c) 2007, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    01/12/12 - changed CEPDateFormat APIs
 anasrini    03/23/09 - insert optimization
 hopark      11/28/08 - use CEPDateFormat
 hopark      11/03/08 - fix schema
 hopark      10/23/08 - use serviceName for schema
 hopark      10/13/08 - support schema
 hopark      10/13/08 - support schema
 parujain    09/23/08 - multiple schema
 skmishra    08/21/08 - packages, imports
 skmishra    08/21/08 - imports, reorg
 mthatte     04/22/08 - adding getTime(String)
 mthatte     03/17/08 - jdbc reorg
 udeshmuk    01/17/08 - change in the data type of time field of TupleValue.
 mthatte     10/04/07 - Adding execute()
 mthatte     10/02/07 - Extending from CEPParseTreeNode
 mthatte     09/04/07 - Adding support for inserting heartbeats on
 client-side
 sbishnoi    08/14/07 - adding new constructor to include column list
 sbishnoi    06/19/07 - adding new constructor
 parujain    05/09/07 - 
 najain      04/27/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/jdbc/parser/CEPInsertNode.java /main/16 2009/03/31 02:50:09 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc.parser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import oracle.cep.common.CEPDate;
import oracle.cep.common.CEPDateFormat;
import oracle.cep.common.Constants;
import oracle.cep.dataStructures.external.TupleKind;
import oracle.cep.jdbc.CEPBaseConnection;
import oracle.cep.jdbc.CEPDatabaseMetaData;
import oracle.cep.jdbc.CEPPreparedStatement;
import oracle.cep.jdbc.CEPStatement;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.parser.CEPBigintConstExprNode;
import oracle.cep.parser.CEPConstExprNode;
import oracle.cep.parser.CEPExprNode;
import oracle.cep.parser.CEPIntConstExprNode;
import oracle.cep.parser.CEPStringConstExprNode;

public class CEPInsertNode implements CEPParseTreeNode
{

  public String                       tableName;
  public int                          numValues;
  public LinkedList<CEPConstExprNode> attrList;
  public LinkedList<CEPExprNode>      columnList;
  private boolean                     isHeartbeat;
  private boolean                     isClientTS;
  private boolean                     isBind;          
  
  /** Query Type # 1: INSERT INTO S1 VALUES (?, ?, ?); */
  public CEPInsertNode(String tableName, int numValues)
  {
    this.tableName = tableName;
    this.numValues = numValues;
    this.isHeartbeat = false;
    this.isBind = true;
  }

  /** Query Type # 2: INSERT INTO S1 (c1,c2,c3) VALUES (? ,? ,?); */
  public CEPInsertNode(String tableName, int numValues,
      LinkedList<CEPExprNode> columnList)
  {
    this.tableName = tableName;
    this.numValues = numValues;
    this.columnList = columnList;
    this.isHeartbeat = false;
    this.isBind = true;
  }

  /** Query Type # 1: INSERT INTO S1 VALUES (const1, const2, const3); */
  public CEPInsertNode(String tableName, LinkedList<CEPConstExprNode> attrList)
  {
    this.tableName = tableName;
    this.numValues = attrList.size();
    this.attrList = attrList;
    this.isHeartbeat = false;
    this.isBind = false;
  }

  /** Query Type # 1: INSERT INTO S1 (c1,c2,c3) VALUES (const1, const2, const3); */
  public CEPInsertNode(String tableName, LinkedList<CEPConstExprNode> attrList,
      LinkedList<CEPExprNode> columnList)
  {
    this.tableName = tableName;
    this.numValues = attrList.size();
    this.attrList = attrList;
    this.columnList = columnList;
    this.isHeartbeat = false;
    this.isBind=false;
  }

  /** Insert heartbeat: INSERT INTO S HEARTBEAT AT timestampValue */
  public CEPInsertNode(String tableName, CEPConstExprNode time)
  {
    this.tableName = tableName;
    this.attrList = new LinkedList<CEPConstExprNode>();
    this.attrList.addFirst(time);
    this.numValues = attrList.size();
    this.isHeartbeat = true;
    this.isBind = false;
  }
  
  /** Insert heartbeat: INSERT INTO S HEARTBEAT AT ? */
  public CEPInsertNode(String tableName)
  {
    this.isHeartbeat = true;
    this.tableName = tableName;
    this.isBind = true;
  }

  public String getTableName()
  {
    return tableName;
  }

  public LinkedList<CEPConstExprNode> getAttrList()
  {
    return attrList;
  }

  public LinkedList<CEPExprNode> getColumnList()
  {
    return this.columnList;
  }

  public boolean isHeartbeat()
  {
    return isHeartbeat;
  }

  public boolean isClientTimeStamped() 
  {
    return isClientTS;
  }
  
  public void checkAndSetClientTS(CEPBaseConnection conn)
  {
    try{
      CEPDatabaseMetaData dbm = (CEPDatabaseMetaData)conn.getMetaData();
      this.isClientTS = dbm.isClientTimeStamped(this.tableName, conn.getSchemaName());
    }catch(Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static long getTime(String t) throws Exception
  {
    try
    {
      CEPDateFormat df = CEPDateFormat.getInstance();
      CEPDate d = df.parse(t);
      return d.getValue();
    }
    catch(ParseException e)
    {
      throw new Exception("Error parsing timestamp");
    }
  }
  
  public static long getTime(CEPConstExprNode n) throws Exception
  {
    if(n instanceof CEPIntConstExprNode) 
      return ((CEPIntConstExprNode)n).getValue();
    else if(n instanceof CEPBigintConstExprNode)
      return ((CEPBigintConstExprNode)n).getValue();
    else if(n instanceof CEPStringConstExprNode)
    {
      try
      {
        CEPDateFormat df = CEPDateFormat.getInstance();
        CEPDate d = df.parse(((CEPStringConstExprNode)n).getValue());
        return d.getValue();
      }
      catch(ParseException e) 
      {
        throw new Exception("Error parsing timestamp");
      }
    }
    else
      throw new Exception("Timestamp can only be int,long or string");
  }
  
  public void prepareStatement(CEPPreparedStatement ps) throws SQLException
  {
      CEPBaseConnection conn = (CEPBaseConnection) ps.getConnection();
      String schemaName = conn.getSchemaName();
      ps.setTableName(tableName);

      // Is this a statement with binds?
      if(isBind)
      {
        // Is this a heartbeat?
        if(isHeartbeat)
        {
          ps.getTuple().setKind(TupleKind.HEARTBEAT);
          ps.getTuple().setObjectName(this.tableName);
          ps.getTuple().setBHeartBeat(true);
        }
        else
        {
          ps.getTuple().setKind(TupleKind.PLUS);
          ps.getTuple().setObjectName(this.tableName);
          ps.getTuple().setBHeartBeat(false);
          
          // Query server for isClientTS
          checkAndSetClientTS(conn);
          if(!isClientTS)
            ps.getTuple().setTime(Constants.NULL_TIMESTAMP);
          // Get the schema from server and create an attrVal array
          CEPDatabaseMetaData dbm = (CEPDatabaseMetaData)conn.getMetaData();
          ResultSet rs = dbm.getColumns(null, schemaName, tableName, "%");
          List<String> columns = new ArrayList<String>();
          while(rs.next())
          {
            String colType  = rs.getString("TYPE_NAME");
            columns.add(colType); 
          }
          if(numValues!=columns.size()) {
            throw new SQLException("Schema mismatch. Re-write statement");
          }
          
          if(isClientTS) 
          {
            // remove the Timestamp attr from the columnList.
            // this list should only reflect the table schema 
            columns.remove(0);
          }
          String[] colArgs = new String[columns.size()];
          columns.toArray(colArgs);
          ps.createAttrVal(colArgs);
        }
      }
      
      // else, it has only constants
      else
      {
        // If its a heartbeat, set the tablename, timestamp and the tuple is
        // ready to go
        if(isHeartbeat)
        {
          try{
            CEPConstExprNode timeVal = attrList.removeFirst();
            ps.getTuple().setTime(getTime(timeVal));
            ps.getTuple().setBHeartBeat(true);
            ps.getTuple().setKind(TupleKind.HEARTBEAT);
            ps.getTuple().setObjectName(this.tableName);
          }catch(Exception pe){
            LogUtil.logStackTrace(LoggerType.TRACE, Level.SEVERE, pe);
          }
        }
        
        else
        {
          ps.getTuple().setKind(TupleKind.PLUS);
          ps.getTuple().setBHeartBeat(false);
          ps.getTuple().setObjectName(this.tableName);
          try{
            ((CEPStatement)ps).setAttributes(attrList);
          }catch(Exception e){
            throw new SQLException("Bad statement. Check and rewrite");
          }
        }
      }
    }
}
