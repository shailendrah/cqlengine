/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreInst.java /main/15 2013/11/27 21:53:24 sbishnoi Exp $ */

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
 anasrini    03/20/11 - support for EXCHANGE
 sbishnoi    12/31/09 - table function operator
 sbishnoi    02/10/09 - adding support for OrderByTop
 parujain    07/07/08 - value based windows
 najain      12/13/07 - xmltable support
 sbishnoi    09/26/07 - support for PO_MINUS
 parujain    06/28/07 - orderby store
 rkomurav    05/14/07 - add classB
 sbishnoi    05/11/07 - support for distinct
 rkomurav    03/15/07 - add patternstrmstore factory
 ayalaman    08/01/06 - partition window store
 najain      07/05/06 - add shared store 
 dlenkov     05/30/06 - support for row windows
 anasrini    05/30/06 - support for group by/aggregation operator 
 najain      05/30/06 - stream_join_project support 
 najain      05/26/06 - stream join support 
 najain      05/15/06 - relations support 
 ayalaman    04/23/06 - add physical store for IStream and DStream 
 anasrini    04/10/06 - support for RSTREAM 
 najain      03/20/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/codegen/StoreInst.java /main/15 2013/11/27 21:53:24 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.codegen;

import java.util.HashMap;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.phyplan.PhyStore;

/**
 * Adds a Physical store for the physical operator
 * 
 * @author najain
 */
public class StoreInst 
{
  private static final int NUM_OPS = 32;

  private static HashMap<PhyOptKind, StoreGenFactory> storeMap;

  static {
    populateStoreMap();
  }

  private static void populateStoreMap() {
    storeMap = new HashMap<PhyOptKind, StoreGenFactory>(NUM_OPS);

    storeMap.put(PhyOptKind.PO_SELECT, new StoreSelectFactory());
    storeMap.put(PhyOptKind.PO_PROJECT, new StoreProjectFactory());
    storeMap.put(PhyOptKind.PO_RANGE_WIN, new StoreRangeWindowFactory());
    storeMap.put(PhyOptKind.PO_ROW_WIN, new StoreRowWindowFactory());
    storeMap.put(PhyOptKind.PO_PARTN_WIN, new StorePartnWindowFactory());
    storeMap.put(PhyOptKind.PO_VALUE_WIN, new StoreValueWindowFactory());
    storeMap.put(PhyOptKind.PO_STREAM_SOURCE, new StoreStreamSourceFactory());
    storeMap.put(PhyOptKind.PO_VIEW_STRM_SRC, new StoreViewStrmSrcFactory());
    storeMap.put(PhyOptKind.PO_VIEW_RELN_SRC, new StoreViewRelnSrcFactory());
    storeMap.put(PhyOptKind.PO_RELN_SOURCE, new StoreRelSourceFactory());
    storeMap.put(PhyOptKind.PO_OUTPUT, new StoreOutputFactory());
    storeMap.put(PhyOptKind.PO_JOIN, new StoreBinJoinFactory());
    storeMap.put(PhyOptKind.PO_STR_JOIN, new StoreBinStreamJoinFactory());
    storeMap.put(PhyOptKind.PO_JOIN_PROJECT, new StoreBinJoinProjectFactory());
    storeMap.put(PhyOptKind.PO_STR_JOIN_PROJECT, new StoreBinStreamJoinProjectFactory());
    storeMap.put(PhyOptKind.PO_RSTREAM, new StoreRStreamFactory());
    storeMap.put(PhyOptKind.PO_ISTREAM, new StoreIStreamFactory());
    storeMap.put(PhyOptKind.PO_DSTREAM, new StoreDStreamFactory());
    storeMap.put(PhyOptKind.PO_GROUP_AGGR, new StoreGroupAggrFactory());
    storeMap.put(PhyOptKind.PO_UNION, new StoreUnionFactory());
    storeMap.put(PhyOptKind.PO_EXCEPT, new StoreExceptFactory());
    storeMap.put(PhyOptKind.PO_PATTERN_STRM, new StorePatternStrmFactory());
    storeMap.put(PhyOptKind.PO_DISTINCT, new StoreDistinctFactory());
    storeMap.put(PhyOptKind.PO_PATTERN_STRM_CLASSB, new StorePatternStrmClassBFactory());
    storeMap.put(PhyOptKind.PO_ORDER_BY, new StoreOrderByFactory());
    storeMap.put(PhyOptKind.PO_MINUS, new StoreMinusFactory());
    storeMap.put(PhyOptKind.PO_XMLTABLE, new StoreXmlTableFactory());
    storeMap.put(PhyOptKind.PO_ORDER_BY_TOP, new StoreOrderByTopFactory());
    storeMap.put(PhyOptKind.PO_TABLE_FUNCTION, new StoreTableFunctionSourceFactory());
    storeMap.put(PhyOptKind.PO_EXCHANGE, new StoreExchangeFactory());
    storeMap.put(PhyOptKind.PO_SLIDE, new StoreSlideFactory());
    storeMap.put(PhyOptKind.PO_BUFFER, new StoreBufferFactory());
    storeMap.put(PhyOptKind.PO_SUBQUERY_SRC, new StoreSubQuerySrcFactory());
  };

  public static PhyStore addStore(StoreGenFactoryContext ctx) {

    // Get the right operator
    PhyOptKind k = ctx.getPhyPlan().getOperatorKind();
    StoreGenFactory f = storeMap.get(k);
    assert f != null : k;

    return f.addStoreOpt(ctx);
  }
}
