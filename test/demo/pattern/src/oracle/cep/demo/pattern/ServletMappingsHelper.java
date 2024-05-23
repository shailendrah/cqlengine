/* $Header: ServletMappingsHelper.java 13-may-2008.10:37:39 hopark Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      04/13/08 - Creation
 */

/**
 *  @version $Header: ServletMappingsHelper.java 13-may-2008.10:37:39 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.demo.pattern;

import javax.servlet.Servlet;

import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;


public class ServletMappingsHelper
{
  public ServletMappingsHelper(ServletHandler handler, 
      Servlet servlet, String className, String displayName, String pathSpec)
  {
    ServletHolder holder = new ServletHolder();
    holder.setName(displayName);
    holder.setClassName(className);
    holder.setDisplayName(displayName);
    holder.setForcedPath(pathSpec);
    holder.setServlet(servlet);
    handler.addServlet(holder);
    ServletMapping mapping = new ServletMapping();
    mapping.setServletName(displayName);
    mapping.setPathSpec(pathSpec);
    handler.addServletMapping(mapping);
  }
}
