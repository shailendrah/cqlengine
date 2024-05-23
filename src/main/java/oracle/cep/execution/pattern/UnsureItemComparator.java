/* $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/UnsureItemComparator.java /main/1 2009/04/05 10:26:18 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    04/01/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/UnsureItemComparator.java /main/1 2009/04/05 10:26:18 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import oracle.cep.execution.pattern.UnsureItem;

public class UnsureItemComparator implements Comparator<UnsureItem>, Externalizable
{
  /**
   * 
   */
  private static final long serialVersionUID = 7183696575874025080L;

  public int compare(UnsureItem u1, UnsureItem u2)
  {
    long ts1 = u1.getMatchedTs();
    long ts2 = u2.getMatchedTs();
    
    if(ts1 < ts2)
      return -1;
    else if(ts1 > ts2)
      return 1;
    else{
      if(u1.getBinding().getBindingId() < u2.getBinding().getBindingId())
        return -1;
      else if(u1.getBinding().getBindingId() > u2.getBinding().getBindingId())
        return 1;
      else 
        return 0;
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
  }
  
}