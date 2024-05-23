/* $Header: CEPParseTreeNode.java 19-mar-2008.14:04:21 mthatte Exp $ */

/* Copyright (c) 2007, 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     03/19/08 - adding SQLException
    mthatte     10/04/07 - Adding execute()
    mthatte     10/02/07 - Base interface for dummy select and insert
    mthatte     10/02/07 - Creation
 */

/**
 *  @version $Header: CEPParseTreeNode.java 19-mar-2008.14:04:21 mthatte Exp $
 *  @author  mthatte 
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.jdbc.parser;

import java.sql.SQLException;

import oracle.cep.jdbc.CEPPreparedStatement;

public interface CEPParseTreeNode {
	void prepareStatement(CEPPreparedStatement ps) throws SQLException;
}