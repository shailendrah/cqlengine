/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/OptInst.java /main/16 2013/11/27 21:53:24 sbishnoi Exp $ */

/* Copyright (c) 2006, 2013, Oracle and/or its affiliates. 
All rights reserved.*/

/*
 DESCRIPTION
 Declares OptInst in package oracle.cep.planmgr.codegen.

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 udeshmuk  07/07/12 - add buffer operator mapping
 sbishnoi  05/29/12 - adding new operator for slide
 anasrini  03/28/11 - support for reInstantiation
 anasrini  03/20/11 - support for EXCHANGE
 sbishnoi  01/01/10 - table function operator
 parujain  03/19/09 - stateless server
 sbishnoi  02/10/09 - support for OrderByTop
 parujain  07/07/08 - value based windows
 najain    12/13/07 - xmltable support
 sbishnoi  09/26/07 - support for MINUS
 parujain  06/28/07 - order by support
 rkomurav  05/15/07 - add classb
 rkomurav  03/02/07 - add pattern
 hopark    11/09/06 - bug 5465978 : refactor newExecOpt
 najain    06/13/06 - bug fix
 najain    05/30/06 - add stream join (project)
 najain    05/25/06 - add join_project
 najain    05/17/06 - view support
 najain    03/30/06 - set input/output ExecOpt
 anasrini  03/15/06 - temp change for compilation
 anasrini  03/13/06 - instOp should throw CEPException
 skaluska  02/28/06 - Creation
 skaluska  02/28/06 - Creation
 */
 
/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/planmgr/codegen/OptInst.java /main/16 2013/11/27 21:53:24 sbishnoi Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.planmgr.codegen;

import java.util.HashMap;

import oracle.cep.metadata.Query;
import oracle.cep.phyplan.PhyOpt;
import oracle.cep.phyplan.PhyOptKind;
import oracle.cep.service.ExecContext;
import oracle.cep.execution.operators.ExecOpt;
import oracle.cep.exceptions.CEPException;

/**
 * Instantiates an execution operator corresponding to a physical operator
 *
 * @author skaluska
 */
public class OptInst
{
    private static final int NUM_OPS = 32;
    private static HashMap<PhyOptKind, ExecOptFactory> execMap;
    private static HashMap<PhyOptKind, ExecOptFactory> processMap;

    static {
        populateExecMap();
        populateProcessMap();
    }

    private static void populateExecMap()
    {
        execMap = new HashMap<PhyOptKind, ExecOptFactory>(NUM_OPS);

        execMap.put(PhyOptKind.PO_SELECT, new SelectFactory());
        execMap.put(PhyOptKind.PO_PROJECT, new ProjectFactory());
        execMap.put(PhyOptKind.PO_JOIN, new BinJoinFactory());
        execMap.put(PhyOptKind.PO_JOIN_PROJECT, new BinJoinProjectFactory());
        execMap.put(PhyOptKind.PO_STR_JOIN, new BinStreamJoinFactory());
        execMap.put(PhyOptKind.PO_STR_JOIN_PROJECT,
                    new BinStreamJoinProjectFactory());
        execMap.put(PhyOptKind.PO_GROUP_AGGR, new GroupAggrFactory());
        execMap.put(PhyOptKind.PO_DISTINCT, new DistinctFactory());
        execMap.put(PhyOptKind.PO_ROW_WIN, new RowWindowFactory());
        execMap.put(PhyOptKind.PO_RANGE_WIN, new RangeWindowFactory());
        execMap.put(PhyOptKind.PO_VALUE_WIN, new ValueWindowFactory());
        execMap.put(PhyOptKind.PO_PARTN_WIN, new PartitionWindowFactory());
        execMap.put(PhyOptKind.PO_ISTREAM, new IStreamFactory());
        execMap.put(PhyOptKind.PO_DSTREAM, new DStreamFactory());
        execMap.put(PhyOptKind.PO_RSTREAM, new RStreamFactory());
        execMap.put(PhyOptKind.PO_UNION, new UnionFactory());
        execMap.put(PhyOptKind.PO_EXCEPT, new ExceptFactory());
        execMap.put(PhyOptKind.PO_STREAM_SOURCE, new StreamSourceFactory());
        execMap.put(PhyOptKind.PO_VIEW_STRM_SRC, new ViewStrmSrcFactory());
        execMap.put(PhyOptKind.PO_RELN_SOURCE, new RelSourceFactory());
        execMap.put(PhyOptKind.PO_VIEW_RELN_SRC, new ViewRelnSrcFactory());
        execMap.put(PhyOptKind.PO_OUTPUT, new OutputFactory());
        execMap.put(PhyOptKind.PO_SINK, new SinkFactory());
        execMap.put(PhyOptKind.PO_SS_GEN, new SysStreamSourceFactory());
        execMap.put(PhyOptKind.PO_PATTERN_STRM, new PatternFactory());
        execMap.put(PhyOptKind.PO_PATTERN_STRM_CLASSB,
                    new PatternStrmClassBFactory());
        execMap.put(PhyOptKind.PO_ORDER_BY, new OrderByFactory());
        execMap.put(PhyOptKind.PO_MINUS, new MinusFactory());
        execMap.put(PhyOptKind.PO_XMLTABLE, new XmlTableFactory());
        execMap.put(PhyOptKind.PO_ORDER_BY_TOP, new OrderByTopFactory());
        execMap.put(PhyOptKind.PO_TABLE_FUNCTION,
                    new TableFunctionRelSourceFactory());
        execMap.put(PhyOptKind.PO_EXCHANGE, new ExchangeFactory());
        execMap.put(PhyOptKind.PO_SLIDE, new SlideFactory());
        execMap.put(PhyOptKind.PO_BUFFER, new BufferFactory());
        execMap.put(PhyOptKind.PO_SUBQUERY_SRC, new SubQuerySrcFactory());
    };

    private static void populateProcessMap()
    {
        processMap = new HashMap<PhyOptKind, ExecOptFactory>(NUM_OPS);

        processMap.put(PhyOptKind.PO_RELN_SOURCE, new RelSourceFactory());
        processMap.put(PhyOptKind.PO_VIEW_RELN_SRC, new ViewRelnSrcFactory());
    };

    public static ExecOpt instOp(ExecContext ec, Query query, PhyOpt phyopt) 
      throws CEPException {

        ExecOpt execop;
        ExecOptFactory f;
        PhyOptKind k;

        // Get the right operator
        k = phyopt.getOperatorKind();
        f = execMap.get(k);
        assert f != null : k;

        CodeGenContext ctx = f.createCodeGenContext(ec, query, phyopt);
        execop = f.instantiate(ctx);
        return execop;
    }


    public static void reInstOp(ExecContext ec, Query query, PhyOpt phyopt) 
      throws CEPException {

        ExecOptFactory f;
        PhyOptKind k;

        // Get the right operator
        k = phyopt.getOperatorKind();
        f = execMap.get(k);
        assert f != null : k;

        CodeGenContext ctx = f.createCodeGenContext(ec, query, phyopt);
        f.reInstantiate(ctx);
    }

    public static void qryProcessOp(ExecContext ec, Query query,
                                    PhyOpt phyopt) 
    {
        ExecOptFactory f;
        PhyOptKind k;

        // Get the right operator
        k = phyopt.getOperatorKind();
        f = processMap.get(k);
        if (f != null)
        {
          CodeGenContext ctx = f.createCodeGenContext( ec, query, phyopt);
          f.processExecOpt(ctx);
        }
    }

    // Leave this method for now for compilation. To be removed
    public static ExecOpt genExecPlan(PhyOpt rootOp) 
    {
        return null;
    }
}
