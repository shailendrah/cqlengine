/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/ActiveBindingComparator.java /main/1 2011/01/04 06:40:13 udeshmuk Exp $ */

/* Copyright (c) 2009, 2010, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    08/20/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/ActiveBindingComparator.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/2 2009/09/03 04:13:34 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
 
/**
 * Comparator for Active Bindings.
 * Used to keep the active bindings in target time order.
 * @author udeshmuk
 */

public class ActiveBindingComparator implements Comparator<Binding>, Externalizable
{
  private static final long serialVersionUID = 1L;

  public int compare(Binding a1, Binding a2)
  {
    long ts1 = a1.getTargetTime();
    long ts2 = a2.getTargetTime();
    
    if(ts1 < ts2)
      return -1;
    else if(ts1 > ts2)
      return 1;
    else{
      if(a1.getStartIndex() < a2.getStartIndex())
        return -1;
      else if(a1.getStartIndex() > a2.getStartIndex())
        return 1;
      else
      {
        if(a1.getBindingId() < a2.getBindingId())
          return -1;
        else if(a1.getBindingId() > a2.getBindingId())
          return 1;
        else          
          return 0;
      }
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
