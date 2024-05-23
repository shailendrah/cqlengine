/* $Header: IXmlContext.java 12-may-2008.16:00:25 parujain Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    05/12/08 - XML Context
    parujain    05/12/08 - Creation
 */

/**
 *  @version $Header: IXmlContext.java 12-may-2008.16:00:25 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.xml;

import oracle.xml.xqxp.datamodel.OXMLItem;

public interface IXmlContext 
{
  XMLItem createItem();
  OXMLItem createOXMLItem();
  long	getId();
}
