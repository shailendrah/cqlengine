/* $Header: ExecQueueFactory.java 04-dec-2006.05:43:24 najain Exp $ */

/* Copyright (c) 2006, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    Base Factory class for the execution queues

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    najain      12/04/06 - stores are not storage allocators
    parujain    07/21/06 - Generic LinkedList 
    najain      06/29/06 - factory allocation cleanup 
    najain      06/18/06 - cleanup
    anasrini    03/16/06 - Creation
 */

/**
 *  @version $Header: ExecQueueFactory.java 04-dec-2006.05:43:24 najain Exp $
 *  @author  anasrini
 *  @since   1.0
 */

package oracle.cep.planmgr.codegen;

import oracle.cep.execution.queues.Queue;
import oracle.cep.exceptions.CEPException;

/**
 * Base Factory class for the execution queues
 *
 * @since 1.0
 */

abstract class ExecQueueFactory {

  abstract Queue newExecQueue(QueueGenContext ctx)
    throws CEPException;
}
