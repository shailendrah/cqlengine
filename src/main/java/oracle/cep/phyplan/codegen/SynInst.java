/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynInst.java /main/11 2013/11/27 21:53:24 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk    07/07/12 - add mapping for buffer operator
 sbishnoi    05/29/12 - adding new operator for slide
 sbishnoi    02/10/09 - adding support for OrderByTop
 parujain    07/07/08 - value based windows
 sbishnoi    09/26/07 - add PO_MINUS
 parujain    06/28/07 - order by support
 rkomurav    05/14/07 - add classB
 sbishnoi    05/11/07 - support for distinct
 ayalaman    08/01/06 - partition window implementation
 dlenkov     07/05/06 - support for union and except
 anasrini    05/30/06 - support for group by / aggregation operator 
 najain      05/30/06 - str_join_project support 
 najain      05/26/06 - stream join support 
 dlenkov     05/24/06 - added row windows support
 najain      05/15/06 - add relation support 
 najain      05/17/06 - view support
 ayalaman    04/23/06 - support for IStream and DStream 
 anasrini    04/10/06 - support for RSTREAM 
 najain      03/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/SynInst.java /main/11 2013/11/27 21:53:24 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import java.util.HashMap;
import oracle.cep.phyplan.PhyOptKind;

/**
 * Adds a Physical synopsis for the physical operator
 * 
 * @author najain
 */
public class SynInst {
  private static final int                          NUM_OPS = 32;

  private static HashMap<PhyOptKind, SynGenFactory> synMap;

  static {
    populateSynMap();
  }

  private static void populateSynMap() {
    synMap = new HashMap<PhyOptKind, SynGenFactory>(NUM_OPS);

    synMap.put(PhyOptKind.PO_SELECT, new SynSelectFactory());
    synMap.put(PhyOptKind.PO_PROJECT, new SynProjectFactory());
    synMap.put(PhyOptKind.PO_RANGE_WIN, new SynRangeWindowFactory());
    synMap.put(PhyOptKind.PO_ROW_WIN, new SynRowWindowFactory());
    synMap.put(PhyOptKind.PO_PARTN_WIN, new SynPartnWindowFactory());
    synMap.put(PhyOptKind.PO_VALUE_WIN, new SynValueWindowFactory());
    synMap.put(PhyOptKind.PO_STREAM_SOURCE, new SynStreamSourceFactory());
    synMap.put(PhyOptKind.PO_VIEW_STRM_SRC, new SynViewStrmSrcFactory());
    synMap.put(PhyOptKind.PO_VIEW_RELN_SRC, new SynViewRelnSrcFactory());
    synMap.put(PhyOptKind.PO_RELN_SOURCE, new SynRelSourceFactory()); 
    synMap.put(PhyOptKind.PO_OUTPUT, new SynOutputFactory());
    synMap.put(PhyOptKind.PO_JOIN, new SynBinJoinFactory());
    synMap.put(PhyOptKind.PO_STR_JOIN, new SynBinStreamJoinFactory()); 
    synMap.put(PhyOptKind.PO_JOIN_PROJECT, new SynBinJoinProjectFactory()); 
    synMap.put(PhyOptKind.PO_STR_JOIN_PROJECT, new SynBinStreamJoinProjectFactory()); 
    synMap.put(PhyOptKind.PO_RSTREAM, new SynRStreamFactory());
    synMap.put(PhyOptKind.PO_ISTREAM, new SynIStreamFactory());
    synMap.put(PhyOptKind.PO_DSTREAM, new SynDStreamFactory());
    synMap.put(PhyOptKind.PO_GROUP_AGGR, new SynGroupAggrFactory());
    synMap.put(PhyOptKind.PO_UNION, new SynUnionFactory());
    synMap.put(PhyOptKind.PO_EXCEPT, new SynExceptFactory());
    synMap.put(PhyOptKind.PO_DISTINCT, new SynDistinctFactory());
    synMap.put(PhyOptKind.PO_PATTERN_STRM_CLASSB, new SynPatternStrmClassBFactory());
    synMap.put(PhyOptKind.PO_ORDER_BY, new SynOrderByFactory());
    synMap.put(PhyOptKind.PO_MINUS, new SynMinusFactory());
    synMap.put(PhyOptKind.PO_ORDER_BY_TOP, new SynOrderByTopFactory());
    synMap.put(PhyOptKind.PO_SLIDE, new SynSlideFactory());
    synMap.put(PhyOptKind.PO_BUFFER, new SynBufferFactory());
    synMap.put(PhyOptKind.PO_SUBQUERY_SRC, new SynSubQuerySrcFactory());

    /**
     * deferred for later  
     * synMap.put(PhyOptKind.PO_DISTINCT, new
     * SynDistinctFactory());  synMap.put(PhyOptKind.PO_PARTN_WIN, new
     * SynPartitionWindowFactory());
     * synMap.put(PhyOptKind.PO_SINK, new
     * SynSinkFactory()); synMap.put(PhyOptKind.PO_SS_GEN, new
     * SynSysStreamSourceFactory());
     */

  };

  public static void addSyn(SynGenFactoryContext ctx) {

    // Get the right operator
    PhyOptKind k = ctx.getPhyPlan().getOperatorKind();
    SynGenFactory f = synMap.get(k);
    assert f != null : k;

    f.addSynOpt(ctx);

    return;
  }
}
