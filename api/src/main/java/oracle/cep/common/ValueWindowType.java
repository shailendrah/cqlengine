/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/ValueWindowType.java /main/1 2011/10/01 09:28:39 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/06/11 - Creation
 */
package oracle.cep.common;

/**
 *  @version $Header: ValueWindowType.java 06-sep-2011.04:45:38 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
public enum ValueWindowType
{
  // RANGE const_val ON column  OR RANGE time_val ON column
  GENERIC,
  
  // CurrentHour ON column
  CURRENT_HOUR , 
                
  // CurrentPeriod (m ,n) ON column
  CURRENT_PERIOD  ;   
  
}
