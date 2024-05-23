/* $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/ServiceNameHelper.java /main/3 2011/11/23 09:58:43 alealves Exp $ */

/* Copyright (c) 2009, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
      alealves  11/27/09 - Data cartridge context, default package support
    alealves    Nov 25, 2009 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/common/src/oracle/cep/common/ServiceNameHelper.java /main/2 2009/12/21 10:05:02 alealves Exp $
 *  @author  alealves
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.common;


public class ServiceNameHelper
{
  /**
   * FIXME Unfortunately service names must be an identifier, as it is used in the create/drop schema DDL,
   *  hence we need to encrypt it somewhat....
   * 
   * symbolic-name := (alphanum|-|_|.)
   * identifier := (alphanum|_)
   */
  private static final String DELIM = "_$ocep$_";

  public static String buildServiceName(String applicationName, String stageName) 
  {
    StringBuilder builder = new StringBuilder();
    builder.append(applicationName);
    builder.append(DELIM);
    builder.append(stageName);
    builder.append(DELIM);
    
    return builder.toString();
  }
  
  public static String getApplicationName(String serviceName)
  {
    String appName = "sys"; // if no application name, then assume it is the sys service.
    
    int index = serviceName.indexOf(DELIM);
    
    if (index != -1)
      appName = serviceName.substring(0, index);
    
    return appName;
  }
  
  public static String [] getAppAndStageNames(String serviceName)
  {
    String [] names = new String[] { "sys", "unknown" };
    
    int index = serviceName.indexOf(DELIM);
    
    if (index != -1)
      names[0] = serviceName.substring(0, index);
    
    int index2 = serviceName.indexOf(DELIM, index + DELIM.length());
    
    if (index2 != -1)
      names[1] = serviceName.substring(index + DELIM.length(), index2);
    
    return names;
  }
  
}
