/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptStrmSrc.java /main/11 2011/03/26 18:53:31 vikshukl Exp $ */

/* Copyright (c) 2006, 2011, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Describes Stream Source logical operator in the package oracle.cep.logplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 anasrini    03/24/11 - extend LogOptSource
 anasrini    03/16/11 - partition parallelism
 sborah      12/16/08 - handle constants
 hopark      10/09/08 - remove statics
 mthatte     04/09/08 - derived ts
 mthatte     04/01/08 - 
 parujain    03/11/08 - derived timestamp
 rkomurav    02/28/08 - parameterize error
 parujain    11/09/07 - external source
 parujain    10/25/07 - db join
 anasrini    08/27/07 - support for ELEMENT_TIME
 anasrini    05/25/07 - inline view support
 rkomurav    02/22/07 - clean up applyWindow_n
 dlenkov     05/22/06 - assert for SetOpQuery
 najain      04/06/06 - cleanup
 najain      03/24/06 - unbounded window check should be in RangeWindow factory
 anasrini    03/23/06 - do not apply window if it is UNBOUNDED 
 najain      02/17/06 - add constructors/setters/getters etc.
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/logplan/LogOptStrmSrc.java /main/9 2009/02/23 06:47:35 sborah Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import java.util.ArrayList;

import oracle.cep.semantic.GenericSetOpQuery;
import oracle.cep.semantic.SFWQuery;
import oracle.cep.semantic.SetOpQuery;
import oracle.cep.semantic.SemQuery;
import oracle.cep.service.ExecContext;
import oracle.cep.logplan.LogicalPlanException;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.logplan.attr.Attr;
import oracle.cep.logplan.attr.AttrNamed;
import oracle.cep.logplan.expr.Expr;
import oracle.cep.metadata.MetadataException;

/**
 * Stream Source Logical Operator
 */
public class LogOptStrmSrc extends LogOptSource implements Cloneable {

  /**Does this have a derived timestamp? */
  private boolean isDerivedTS;
  
  /**The timestamp expression */
  private Expr derivedTSExpr;


  public boolean isDerivedTS()
  {
    return isDerivedTS;
  }

  public void setDerivedTS(boolean isDerivedTS)
  {
    this.isDerivedTS = isDerivedTS;
  }

  public Expr getDerivedTSExpr()
  {
    return derivedTSExpr;
  }

  public void setDerivedTSExpr(Expr derivedTSExpr)
  {
    this.derivedTSExpr = derivedTSExpr;
    this.isDerivedTS = true;
  }

  public int getStreamId() {
    return getEntityId();
  }

  public void setStreamId(int streamId) {
    setEntityId(streamId);
  }

  public void setStreamName(String name) {
    setEntityName(name);
  }
  
  public String getStreamName()
  {
    return getEntityName();
  }


  public LogOptStrmSrc() {
    super(LogOptKind.LO_STREAM_SOURCE);
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public LogOptStrmSrc clone() throws CloneNotSupportedException {
    LogOptStrmSrc op = (LogOptStrmSrc) super.clone();
    return op;
  }

  public LogOptStrmSrc(ExecContext ec, SemQuery query, int varId, int strId)
      throws LogicalPlanException {
    super(ec, LogOptKind.LO_STREAM_SOURCE, varId, strId, true);

    assert (query instanceof SFWQuery) || 
           (query instanceof SetOpQuery) ||
           (query instanceof GenericSetOpQuery);

    this.setIsStream(true);
    this.setNumInputs(0);
  }

  // toString method override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("<StreamSourceLogicalOperator>");

    // Dump the common fields
    sb.append(super.toString());

    sb.append("<VariableId varId=\"" + varId + "\" />");
    sb.append("<StreamId varId=\"" + entityId + "\" />");

    sb.append("</StreamSourceLogicalOperator>");

    return sb.toString();
  }
}
