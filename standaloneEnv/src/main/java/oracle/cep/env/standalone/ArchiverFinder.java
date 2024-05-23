/* $Header: cep/wlevs_cql/modules/cqlengine/standaloneEnv/src/oracle/cep/env/standalone/ArchiverFinder.java /main/1 2011/05/18 04:38:13 udeshmuk Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    04/26/11 - Creation
 */

package oracle.cep.env.standalone;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import oracle.cep.extensibility.datasource.IArchiver;
import oracle.cep.service.IArchiverFinder;

/**
 *  @version $Header: ArchiverFinder.java 26-apr-2011.06:18:33 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public class ArchiverFinder implements IArchiverFinder
{
  private Map<String, IArchiver> archiverMap;
  
  public ArchiverFinder()
  {
    archiverMap = new HashMap<String, IArchiver>();
  }

  @Override
  public void addArchiver(String name, IArchiver ds)
  {
    archiverMap.put(name, ds);
  }

  @Override
  public IArchiver findArchiver(String name)
  {
    return archiverMap.get(name);
  }

  @Override
  public void init()
  {
    archiverMap.clear();
  }

  @Override
  public void removeArchiver(String name)
  {
    archiverMap.remove(name);
  }
  
  private void setArchiver(Properties props)
  {
    String archiverName = null;
    String archiverClassName = null;
    Iterator<Object> i = props.keySet().iterator();
    while (i.hasNext())
    {
      String name = (String) i.next();
      String val = props.getProperty(name);
      if (name.equals("name")) 
      {
        archiverName = val;
      } 
      else if (name.equals("class")) 
      {
        archiverClassName = val;
      }       
    }
    
    // Assert that archiver context should contain both name and class values
    assert archiverName != null;
    assert archiverClassName != null;
        
    Class<?> archiverClass = instantiateClass(archiverClassName);
    IArchiver archiverInstance = null;
    
    try
    {
      archiverInstance = (IArchiver) archiverClass.newInstance();
    } 
    catch (InstantiationException e)
    {      
      e.printStackTrace();
    } 
    catch (IllegalAccessException e)
    {     
      e.printStackTrace();
    }
    System.out.println("***** Populating Archiver Map with entry: " + archiverName);
    addArchiver(archiverName, archiverInstance);
  }
  
  private Class<?> instantiateClass(String className)
  {
    try 
    {
      // Create the implementation class execution object for validation purpose
      Class<?> cf = Class.forName(className, true, 
          Thread.currentThread().getContextClassLoader());
      return cf;
    } 
    catch (ClassNotFoundException cnf) 
    {
      try
      {
        Class<?> cf = Class.forName(className);
        return cf;
      }
      catch(ClassNotFoundException cnf_inner)
      {          
        cnf_inner.printStackTrace();
        return null;
        //Eat the Exception    
      }
    }
  }
  
  public void setArchiver0(Properties props)
    throws Exception
  {
    setArchiver(props);
  }

  
  
}
