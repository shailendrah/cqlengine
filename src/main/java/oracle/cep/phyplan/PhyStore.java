/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyStore.java /main/8 2009/06/02 12:21:26 parujain Exp $ */
/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */
/*
 DESCRIPTION
 Declares PhyStore in package oracle.cep.logplan.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain  05/29/09 - maintain Ids in PlanManager
 parujain  05/08/09 - lifecycle mgmt
 udeshmuk  12/06/08 - initialize id
 hopark    10/09/08 - remove statics
 hopark    02/07/08 - fix index logging
 hopark    06/14/07 - copy id to execStore
 hopark    06/05/07 - add visitor
 najain    06/15/06 - query cleanup 
 najain    06/13/06 - bug fix 
 najain    04/06/06 - cleanup
 skaluska  04/05/06 - pass planManager in constructor 
 najain    03/30/06 - getter/setter for Id
 anasrini  03/23/06 - addStub should return stubId 
 najain    03/22/06 - change stubs to an ArrayList 
 anasrini  03/21/06 - add getInstStore method 
 najain    03/20/06 - misc
 anasrini  03/12/06 - add getter method getStoreKind 
 najain    03/08/06 - beautify
 skaluska  02/15/06 - Creation
 skaluska  02/15/06 - Creation
 */
/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhyStore.java /main/8 2009/06/02 12:21:26 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.phyplan;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.execution.indexes.Index;
import oracle.cep.execution.stores.ExecStore;
import oracle.cep.logging.LogUtil;
import oracle.cep.logging.LoggerType;
import oracle.cep.planmgr.IPlanVisitable;
import oracle.cep.planmgr.IPlanVisitor;
import oracle.cep.service.ExecContext;

/**
 * @author skaluska
 * 
 */
public class PhyStore implements IPlanVisitable {
  /* store id - used to index planmanager's store array */
  int                    id;
  
  /* Type of store */
  PhyStoreKind           kind;

  /* Operator who owns the store */
  PhyOpt                 ownOp;

  /* Instantiated store */
  ExecStore              instStore;
  
  List<PhyIndex>         indexes;
  
  
  /**
   * @return Returns the id.
   */
  public int getId() {
    return id;
  }

  /**
   * Constructor
   * @param ec TODO
   * @param kind
   *          the kind of store
   */
  public PhyStore(ExecContext ec, PhyStoreKind kind) {
    super();
    this.kind = kind;
    indexes = null;
    id = ec.getPlanMgr().getNextPhyStoreId();
    ec.getPlanMgr().addPhyStore(this);
  }

  // Getter methods

  /**
   * Get the kind of the store
   * 
   * @return the kind of the store
   */
  public PhyStoreKind getStoreKind() {
    return kind;
  }

  /**
   * Get the execution layer representation of this store
   * 
   * @return the execution layer representation of this store
   */
  public ExecStore getInstStore() {
    return instStore;
  }

  /**
   * Get the physical operator that "owns" this store
   * 
   * @return the physical operator that "owns" this store
   */
  public PhyOpt getOwnOp() {
    return ownOp;
  }

  // Setter methods

  /**
   * Set the execution layer representation of this store
   * 
   * @param execStore
   *          the execution layer representation of this store
   */
  public void setInstStore(ExecStore execStore) {
    this.instStore = execStore;
    execStore.setPhyId(id);
  }

  public List<PhyIndex> getIndexes() 
  {
    if (indexes == null)
    {
      if (instStore == null)
      {
        LogUtil.fine(LoggerType.TRACE, "No instStore for "+toString());
        return null;
      }
      List<Index> ixs = instStore.getIndexes();
      if (ixs != null) {
        indexes = new ArrayList<PhyIndex>(ixs.size());
        for (Index i : ixs) {
          indexes.add(new PhyIndex(i));
        }
      }
    }
    return indexes;
  }
  
  /**
   * @param ownOp
   *          The ownOp to set.
   */
  public void setOwnOp(PhyOpt ownOp) {
    this.ownOp = ownOp;
  }
  
  // toString method override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalStore>");
    sb.append("<Id storeId=\"" + id + "\" />");
    sb.append("<Kind storeKind=\"" + kind + "\" />");

    // More things can be added if needed later
    sb.append("</PhysicalStore>");
    return sb.toString();
  }

  /**
   *
   * @param visitor
   */
  public void accept(IPlanVisitor visitor) 
  {
    visitor.visit(this);   
    List<PhyIndex> idxes = getIndexes();
    if (idxes != null && visitor.canVisit(IPlanVisitor.ObjType.SUB_INDEX)) {
      for (PhyIndex i : idxes) {
        i.accept(visitor);
      }
    }
  }

}
