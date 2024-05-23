/* $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/UnsureItem.java /main/1 2009/04/05 10:26:18 udeshmuk Exp $ */

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
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/UnsureItem.java /main/1 2009/04/05 10:26:18 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class UnsureItem implements Externalizable
{
  /** matchedTs value of the final binding */
  private long matchedTs;
  
  /** 
   * reference to binding to which this instance of UnsureItem corresponds.
   * used to ensure uniqueness
   */
  private Binding binding;
  
  /**
   * Empty Constructor for deserialized objects
   */
  public UnsureItem()
  {}
  
  /**
   * Constructor
   * @param matchedTs matchedTs value 
   * @param binding reference to binding
   */
  public UnsureItem(long matchedTs, Binding binding)
  {
    this.matchedTs = matchedTs;
    this.binding   = binding;
  }
  
  /**
   * @return returns the matchedTs value
   */
  public long getMatchedTs()
  {
    return this.matchedTs;
  }

  /**
   * @return returns the reference to binding
   */
  public Binding getBinding()
  {
    return this.binding;
  }
  
  /**
   * Over-ridden implementation of equals(Object)
   */
  public boolean equals(Object o)
  {
    UnsureItem other = (UnsureItem) o;
    
    if(this.matchedTs != other.getMatchedTs())
      return false;
    
    if(this.binding != other.getBinding())
      return false;
    
    return true;
  }
   
  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeLong(matchedTs);
    out.writeObject(binding);
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
  {
    this.matchedTs = in.readLong();
    this.binding = (Binding)in.readObject();
  }
}