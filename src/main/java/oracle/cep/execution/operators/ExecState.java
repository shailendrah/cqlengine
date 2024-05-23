/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecState.java /main/24 2012/10/22 14:42:18 vikshukl Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares ExecState in package oracle.cep.execution.operators.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
    vikshukl  10/02/12 - add S_ARCHIVED_SIA_DONE to indicate that an operator
                         has initialized its state
    sbishnoi  10/01/11 - XbranchMerge sbishnoi_bug-12720971_ps5 from
                         st_pcbpel_11.1.1.4.0
    sbishnoi  09/13/11 - silent relation cleanup
    udeshmuk  03/22/10 - rename nonevent state
    parujain  04/08/09 - piggyback support
    sbishnoi  03/11/09 - adding few states
    parujain  06/18/08 - slide support
    rkomurav  06/03/08 - add S_NONEVENT_OTHER_PARTITIONS
    mthatte   04/07/08 - adding state OUTPUT_TUPLE_DERIVED_TIMESTAMP
    parujain  12/18/07 - external relations
    parujain  12/14/07 - Add state for external reln
    sbishnoi  10/30/07 - add S_PROCESS_UPDATE
    rkomurav  10/09/07 - reorganize
    rkomurav  10/03/07 - add remove partition state
    rkomurav  08/08/07 - add state
    anasrini  07/13/07 - support for patterns partition by
    parujain  06/29/07 - orderby support
    rkomurav  06/19/07 - add report_binding
    rkomurav  05/29/07 - add PREINIT
    hopark    05/07/07 - add partitionwindow specific states
    rkomurav  04/16/07 - add bindingconsumed state
    najain    03/12/07 - bug fix
    hopark    12/29/06 - add expiring old
    rkomurav  11/12/06 - outerjoin support
    najain    09/25/06 - add more states
    najain    08/09/06 - fix except
    dlenkov   07/30/06 - added states
    najain    04/18/06 - add S_INPUT_ELEM_CONSUMED
    najain    04/18/06 - more states
    najain    04/12/06 - add more states 
    skaluska  04/04/06 - add OUTPUT_TIMESTAMP 
    skaluska  03/14/06 - query manager 
    skaluska  02/24/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/execution/operators/ExecState.java /main/24 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */


package oracle.cep.execution.operators;

/**
 * Execution States for an operator
 * State persists across invocations of run().
 * Thus, operator execution needs to design these as
 * re-entrant states.
 * @author skaluska
 */
public enum ExecState
{
  //uninitialized
  S_UNINIT,
  // propagate old data
  S_PROPAGATE_OLD_DATA,
  //pre initialisation process
  S_PRE_INIT,
  //initialized and ready to process
  S_INIT,
  //input tuple has been dequeued
  S_INPUT_DEQUEUED,
  //operator specific processing steps
  S_PROCESSING1,
  S_PROCESSING2,
  S_PROCESSING3,
  S_PROCESSING4,
  S_PROCESSING5,
  S_PROCESSING6,
  S_PROCESSING7,
  S_PROCESSING8,
  // rows slide specific states
  S_POPULATE_SLIDE_LIST,
  S_IGNORE_POPULATING_OUTPUTS,
  S_POPULATE_ALL_OUTPUTS,
  // JOIN operator specific states
  S_OUTER_INPUT_DEQUEUED,
  S_INNER_INPUT_DEQUEUED,
  S_GENERATE_HEARTBEAT,
  S_PROCESS_OUTER_PLUS,
  S_PROCESS_OUTER_MINUS,
  S_PROCESS_INNER_PLUS,
  S_PROCESS_INNER_MINUS,
  S_PROCESS_OUTER_SCAN,
  S_PROCESS_INNER_SCAN,
  S_PROCESS_GET_NEXT_OUTER_ELEM,
  S_PROCESS_GET_NEXT_INNER_ELEM,
  S_ALLO_POPU_OUTPUT_TUPLE,
  S_POPULATE_JOIN_SYNOPSIS,
  S_PROCESS_OUTER_SCAN_DEL,
  S_PROCESS_INNER_SCAN_DEL,
  S_PROCESS_GET_NEXT_OUTER_ELEM_DEL,
  S_PROCESS_GET_NEXT_INNER_ELEM_DEL,
  S_DELETE_OUTPUT_TUPLE,
  S_PROCESS_JOIN_SCAN_DEL,
  // Relation specific states
  S_PROCESS_PLUS,
  S_PROCESS_MINUS,
  S_PROCESS_UPDATE,
  S_PROCESS_UPSERT,
  // allocate memory
  S_ALLOCATE_ELEM,
  //output tuple allocated
  S_OUTPUT_TUPLE,
  //output tuple allocated with derived timestamp
  S_OUTPUT_TUPLE_DERIVED_TIMESTAMP,
  //output tuple is ready
  S_OUTPUT_READY,
  //output element timestamp is allocated
  S_OUTPUT_TIMESTAMP,
  //output element allocated
  S_OUTPUT_ELEMENT,
  //output element enqueued
  S_OUTPUT_ENQUEUED,
  S_LAST_OUTPUT_ELEMENT,
  // input element consumed
  S_INPUT_ELEM_CONSUMED,
  S_OUTER_INPUT_ELEM_CONSUMED,
  S_INNER_INPUT_ELEM_CONSUMED,
  S_OUTPUT_NEG_NULL_INNER,
  S_OUTPUT_POS_NULL_INNER,
  S_OUTPUT_NEG_OUTER_NULL,
  S_OUTPUT_POS_OUTER_NULL,
  // partition window specific states
  S_EXPIRING_OUTOFRANGE,
  S_EXPIRING_OLDEST_IN_PARTITION,
  S_EXPIRING_OLD,
  // pattern specific
  S_BINDING_CONSUMED,
  S_INIT_REPORT_BINDING,
  S_REPORT_BINDING,
  S_PROCESS_OUTPUT_BINDING,
  S_FIND_PARTITION,
  S_REMOVE_EMPTY_PARTNS,
  //non event and within pattern related
  S_NONEVENT_WITHIN_OTHER_PARTITIONS,
  // order by specific states
  S_SORT_LIST,
  S_READ_LIST_ELEM;
}
