/* $Header: pcbpel/cep/server/src/oracle/cep/metadata/IdStub.java /main/3 2009/01/16 22:55:00 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    01/14/09 - metadata in-mem
    parujain    01/24/07 - 
    najain      06/16/06 - bug fix 
    najain      06/13/06 - bug fix 
    najain      06/05/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/metadata/IdStub.java /main/3 2009/01/16 22:55:00 parujain Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.metadata;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;

// Structure that stores RelnId --> OldNumStubs
public class IdStub implements Externalizable, Cloneable
{
 
  private static final long serialVersionUID = -8745019577921531077L;

  private int  relnId;
  private BitSet readers;

  public IdStub()
  {
      
  }
  
  public IdStub(int relnId, BitSet readers)
  {
    this.relnId  = relnId;
    this.readers = readers;
  }
 
  public IdStub clone() throws CloneNotSupportedException {
    IdStub stub = (IdStub)super.clone();
    return stub;
  }


  public int getRelnId() {
    return relnId;
  }

  public BitSet getReaders() {
    return readers;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(relnId);
      out.writeObject(readers);
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      relnId = in.readInt();
      readers = (BitSet) in.readObject();
  }
}
