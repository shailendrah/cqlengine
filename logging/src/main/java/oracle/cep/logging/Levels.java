/* $Header: pcbpel/cep/logging/src/oracle/cep/logging/Levels.java /main/2 2009/02/26 21:32:10 hopark Exp $ */

/* Copyright (c) 2007, 2009, Oracle and/or its affiliates.
All rights reserved. */

/*
   DESCRIPTION
    Levels encapsulate the bitset implementation that keeps levels.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      02/24/09 - add getInfoXML
    hopark      08/01/07 - add fromList
    hopark      06/12/07 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/logging/src/oracle/cep/logging/Levels.java /main/2 2009/02/26 21:32:10 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logging;

import java.util.List;

public class Levels
{
  // Levels uses long at this point.
  // If the number of level is bigger than 64, it should be change to
  // BitSet
  long  m_bits;
  
  static final String LEVELS_TAG = "Levels";
  static final String LEVEL_TAG = "Level";

  public Levels()
  {
    m_bits = 0;
  }
  
  public void set(boolean enable, int bit)
  {
    if (enable) set(bit);
    else clear(bit);
  }
  
  public void set(int bit)
  {
    assert (bit >= 0 && bit < 64);
    m_bits |= (1L << bit);
  }
  
  public void clear(int bit)
  {
    assert (bit >= 0 && bit < 64);
    m_bits &= ~(1L << bit);
  }
  
  public boolean get(int bit)
  {
    assert (bit >= 0 && bit < 64);
    return ((m_bits & ( 1L << bit)) != 0);
  }
  
  public void or(Levels other)
  {
    m_bits |= other.m_bits;
  }
  
  public int nextSetBit(int bit)
  {
    assert (bit >= 0 && bit < 64);

    while (bit < 64) 
    {
      if (((m_bits & ( 1L << bit)) != 0))
        return bit;
      bit++;
    }
    return -1;
  }

  public boolean isEmpty()
  {
    return (m_bits == 0);
  }
  
  public boolean equals(Object other)
  {
    if (!(other instanceof Levels)) return false;
    Levels o = (Levels) other;
    return (m_bits == o.m_bits);
  }
  
  public static Levels fromList(List<Integer> lvs)
  {
    Levels levels = new Levels();
    for (Integer i : lvs)
      levels.set(i);
    return levels;
  }
  
  public String getInfoXML()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("<"); buf.append(LEVELS_TAG);  buf.append("> ");
    for (int level = nextSetBit(0); level >= 0; level = nextSetBit(level+1))
    {
      buf.append("<"); buf.append(LEVEL_TAG);  buf.append(">");
      buf.append(level);
      buf.append("</"); buf.append(LEVEL_TAG);  buf.append("> ");
    }
    buf.append("</"); buf.append(LEVELS_TAG); buf.append(">\n");
    return buf.toString();
  }
  
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    for (int level = nextSetBit(0); level >= 0; level = nextSetBit(level+1))
    {
      buf.append(level + " ");
    }
    return buf.toString();
  }
}
