/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/JavaDriver.java /main/8 2010/11/19 07:47:47 udeshmuk Exp $ */

/* Copyright (c) 2007, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/23/10 - propagate hb
    sbishnoi    12/09/09 - batching events support
    hopark      01/28/09 - add usage of output dest id
    hopark      10/10/08 - remove statics
    hopark      10/09/08 - remove statics
    sbishnoi    04/01/08 - modifying class.forName to incorporate ClassLoader
    sbishnoi    03/11/08 - handled a case when <Argument> tag inside
                           <Arguments> tag is empty
    sbishnoi    02/18/08 - support of specifying java source or destination as
                           xml
    sbishnoi    01/21/08 - adding Java as a TableSource
    sbishnoi    12/11/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/interfaces/JavaDriver.java /main/8 2010/11/19 07:47:47 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.InterfaceError;
import oracle.cep.interfaces.input.TableSource;
import oracle.cep.interfaces.input.TableSourceBase;
import oracle.cep.interfaces.output.QueryOutput;
import oracle.cep.interfaces.output.QueryOutputBase;
import oracle.cep.metadata.ConfigManager;
import oracle.cep.service.ExecContext;
import oracle.cep.service.IQueryDestLocator;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;

public class JavaDriver extends InterfaceDriver
{
  JavaDriver(ExecContext ec)
  {
    super(ec, InterfaceType.JAVA);
  }
  
  @Override
  public InterfaceDriverContext CreateDriverContext(URI uri, 
    XMLDocument doc, int id) throws CEPException
  {
    return new JavaDriverContext(execContext, uri.getHost(), false);    
  }
  
  @Override
  public InterfaceDriverContext CreateDriverContext(InterfaceDriver.KeyValue[] vals,
    XMLDocument doc, int id) throws CEPException
  {
    return new JavaDriverContext(execContext, vals);
  }
  
  public QueryOutput subscribe_output(InterfaceDriverContext desc)
   throws CEPException
  {
    JavaDriverContext ctx = (JavaDriverContext)desc;
    Class<?> destClass;
    QueryOutput outClassInstance = null;
    String outputDest = ctx.getClassName();
    ExecContext ec = ctx.getExecContext();
    try
    {
      if (ctx.hasDestinationId())
      {
        String destId = ctx.getDestinationId();
	boolean isBatchOutput = ctx.isBatchOutput();
	boolean propagateHb = ctx.isPropagateHeartbeat();
        ConfigManager cm = ec.getServiceManager().getConfigMgr();
        IQueryDestLocator objReg = cm.getQueryDestLocator();
        if (objReg == null)
        {
          throw new CEPException(InterfaceError.OUTPUT_DEST_LOCATOR_NOT_FOUND);
        }
        outClassInstance = objReg.find(destId, isBatchOutput, propagateHb);
        if (outClassInstance == null)
        {
          throw new CEPException(InterfaceError.OUTPUT_CLASS_NOT_VALID, 
              new Object[]{destId});
        }
      }
      else
      {
        destClass        = Class.forName(outputDest, true, 
                             Thread.currentThread().getContextClassLoader());
        Class<?>[] ptypes = new Class[1];
        ptypes[0] = ExecContext.class;
        Constructor constructor = destClass.getConstructor(ptypes);
        Object[] args = new Object[1];
        args[0] =  ec;
        outClassInstance = (QueryOutput)constructor.newInstance(args);
      }
      if(ctx.hasArguments())
      {
        assert ctx.getArguments() instanceof XMLElement :ctx.getArguments();
        XMLElement argRootElem = (XMLElement) ctx.getArguments();
        
        NodeList childArgList = argRootElem.getChildrenByTagName("Argument");
        int numArgs  = childArgList.getLength();
        
        Node     childArgElem     = null;
        NodeList childValElemList = null;
        Node     childValElem     = null;
        String[] eprArgs          = new String[numArgs];
        
        for(int i = 0; i < numArgs; i++)
        {
          childArgElem = childArgList.item(i);
          childValElemList = childArgElem.getChildNodes();
          childValElem = childValElemList.item(0);
          if(childValElem == null)
            throw new CEPException(InterfaceError.INVALID_EPR_QUERY, 
                                   "<Argument></Argument>");
          eprArgs[i] = childValElem.getNodeValue();
        }
        outClassInstance.setEprArgs(eprArgs);
      }
      
    }
    catch(ClassNotFoundException e) {
      throw new CEPException(InterfaceError.CLASS_NOT_FOUND, 
        new Object[]{outputDest});
    }
    catch(InstantiationException e){
      throw new CEPException(InterfaceError.CLASS_NOT_INSTANTIATED, 
        new Object[]{outputDest});
    }
    catch(InvocationTargetException e){
      throw new CEPException(InterfaceError.CLASS_NOT_INSTANTIATED, 
        new Object[]{outputDest});
    }
    catch(IllegalAccessException e){
      throw new CEPException(InterfaceError.CLASS_NOT_ACCESSIBLE,
          new Object[]{outputDest});
    }
    catch(NoSuchMethodException e)
    {
      throw new CEPException(InterfaceError.OUTPUT_CLASS_NOT_VALID, 
          new Object[]{outputDest});
    }
    catch(ClassCastException e) {
      throw new CEPException(InterfaceError.OUTPUT_CLASS_NOT_VALID, 
          new Object[]{outputDest});
    }
    
    return outClassInstance;
  }

  
  public void unsubscribe_output(InterfaceDriverContext desc) {
    
  }
  
  public TableSource subscribe_source(InterfaceDriverContext desc) {
    JavaDriverContext ctx = (JavaDriverContext)desc;
    Class<?> destClass;
    TableSourceBase outClassInstance = null;
    
    try{
      destClass = Class.forName(ctx.getClassName(), true, 
                    Thread.currentThread().getContextClassLoader());
      outClassInstance = (TableSourceBase)destClass.newInstance();
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
    return outClassInstance;
  }
  
  public void unsubscribe_source(InterfaceDriverContext desc) {
  }
  
}
