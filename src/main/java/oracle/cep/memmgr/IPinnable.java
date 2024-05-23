/* $Header: IPinnable.java 18-dec-2007.10:30:54 hopark Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      12/07/07 - cleanup spill
    hopark      11/07/07 - fix pin api
    hopark      06/19/07 - cleanup
    hopark      04/06/07 - add permanent pin
    hopark      03/26/07 - add checking pinmode
    hopark      03/23/07 - Creation
 */

/**
 *  @version $Header: IPinnable.java 18-dec-2007.10:30:54 hopark Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.memmgr;

import oracle.cep.execution.ExecException;

/**
 * IPinnable
 *
 * @author hopark
 */
public interface IPinnable<E>
{
  public final static int READ  = 0;
  public final static int WRITE = 1;
  
  /**
     * Pins the node
     */
  <T extends IPinnable<E>> T pin(int mode) throws ExecException;
  
  void unpin() throws ExecException;
}

