/* $Header: PhyQueueKind.java 16-mar-2006.01:04:27 anasrini Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Enumeration of the various kinds of queues

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    anasrini    03/16/06 - Creation
    anasrini    03/16/06 - Creation
    anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: PhyQueueKind.java 16-mar-2006.01:04:27 anasrini Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.phyplan;

/**
 * Enumeration of the various kinds of queues
 *
 * @since 1.0
 */

public enum PhyQueueKind {
  SIMPLE_Q, READER_Q, WRITER_Q;
}
