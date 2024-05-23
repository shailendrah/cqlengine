/* $Header: CEPExplainPlanNode.java 18-apr-2008.04:37:18 rkomurav Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    rkomurav    04/18/08 - Creation
 */

/**
 *  @version $Header: CEPExplainPlanNode.java 18-apr-2008.04:37:18 rkomurav Exp $
 *  @author  rkomurav
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc.parser;

import java.sql.SQLException;

import oracle.cep.jdbc.CEPPreparedStatement;

public class CEPExplainPlanNode implements CEPParseTreeNode
{
  public CEPExplainPlanNode()
  {
  }
  
  public void prepareStatement(CEPPreparedStatement ps) throws SQLException
  {
  }
}
