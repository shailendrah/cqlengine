/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/LogPlanInterpreterFactory.java /main/13 2012/06/07 03:24:37 sbishnoi Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 <short description of component this file declares/defines>

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 sbishnoi    05/28/12 - support slide without window
 vikshukl    08/25/11 - subquery support
 anasrini    03/20/11 - support for PhyOptExchange
 sbishnoi    12/26/09 - adding new operator for table function
 parujain    07/07/08 - value based windows
 najain      12/11/07 - add xmltype support
 sbishnoi    09/26/07 - support for MINUS setop
 parujain    06/28/07 - order by support
 sbishnoi    05/11/07 - support for distinct
 parujain    03/07/07 - Extensible window support
 rkomurav    02/27/07 - add pattern
 ayalaman    08/01/06 - add partition window interpreter
 najain      07/31/06 - silent relations
 dlenkov     06/12/06 - Union & except support
 najain      06/05/06 - add query 
 anasrini    05/31/06 - Support for GROUP/AGGREGATION 
 najain      05/26/06 - support StreamJoin 
 ayalaman    04/23/06 - support for IStream and DStream
 anasrini    04/04/06 - support for RSTREAM 
 najain      04/04/06 - cleanup
 najain      03/01/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/phyplan/factory/LogPlanInterpreterFactory.java /main/13 2012/06/07 03:24:37 sbishnoi Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan.factory;

import java.util.HashMap;
import oracle.cep.logplan.LogOptCross;
import oracle.cep.exceptions.CEPException;
import oracle.cep.logplan.LogOptSlide;
import oracle.cep.logplan.LogOptSubquerySrc;
import oracle.cep.logplan.LogOptExchange;
import oracle.cep.logplan.LogOptExtensibleWin;
import oracle.cep.logplan.LogOptOrderBy;
import oracle.cep.logplan.LogOptPatternStrm;
import oracle.cep.logplan.LogOptStrmCross;
import oracle.cep.logplan.LogOptStrmSrc;
import oracle.cep.logplan.LogOptRelnSrc;
import oracle.cep.logplan.LogOptRngWin;
import oracle.cep.logplan.LogOptNowWin;
import oracle.cep.logplan.LogOptRowWin;
import oracle.cep.logplan.LogOptPrtnWin;
import oracle.cep.logplan.LogOptTableFunctionRelSource;
import oracle.cep.logplan.LogOptValueWin;
import oracle.cep.logplan.LogOptSelect;
import oracle.cep.logplan.LogOptProject;
import oracle.cep.logplan.LogOptRStream;
import oracle.cep.logplan.LogOptIStream;
import oracle.cep.logplan.LogOptDStream;
import oracle.cep.logplan.LogOptGrpAggr;
import oracle.cep.logplan.LogOptUnion;
import oracle.cep.logplan.LogOptExcept;
import oracle.cep.logplan.LogOptDistinct;
import oracle.cep.logplan.LogOptMinus;
import oracle.cep.logplan.LogOpt;
import oracle.cep.logplan.LogOptXmlTable;
import oracle.cep.phyplan.PhyOpt;

/**
 * Factory for the parse tree node specific interpreters
 * <p>
 * This is private to the semantic analysis module.
 *
 * @since 1.0
 */

public class LogPlanInterpreterFactory {

  private static HashMap<String, PhyOptFactory> interpMap;

  static {
    populateInterpMap();
  }

  static void populateInterpMap()
  {
    interpMap = new HashMap<String, PhyOptFactory>();

    interpMap.put(LogOptStrmSrc.class.getName(), new PhyOptStrmSrcFactory());
    interpMap.put(LogOptRelnSrc.class.getName(), new PhyOptRelnSrcFactory());
    interpMap.put(LogOptRngWin.class.getName(), new PhyOptRngWinFactory());
    interpMap.put(LogOptNowWin.class.getName(), new PhyOptRngWinFactory());
    interpMap.put(LogOptExtensibleWin.class.getName(), new PhyOptRngWinFactory());
    interpMap.put(LogOptRowWin.class.getName(), new PhyOptRowWinFactory());
    interpMap.put(LogOptPrtnWin.class.getName(), new PhyOptPrtnWinFactory());
    interpMap.put(LogOptValueWin.class.getName(), new PhyOptValueWinFactory());
    interpMap.put(LogOptSelect.class.getName(), new PhyOptSelectFactory());
    interpMap.put(LogOptProject.class.getName(), new PhyOptProjectFactory());
    interpMap.put(LogOptCross.class.getName(), new PhyOptJoinFactory());
    interpMap.put(LogOptStrmCross.class.getName(), new PhyOptStrJoinFactory());
    interpMap.put(LogOptRStream.class.getName(), new PhyOptRStrmFactory());
    interpMap.put(LogOptIStream.class.getName(), new PhyOptIStrmFactory());
    interpMap.put(LogOptDStream.class.getName(), new PhyOptDStrmFactory());
    interpMap.put(LogOptGrpAggr.class.getName(), new PhyOptGroupAggrFactory());
    interpMap.put(LogOptUnion.class.getName(), new PhyOptUnionFactory());
    interpMap.put(LogOptExcept.class.getName(), new PhyOptExceptFactory());
    interpMap.put(LogOptPatternStrm.class.getName(), new PhyOptPatternFactory());
    interpMap.put(LogOptDistinct.class.getName(), new PhyOptDistinctFactory());
    interpMap.put(LogOptOrderBy.class.getName(), new PhyOptOrderByFactory());
    interpMap.put(LogOptMinus.class.getName(), new PhyOptMinusFactory());
    interpMap.put(LogOptXmlTable.class.getName(), new PhyOptXmlTableFactory());
    interpMap.put(LogOptTableFunctionRelSource.class.getName(), new PhyOptTableFunctionRelSrcFactory());
    interpMap.put(LogOptExchange.class.getName(), new PhyOptExchangeFactory());
    interpMap.put(LogOptSubquerySrc.class.getName(), new PhyOptSubquerySrcFactory());
    interpMap.put(LogOptSlide.class.getName(), new PhyOptSlideFactory());
  }

  public static PhyOpt getInterpreter(LogOpt op,
      LogPlanInterpreterFactoryContext ctx) throws CEPException {

    PhyOptFactory o = interpMap.get(op.getClass().getName());
    assert o != null : op.getClass().getName();

    return (PhyOpt)o.getNewPhyOpt(ctx);
  }
}
