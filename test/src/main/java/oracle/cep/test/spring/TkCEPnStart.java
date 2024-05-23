package oracle.cep.test.spring;
/* $Header: pcbpel/cep/test/src/TkCEPnStart.java /main/1 2008/10/24 15:50:24 hopark Exp $ */

/* Copyright (c) 2008, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/23/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/test/src/TkCEPnStart.java /main/1 2008/10/24 15:50:24 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.List;

import oracle.cep.common.Constants;
import oracle.cep.common.Datatype;
import oracle.cep.dataStructures.external.TupleValue;
import oracle.cep.descriptors.ArrayContext;
import oracle.cep.descriptors.ColumnMetadataDescriptor;
import oracle.cep.descriptors.MetadataDescriptor;
import oracle.cep.descriptors.TableMetadataDescriptor;
import oracle.cep.exceptions.CEPException;
import oracle.cep.exceptions.CustomerLogMsg;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.metadata.MetadataException;
import oracle.cep.metadata.Table;
import oracle.cep.metadata.TableManager;
import oracle.cep.metadata.View;
import oracle.cep.metadata.ViewManager;
import oracle.cep.metadata.cache.CacheObject;
import oracle.cep.service.CEPManager;
import oracle.cep.service.CEPServerXface;
import oracle.cep.service.ExecContext;
import oracle.cep.storage.IStorageContext;
import oracle.cep.storage.StorageException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class TkCEPnStart
{
  public static void createContext(String ctx)
  {
    
  }
  
  public static void main(String args[])
  {
    try
    {
      //springframework uses log4j and prints some configuration related exceptions by default.
      //In order to suppress messages, set some logger configuration 
      System.setProperty("log4j.defaultInitOverride", "true");
      org.apache.log4j.BasicConfigurator.configure();
      org.apache.log4j.Logger rootLogger = org.apache.log4j.LogManager.getRootLogger();
      rootLogger.setLevel(org.apache.log4j.Level.WARN);

      String ctxFile = args[0];
      int iter = Integer.parseInt(args[1]);
      int delay = Integer.parseInt(args[2]);
      for (int i = 0; i < iter; i++)
      {
        System.out.println(i + " ====================== Creating context from " + ctxFile);
        ApplicationContext appContext = new FileSystemXmlApplicationContext(ctxFile);
        Thread.sleep(delay);
      }
      for (int i = 0; i < iter; i++)
      {
        System.out.println(i + " ====================== Creating context from " + ctxFile);
        ApplicationContext appContext = new FileSystemXmlApplicationContext(ctxFile);
        Thread.sleep(delay);
        System.out.println(i + " ====================== Closing cepManager");
        CEPManager cepMgr = (CEPManager) appContext.getBean("cepManager");
        cepMgr.close();
        Thread.sleep(delay);
      }
      System.exit(0);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}

