/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILogLevelManager.java /main/5 2009/05/01 16:16:48 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES

 MODIFIED    (MM/DD/YY)
 hopark      04/21/09 - add dump
 hopark      12/05/08 - add setConfig
 hopark      10/10/08 - remove statics
 hopark      06/18/08 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/ILogLevelManager.java /main/5 2009/05/01 16:16:48 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging;

import java.util.List;

public interface ILogLevelManager
{
  void setConfig(boolean useXML, String traceFolder);
  String getTraceFolder();
  boolean getUseXML();
  
  void clear();

  Levels getLevels(ILogArea a, int id, ILogEvent event);

  void set(boolean enable, ILogArea a, int id, ILogEvent event, int level);

  void dump(ILogArea area, int id, int level);

  String getCurrentInfoXML();
  String getCurrentInfo();

  ILogArea[] getLogAreas();
  ILogArea getLogAreaFromValue(int v);
  ILogEvent getLogEventFromValue(int v);
  ILogEvent[] getEvents(ILogArea area);
  LevelDesc[] getLevelDescs(ILogArea a);
  int[] getLevels(ILogArea a);
  String getLevelDesc(ILogArea a, int level);

  IDumpContext openDumper(String key, IDumpContext prev);
  void closeDumper(String key, IDumpContext prev, IDumpContext current);
  
  /**
   * only for unit testing
   */
  List<Integer> getIds(ILogArea a);
  List<Integer> getEvents(ILogArea a, int id);
 }
