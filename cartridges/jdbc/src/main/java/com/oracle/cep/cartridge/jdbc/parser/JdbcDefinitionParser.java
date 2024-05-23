/* $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/parser/JdbcDefinitionParser.java /main/2 2010/05/05 05:35:07 udeshmuk Exp $ */

/* Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/27/10 - move config from epn to app config
    udeshmuk    01/06/10 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/cartridges/jdbc/src/main/java/com/oracle/cep/cartridge/jdbc/parser/JdbcDefinitionParser.java /main/2 2010/05/05 05:35:07 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package com.oracle.cep.cartridge.jdbc.parser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import com.oracle.cep.cartridge.jdbc.JdbcCartridgeContext;

/**
 * Parses the jdbc-context declaration appearing in EPN
 * @author udeshmuk
 */

public class JdbcDefinitionParser extends AbstractSingleBeanDefinitionParser
{ 
  private static final String JDBC_SPEC_ID_TAG = "id";
      
  protected Class getBeanClass()
  {
    return JdbcCartridgeContext.class;
  }
  
  @Override
  protected void doParse(Element element, BeanDefinitionBuilder builder)
  {
    /* This method gets called when jdbc-context tag is encountered. */
  
    //Set the JdbcCartridgeContext as the BeanClass. 
    //Object of this class will be created by BeanFactory when BeanDefinition
    //constructed here is processed.
    builder.getBeanDefinition().setBeanClassName(
      JdbcCartridgeContext.class.getName());
    
    String contextName = element.getAttribute(JDBC_SPEC_ID_TAG);
    builder.addPropertyValue("contextName", contextName);
  }
}
