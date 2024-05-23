/* $Header: ComparatorSpecs.java 29-jun-2007.14:46:21 parujain Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>
    
   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    parujain    06/29/07 - ComparatorSpecs
    parujain    06/29/07 - Creation
 */

/**
 *  @version $Header: ComparatorSpecs.java 29-jun-2007.14:46:21 parujain Exp $
 *  @author  parujain
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.comparator;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ComparatorSpecs implements Externalizable {
  private static final long serialVersionUID = -8549465974450683467L;
  
  int colNum;
  boolean isNullsFirst;
  boolean isAscending;
  
  public ComparatorSpecs()
  {
  }
  
  public ComparatorSpecs(int col, boolean isfirst, boolean isasc)
  {
    this.colNum = col;
    this.isNullsFirst = isfirst;
    this.isAscending = isasc;
  }
  
  public int getColNum()
  {
    return colNum;
  }
  
  public boolean isNullsFirst()
  {
    return isNullsFirst;
  }
  
  public boolean isAscending()
  {
    return isAscending;
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
      out.writeInt(colNum);
      out.writeBoolean(isNullsFirst);
      out.writeBoolean(isAscending);  
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException,
        ClassNotFoundException {
      colNum = in.readInt();
      isNullsFirst = in.readBoolean();
      isAscending = in.readBoolean();;  
  }
}
