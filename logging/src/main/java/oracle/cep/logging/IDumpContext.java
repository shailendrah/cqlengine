/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/IDumpContext.java /main/2 2008/10/24 15:50:22 hopark Exp $ */

/* Copyright (c) 2007, 2008, Oracle and/or its affiliates. All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      10/10/08 - remove statics
    hopark      02/05/08 - add makeVoid
    hopark      12/26/07 - add xml support
    hopark      11/28/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/IDumpContext.java /main/2 2008/10/24 15:50:22 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.logging;

public interface IDumpContext
{
  ILogLevelManager getLogLevelManager();
  ILogArea getArea();
  int getLevel();
  void setLevel(ILogArea area, int level, boolean verbose);
  boolean isVerbose();
  void setVoid();
  boolean isVoid();
  void close();
  String toString();
  void writeln(String tag, Object v);
  void beginTag(String tag, String[] attribs, Object[] vals);
  void endTag(String tag);
  IDumpContext openDumper(String dumKey);
  void closeDumper(String dumpKey, IDumpContext prev);
}
