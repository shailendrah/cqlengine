/* $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/ActiveItem.java /main/1 2009/05/19 02:11:10 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    05/18/09 - make equals method consistent with compare
    udeshmuk    05/05/09 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/execution/pattern/ActiveItem.java /main/1 2009/05/19 02:11:10 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ActiveItem implements Externalizable {
  
  Binding referredBinding;
  
  PatternPartnContext ownerContext; 
  
  /**
   * Empty Constructor for deserialized object
   */
  public ActiveItem()
  {}
  
  /**
   * Constructor
   * @param refBinding - binding to which the instance of ActiveItem corresponds
   * @param ownerContext - Partn context in whose ActiveList the binding resides
   */
  public ActiveItem(Binding refBinding, PatternPartnContext ownerContext)
  {
    this.referredBinding = refBinding;
    this.ownerContext = ownerContext;
  }
  
  /**
   * returns referred binding
   * @return binding corresponding to this ActiveItem instance
   */
  public Binding getReferredBinding()
  {
    return this.referredBinding;
  }
  
  /**
   * returns partn context to which the binding belongs
   * @return partn context to which the binding belongs
   */
  public PatternPartnContext getOwnerPartnContext()
  {
    return this.ownerContext;
  }
  
  /**
   * set the referred binding  
   * @param refBinding
   */
  public void setReferredBinding(Binding refBinding)
  {
    this.referredBinding = refBinding;
  }
  
  /**
   * set the pattern partn context
   * @param partnContext
   */
  public void setOwnerPartnContext(PatternPartnContext partnContext)
  {
    this.ownerContext = partnContext;
  }
  
  /**
   * Over-ridden implementation of equals method
   */
  public boolean equals(Object o)
  {
    ActiveItem other = (ActiveItem) o;
    
    if(this.referredBinding.getBindingId() != other.getReferredBinding().getBindingId())
      return false;
    
    return true;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException
  {
    out.writeObject(referredBinding);
    out.writeObject(ownerContext);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException
  {
    this.referredBinding = (Binding) in.readObject();  
    this.ownerContext = (PatternPartnContext) in.readObject();
  }
}
