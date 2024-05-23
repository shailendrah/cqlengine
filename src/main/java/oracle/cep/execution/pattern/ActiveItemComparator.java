/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/ActiveItemComparator.java /main/2 2011/01/04 06:40:13 udeshmuk Exp $ */

/* Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    udeshmuk    09/01/09 - include startIndex in comparison.
    udeshmuk    05/05/09 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/pattern/ActiveItemComparator.java st_pcbpel_udeshmuk_reorg_and_variable_nonevent/1 2009/09/03 04:13:36 udeshmuk Exp $
 *  @author  udeshmuk
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.execution.pattern;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;
public class ActiveItemComparator implements Comparator<ActiveItem>, Externalizable
{
  private static final long serialVersionUID = 1L;

  public int compare(ActiveItem a1, ActiveItem a2)
  {
    long ts1 = a1.getReferredBinding().getTargetTime();
    long ts2 = a2.getReferredBinding().getTargetTime();
    
    if(ts1 < ts2)
      return -1;
    else if(ts1 > ts2)
      return 1;
    else{
      if(a1.getReferredBinding().getStartIndex() < a1.getReferredBinding().getStartIndex())
        return -1;
      else if(a1.getReferredBinding().getStartIndex() > a1.getReferredBinding().getStartIndex())
        return 1;
      else {
        if(a1.getReferredBinding().getBindingId() < a2.getReferredBinding().getBindingId())
          return -1;
        else if(a1.getReferredBinding().getBindingId() > a2.getReferredBinding().getBindingId())
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