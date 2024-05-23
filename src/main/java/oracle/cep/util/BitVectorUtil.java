/* $Header: BitVectorUtil.java 18-dec-2007.10:30:55 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    BitVectorUtil provides bitwiser operations on byte[].
    It is mostly used by the store implementations for stubs.

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/18/07 - 
    najain      05/11/07 - variable length support
    hopark      05/08/07 - Creation
 */

/**
 *  @version $Header: BitVectorUtil.java 18-dec-2007.10:30:55 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.util;

import oracle.cep.common.Constants;

public class BitVectorUtil
{
  public static final int                BYTE_SIZE = Constants.BITS_PER_BYTE;

  public final static byte[] alloc(int bitSize)
  {
    int sz = bitSize / BYTE_SIZE + 1;
    return new byte[sz];
  }
  
  public final static boolean isNull(byte[] bvec)
  {
    if (bvec != null)
      for (int i = 0; i < bvec.length; i++)
	if (bvec[i] != 0)
	  return false;
    
    return true;
  }
  
  public final static void setBit0(byte[] bvec, int pos)
  {
    int bpos = pos / BYTE_SIZE;
    bvec[bpos] |= 1 << (pos % BYTE_SIZE);
  }
  
  public final static void clearBit0(byte[] bvec, int pos)
  {
    int bpos = pos / BYTE_SIZE;
    bvec[bpos] &= ~(1 << (pos % BYTE_SIZE));
  }
  
  public final static byte[] setBit(byte[] bvec, int pos)
  {
    int bpos = pos / BYTE_SIZE;
    // Grow the array if needed
    if (bvec == null || bvec.length <= bpos)
    {
      byte[] newStubs = new byte[bpos + 1];
      // Copy the stubs to a bigger array and let the garbage collector take
      // care of the old insStubs array
      if (bvec != null)
        for (int i = 0; i < bvec.length; i++)
          newStubs[i] = bvec[i];
      bvec = newStubs;
    }

    bvec[bpos] |= 1 << (pos % BYTE_SIZE);
    return bvec;
  }
  
  public final static byte[] clearBit(byte[] bvec, int pos)
  {
    int bpos = pos / BYTE_SIZE;

    // Grow the array if needed
    if (bvec == null || bvec.length <= bpos)
    {
      byte[] newStubs = new byte[bpos + 1];
      // Copy the stubs to a bigger array and let the garbage collector take
      // care of the old insStubs array
      if (bvec != null)
        for (int i = 0; i < bvec.length; i++)
          newStubs[i] = bvec[i];
      bvec = newStubs;
    }

    bvec[bpos] &= ~(1 << (pos % BYTE_SIZE));
    return bvec;
  }
  
  public final static boolean checkBit(byte[] bits, int pos)
  {
    int bpos = pos / BYTE_SIZE;
    int bitpos = pos % BYTE_SIZE;
    
    return ((bits != null) && (bpos < bits.length) && ((bits[bpos] & (1 << bitpos)) != 0));
  }
  
  public final static int nextClearBit(byte[] bits)
  {
    int pos = 0;
    int idx = 0;
    while (pos < bits.length)
    {
      byte v = bits[pos];
      if (v != 0xff)
      {
        idx = s_trailingZeroTable[~v & 0xff];
        return (pos * BYTE_SIZE) + idx;
      }
      pos++;
    }
    return -1;
  }

  /*
   * trailingZeroTable[i] is the number of trailing zero bits in the binary
   * representation of i.
   */
  private final static byte s_trailingZeroTable[] = {
    -25, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
      4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0};

}
