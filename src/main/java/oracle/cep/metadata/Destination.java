/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Destination.java /main/7 2014/12/10 06:05:27 udeshmuk Exp $ */

/* Copyright (c) 2007, 2014, Oracle and/or its affiliates. 
All rights reserved.*/

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    11/24/14 - bug 19305663 - add helper method to get properties
                           as a string
    udeshmuk    09/24/10 - XbranchMerge udeshmuk_prop_hb_across_processors from
                           st_pcbpel_11.1.1.4.0
    udeshmuk    09/01/10 - add propagateHeartbeat
    sbishnoi    08/25/09 - support for batch output
    parujain    02/09/09 - equals
    parujain    01/14/09 - metadata in-mem
    sbishnoi    11/06/07 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/metadata/Destination.java /main/7 2014/12/10 06:05:27 udeshmuk Exp $
 *  @author  sbishnoi
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Destination implements Externalizable , Cloneable {
  
  /**
   * Default suid
   */
  private static final long serialVersionUID = 1L;
  
  /** EPR Destination String*/
  private String extDest;
  
  /** Flag if Destination supports update Semantics*/
  private boolean isUpdateSemantics;
  
  private int destId;
  
  /** flag to check if output tuples should be batched over time*/
  private boolean isBatchOutputTuples;

  /** flag to indicate whether heartbeats should also be propagated */
  private boolean propagateHeartbeat;
  
  public Destination()
  {
  }

  /**
   * Constructor
   * @param extDest Destination string
   */
  Destination(String extDest)
  {
    this(extDest, false);
  }
    
  public Destination clone() throws CloneNotSupportedException {
     Destination dest = (Destination)super.clone();
     return dest;
  }
 
  /**
   * Constructor
   * @param extDest Destination string
   * @param isUpdateSemantics flag whether query output should contain Update
   *   tuples.
   */
  Destination(String extDest, boolean isUpdateSemantics)
  {
    this.extDest           = extDest;
    this.isUpdateSemantics = isUpdateSemantics;
    this.destId = 0;
    this.propagateHeartbeat = false;
  }
  
  /**
   * Constructor
   * @param extDest Destination EPR string
   * @param isUpdateSemantics flag whether query output should contain Update
   * @param isBatchOutputTuples flag whether the query output tuples should be
   *        batched over time
   */
  Destination(String extDest, boolean isUpdateSemantics, 
              boolean isBatchOutputTuples)
  {
    this(extDest, isUpdateSemantics);
    this.isBatchOutputTuples = isBatchOutputTuples;
    this.propagateHeartbeat = false;
  }
  
  /**
   * Constructor
   * @param extDest Destination EPR string
   * @param isUpdateSemantics flag whether query output should contain Update
   * @param isBatchOutputTuples flag whether the query output tuples should be
            batched over time
   * @param propagateHeartbeat flag whether heartbeat should also be propagated
   */
  Destination(String extDest, boolean isUpdateSemantics,
              boolean isBatchOutputTuples, boolean propagateHeartbeat)
  {
    this(extDest, isUpdateSemantics, isBatchOutputTuples);
    this.propagateHeartbeat = propagateHeartbeat;
  }
  
  /**
   * Get Destination String
   * @return
   */
  public String getExtDest()
  {
    return this.extDest;
  }
  
  /**
   * Get isUpdateSemantics Flag
   * @return
   */
  public boolean getIsUpdateSemantics()
  {
    return this.isUpdateSemantics;
  }
 
  /**
   * Get propagateHeartbeat flag
   * @return
   */
  public boolean getPropagateHeartbeat()
  {
    return this.propagateHeartbeat;
  }
   
  public void setDestId(int id)
  {
    this.destId = id;
  }
  
  public boolean equals(Destination o)
  {
    if(o == null)
      return false;
    Destination other = (Destination)o;
    if(this.extDest.equals(other.extDest))
    {
      if(this.isUpdateSemantics == other.isUpdateSemantics)
      {
        if(this.propagateHeartbeat = other.propagateHeartbeat)
	{
          if(this.destId == other.destId)
	    return true;
	}
      }
    }
    return false;
  }

  /**
   * @return the isBatchOutputTuples
   */
  public boolean isBatchOutputTuples()
  {
    return isBatchOutputTuples;
  }

  //Helper method that returns the properties as a string
  //Used in construction of query destination DDL
  public String getProps()
  {
    if((isBatchOutputTuples()) && (getIsUpdateSemantics()))
      return new String(" batch output, use update semantics");
    else if((isBatchOutputTuples()) && (getPropagateHeartbeat()))
      return new String(" batch output, propagate heartbeat");
    else if(isBatchOutputTuples())
      return new String(" batch output");
    else if(getIsUpdateSemantics())
      return new String(" use update semantics");
    else if(getPropagateHeartbeat())
      return new String(" propagate heartbeat");
    else
      return null;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeObject(extDest);
      out.writeInt(destId);
      out.writeBoolean(isUpdateSemantics);
      out.writeBoolean(isBatchOutputTuples);
      out.writeBoolean(propagateHeartbeat);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      extDest = (String) in.readObject();
      destId = in.readInt();
      isUpdateSemantics = in.readBoolean();
      isBatchOutputTuples = in.readBoolean();
      propagateHeartbeat = in.readBoolean();
  }
}
