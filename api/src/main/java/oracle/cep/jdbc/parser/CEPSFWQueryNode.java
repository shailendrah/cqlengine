/* $Header: pcbpel/cep/common/src/oracle/cep/jdbc/parser/CEPSFWQueryNode.java /main/7 2009/02/19 11:21:29 skmishra Exp $ */

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
    skmishra    01/27/09 - adding toQCXML()
    hopark      11/03/08 - fix schema
    hopark      10/23/08 - user serviceName for schema
    hopark      10/23/08 - use servicename for schema
    skmishra    10/14/08 - schema support
    skmishra    08/21/08 - packages, imports
    skmishra    08/21/08 - imports
    mthatte     12/06/07 - removing System.out.println()
    mthatte     10/04/07 - Adding execute()
    mthatte     10/02/07 - For dummy selects in JDBC client
    mthatte     10/02/07 - Creation
 */
package oracle.cep.jdbc.parser;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.SelectResultMetadataDescriptor;
import oracle.cep.jdbc.CEPBaseConnection;
import oracle.cep.jdbc.CEPPreparedStatement;
import oracle.cep.jdbc.CEPResultSet;
import oracle.cep.parser.CEPQueryRelationNode;

/**
 * Parse tree node for a select-from-where query, a query without set
 * operations
 */

public class CEPSFWQueryNode extends CEPQueryRelationNode implements CEPParseTreeNode {

  /** The project terms from select clause */
  protected List<String> projList;

  /** The relation from clause */
  protected String relName;  //We only allow one relation for now.

  /**
   * Constructor
   * @param selectClause the select Clause
   * @param fromClause the from Clause
   */
  CEPSFWQueryNode(List<String> selectClause, 
                  String fromReln) {
    this.projList  = selectClause;
    relName = fromReln;
  }

  // getter methods

  /**
   * Get the from clause relations
   * @return the from clause relations
   */
  public String getFromClauseRelations() {
    return relName;
  }
  

  /**
   * Get the select list
   * @return the select list
   */
  public List<String> getProjectList() {
    return projList;
  }

  //Called from PrepStmt with the PS object passing itself as arg.
  //Creates a MetadataDescriptor based on the Project list
  public void prepareStatement(CEPPreparedStatement ps) {
	  	try{
	  	 //Cast to a select node
		  CEPSFWQueryNode selectNode = this;
		  
		  //Find project columns i.e. SELECT c1,c2...
		  List<String> cols = selectNode.getProjectList();
		  
		  //Find the table name i.e. SELECT c1,c2 FROM T.... 
		  String reln = selectNode.getFromClauseRelations();
		  
		  ResultSet rs = null;
		  
		  CEPBaseConnection conn = (CEPBaseConnection) ps.getConnection();
		  //Get the columns of this table
		  DatabaseMetaData dbm = conn.getMetaData();
		  
		  rs = dbm.getColumns(null,conn.getSchemaName(),reln,null); 
		  			  
		  if(rs!=null) {
			  SelectResultMetadataDescriptor smd = new SelectResultMetadataDescriptor();
			  while(rs.next()) {
				  String colName = rs.getString("COLUMN_NAME");
				  for(String col: cols) {
					  if(colName.equals(col)) {
						  String tableName = rs.getString("TABLE_NAME");
						  int type = rs.getInt("DATA_TYPE");
						  int attrLen = rs.getInt("COLUMN_SIZE");
						  smd.addColumn(tableName,colName,type, attrLen);
					  }
				  }
			}
			  long id = -1;
			  MetadataDescriptor md = smd;
			  
			  ps.setResultSet(new CEPResultSet(id,(CEPBaseConnection)ps.getConnection(),md));
		  }
		  
	  	}
	  	
	  	catch(Exception e) {
	  		
	  	}
	   
	  		return;
  }

  public int toQCXML(StringBuffer queryXml, int operatorID)
      throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException("Not supported");
  }

  /**
   * Returns true if SFW Query represented by this node contains
   * any logical CQL syntax.
   */
  @Override
  public boolean isLogical()
  {
    throw new UnsupportedOperationException("isLogical() Operation Not Supported");
  }
}
