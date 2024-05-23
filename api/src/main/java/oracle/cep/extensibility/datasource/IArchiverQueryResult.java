/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/extensibility/datasource/IArchiverQueryResult.java /main/1 2011/05/18 04:38:12 udeshmuk Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/30/11 - Creation
 */

/**
 *  @version $Header: IArchiverQueryResult.java 30-apr-2011.17:21:26 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.extensibility.datasource;

import java.sql.ResultSet;

public interface IArchiverQueryResult
{
	int	getResultCount();
	ResultSet getResult(int idx);
}
