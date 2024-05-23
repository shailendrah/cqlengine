/* $Header: pcbpel/cep/common/src/oracle/cep/metadata/ViewInfo.java /main/2 2009/06/02 12:21:27 parujain Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    06/02/09 - fix viewinfo
    parujain    02/17/09 - view information
    parujain    02/17/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/common/src/oracle/cep/metadata/ViewInfo.java /main/2 2009/06/02 12:21:27 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.util.LinkedHashMap;

public class ViewInfo
{

  LinkedHashMap<String, String> attributes;

  boolean   isStream;

  boolean   isRunning;

  public ViewInfo(LinkedHashMap<String, String> attrs, boolean stream, boolean isrun)
  {
    attributes = attrs;
    isStream = stream;
    isRunning = isrun;
  }

  public LinkedHashMap<String, String> getAttributes()
  {
    return attributes;
  }

  public boolean isStream()
  {
    return isStream;
  }

  public boolean isRunning()
  {
    return isRunning;
  }
}
