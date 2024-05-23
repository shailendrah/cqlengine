/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/internals/windows/ValueWindow.java /main/1 2011/10/01 09:28:39 sbishnoi Exp $ */

/* Copyright (c) 2011, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    sbishnoi    09/24/11 - support for slide in value window
    sbishnoi    09/07/11 - Creation
 */
package oracle.cep.execution.internals.windows;

/**
 *  @version $Header: ValueWindow.java 07-sep-2011.03:47:41 sbishnoi Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */

public abstract class ValueWindow
{
  public abstract boolean visibleW(long currVal);
  
  public abstract boolean visibleW(double currVal);
  
  public abstract boolean expiredW(long currVal);

  public abstract boolean expiredW(double currVal);
 
  public abstract void setWindowSize(Object size);
  
  public abstract void setBaseValue(long val);
  
  public abstract void setBaseValue(double val);
  
  public abstract long getVisibleVal(long currVal );
  
  public abstract long getExpiredVal(long currVal);
  
  public abstract void setSlide(long slideSize);
}
