/* $Header: TimedTupleSinglyList.java 03-mar-2008.13:56:30 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
   TimedTupleSinglyList

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
     The methods in this class are not synchronized.
     It should be done on client side.

   MODIFIED    (MM/DD/YY)
    hopark      12/27/07 - support xmllog
    hopark      11/03/07 - remove getNodeStr
    hopark      12/19/07 - change clear behavior
    hopark      11/29/07 - make it shared
    hopark      10/18/07 - Creation
 */

/**
 *  @version $Header: TimedTupleSinglyList.java 03-mar-2008.13:56:30 hopark Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.dataStructures.internal.memory;

import oracle.cep.dataStructures.internal.ITimedTupleSinglyList;
import oracle.cep.dataStructures.internal.ITimedTupleSinglyListNode;
import oracle.cep.dataStructures.internal.ITuplePtr;
import oracle.cep.execution.ExecException;
import oracle.cep.logging.DumpDesc;

@DumpDesc(attribTags={"Id", "Length"}, 
          attribVals={"getId", "getSize"})
public class TimedTupleSinglyList 
  extends TupleSinglyList
  implements ITimedTupleSinglyList
{
  /**
   * Empty Constructor
   * 
   */
  public TimedTupleSinglyList()
  {
    super();
  }
  
  public void add(ITuplePtr elem, long ts, int readers) throws ExecException
  {
    TimedTupleSinglyListNode node4 = addElem(elem);
    node4.setTs(ts);
    node4.setReaders(readers);
  }

  @DumpDesc(attribTags={"Id", "Next"}, 
            attribVals={"getId", "getNextId"},
            valueTags={"Tuple", "Ts", "Readers"},
            values={"getNodeElem", "getTs", "@readers"})
  public static class TimedTupleSinglyListNode 
    extends TupleSinglyListNode
    implements ITimedTupleSinglyListNode
  {
    long ts;
    int  readers;
    
    public TimedTupleSinglyListNode()
    {
      super();
      ts = 0;
      readers = 0;
    }
  
    public void setReaders(int n)
    {
      readers = n;
    }
    
    public int decrementAndGet()  throws ExecException
    {
      return --readers;
    }
    
    public int incrementAndGet()  throws ExecException
    {
      return ++readers;
    }
    
    /**
     * Clears the node
     */
    public void clear()
    {
      super.clear();
      ts = 0;
      readers = 0;
    }

    public void setTs(long ts) throws ExecException
    {
      this.ts = ts;
    }

    public long getTs() throws ExecException
    {
      return ts;
    }

    public String toString()
    {
      StringBuffer buff = new StringBuffer();
      buff.append(super.toString());
      buff.append(" ts=");
      buff.append(Long.toString(ts));
      buff.append(" readers=");
      buff.append(readers);
      return buff.toString();
    }
  }
}

