/* $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptRelnSrc.java /main/16 2012/10/22 14:42:18 vikshukl Exp $ */

/* Copyright (c) 2006, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Describes the Relation Source logical operator in the package 
 oracle.cep.logplan

 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>

 NOTES
 <other useful comments, qualifications, etc.>

 MODIFIED    (MM/DD/YY)
 vikshukl    08/07/12 - archived dimensions
 vikshukl    03/25/11 - support for n-ary set operations
 anasrini    03/24/11 - extend LogOptSource
 sbishnoi    07/29/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main_11.1.1.4.0
                        from st_pcbpel_11.1.1.4.0
 sbishnoi    07/28/10 - XbranchMerge sbishnoi_bug-9947670_ps3_main from main
 sbishnoi    07/28/10 - passing parameter for error BAD_JOIN_WITH_EXTERNAL_RELN
 sborah      12/28/09 - support for multiple external joins
 sbishnoi    09/25/09 - support for table function
 sborah      12/16/08 - handle constants
 parujain    11/19/08 - bug fix
 hopark      10/09/08 - remove statics
 rkomurav    02/28/08 - parameterize errors
 parujain    11/09/07 - external source
 parujain    10/25/07 - db join
 mthatte     10/22/07 - adding isOnDemand
 anasrini    05/25/07 - inline view support
 rkomurav    02/22/07 - remove applywindow_n
 dlenkov     06/09/06 - Added SetOpQuery
 najain      04/06/06 - cleanup
 najain      02/08/06 - Creation
 */

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/server/src/oracle/cep/logplan/LogOptRelnSrc.java /main/16 2012/10/22 14:42:18 vikshukl Exp $
 *  @author  najain  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.logplan;

import oracle.cep.semantic.GenericSetOpQuery;
import oracle.cep.exceptions.LogicalPlanError;
import oracle.cep.metadata.MetadataException;
import oracle.cep.semantic.SFWQuery;
import oracle.cep.semantic.SemQuery;
import oracle.cep.semantic.SetOpQuery;
import oracle.cep.service.ExecContext;

/**
 * Stream Source Logical Operator
 */
public class LogOptRelnSrc extends LogOptSource implements Cloneable {

  public int getRelationId() {
    return getEntityId();
  }

  public void setRelationId(int relationId) {
    setEntityId(relationId);
  }
  
  public void setRelationName(String name)
  {
    setEntityName(name);
  }
  
  public String getRelationName()
  {
    return getEntityName();
  }

  public LogOptRelnSrc() {
    // super(LogOptKind.LO_RELN_SOURCE);
    super();
  }

  // clone has been re-implemented to perform a deep copy instead of the
  // default shallow copy
  public LogOptRelnSrc clone() throws CloneNotSupportedException {
    LogOptRelnSrc op = (LogOptRelnSrc) super.clone();
    return op;
  }

  public LogOptRelnSrc(ExecContext ec, SemQuery query, int varId, int relnId)
      throws LogicalPlanException {
    super(ec, LogOptKind.LO_RELN_SOURCE, varId, relnId, false);

    assert (query instanceof SFWQuery) || (query instanceof SetOpQuery) ||
           (query instanceof GenericSetOpQuery);

    // Is this an External relation?
    // Is this an archived dimension?
    try 
    {
      this.setExternal(ec.getTableMgr().isExternal(relnId));
      this.setArchivedDim(ec.getTableMgr().isDimension(relnId));
    }
    catch(MetadataException me) {
      throw new LogicalPlanException(LogicalPlanError.TABLE_ATTR_NOT_FOUND,
                                   new Object[]{entityName});
    }
    
    this.setIsStream(false);
    this.setNumInputs(0);
  }
  
  @Override
  public void setExternal(boolean isExternal) 
  {
    // set the pull operator flag according to the isExternal flag
    this.setPullOperator(isExternal);
    
    // now call the super class method
    super.setExternal(isExternal);
  }

  @Override
  public void validate() throws LogicalPlanException{
  if(this.isExternal()) {
    LogOpt output = this.getOutput();
    // This is possible for query like select * from external relation
    if(output == null)
    {  
      throw new LogicalPlanException(
        LogicalPlanError.BAD_JOIN_WITH_EXTERNAL_RELN, getRelationName());
    }
    if(output.getOperatorKind().compareTo(LogOptKind.LO_CROSS)!=0 
       && output.getOperatorKind().compareTo(LogOptKind.LO_STREAM_CROSS)!=0) 
    {
     throw new LogicalPlanException(
       LogicalPlanError.BAD_JOIN_WITH_EXTERNAL_RELN, getRelationName());
      }
    }
  }
  // toString method override
  public String toString() {

    StringBuilder sb = new StringBuilder();

    sb.append("<RelationSourceLogicalOperator>");

    // Dump the common fields
    sb.append(super.toString());

    sb.append("<VariableId varId=\"" + varId + "\" />");
    sb.append("<RelationId varId=\"" + entityId + "\" />");

    sb.append("</RelationSourceLogicalOperator>");

    return sb.toString();
  }

 
}
