/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/StaleTableSourceException.java /main/1 2010/02/08 21:26:46 sbishnoi Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    02/02/10 - Creation
 */

package oracle.cep.exceptions;

import java.sql.SQLException;

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/exceptions/StaleTableSourceException.java /main/1 2010/02/08 21:26:46 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class StaleTableSourceException extends SQLException
{
  public StaleTableSourceException(String reason, Throwable cause)
  {
    super(reason, cause);
  }
}
