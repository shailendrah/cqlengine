/* $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhySynopsis.java /main/14 2009/06/02 12:21:26 parujain Exp $ */

/* Copyright (c) 2006, 2009, Oracle and/or its affiliates. 
All rights reserved. */

/*
 DESCRIPTION
 Declares Synopsis in package oracle.cep.phyplan.
 
 PRIVATE CLASSES
 <list of private classes defined - with one-line descriptions>
 
 NOTES
 <other useful comments, qualifications, etc.>
 
 MODIFIED    (MM/DD/YY)
 parujain  05/29/09 - maintain Ids in PlanManager
 parujain  05/08/09 - lifecycle mgmt
 parujain  01/08/09 - id always set to -1
 udeshmuk  12/06/08 - initialize id.
 udeshmuk  11/05/08 - renaming patternpartnwindowsynopsis.
 hopark    10/09/08 - remove statics
 hopark    10/07/08 - use execContext to remove statics
 udeshmuk  10/12/08 - change in getSynPos for the new pattern partn synopsis.
 rkomurav  04/15/08 - fix syn pos for ExT Syn
 hopark    02/07/08 - fix index logging
 parujain  11/29/07 - External synopsis
 hopark    06/14/07 - copy id to execSynopsis
 hopark    06/05/07 - add visitor
 rkomurav  05/16/07 - add pos for bindsyn
 rkomurav  09/11/06 - cleanup for xmldump
 rkomurav  08/24/06 - add getXMLPlan2
 najain    06/15/06 - query cleanup 
 anasrini  06/03/06 - remove stubId from physical layer 
 najain    04/06/06 - cleanup
 najain    03/30/06 - getters/setters for id
 anasrini  03/23/06 - set stubId 
 najain    03/20/06 - add Constructor with SynopsisKind only
 anasrini  03/14/06 - add method getStubId 
 skaluska  02/15/06 - Creation
 skaluska  02/15/06 - Creation
 */

/**
 *  @version $Header: pcbpel/cep/server/src/oracle/cep/phyplan/PhySynopsis.java /main/14 2009/06/02 12:21:26 parujain Exp $
 *  @author  skaluska
 *  @since   release specific (what release of product did this appear in)
 */

package oracle.cep.phyplan;

import java.util.ArrayList;
import java.util.List;

import oracle.cep.execution.indexes.Index;
import oracle.cep.execution.synopses.ExecSynopsis;
import oracle.cep.planmgr.IPlanVisitable;
import oracle.cep.planmgr.IPlanVisitor;
import oracle.cep.service.ExecContext;

/**
 * @author skaluska
 * 
 */
public class PhySynopsis implements IPlanVisitable
{
  /* synopsis id */
  int          id ;
  
  /* kind of synopsis */
  SynopsisKind kind;

  /* store */
  PhyStore     stwstore;

  /* Operator that owns the synopsis */
  PhyOpt       ownOp;

  /* instantiated execution synopsis */
  ExecSynopsis syn;

  List<PhyIndex>   indexes;
  
  /**
   * @param k
   *          kind of synopsis
   */
  public PhySynopsis(ExecContext ec, SynopsisKind k)
  {
    id = ec.getPlanMgr().getNextPhySynId();
    kind = k;
    indexes = null;
    ec.getPlanMgr().addPhySyn(this);
  }

  /**
   * @return Returns the id.
   */
  public int getId()
  {
    return id;
  }


  /**
   * @return Returns the synopsis kind.
   */
  public SynopsisKind getKind()
  {
    return kind;
  }

  /**
   * @param kind
   *          The synopsis kind to set.
   */
  public void setKind(SynopsisKind kind)
  {
    this.kind = kind;
  }

  /**
   * @return Returns the ownOp.
   */
  public PhyOpt getOwnOp()
  {
    return ownOp;
  }

  /**
   * @return Returns the stwstore.
   */
  public PhyStore getStwstore()
  {
    return stwstore;
  }

  /**
   * @return Returns the syn.
   */
  public ExecSynopsis getSyn()
  {
    return syn;
  }

  /**
   * @param ownOp
   *          The ownOp to set.
   */
  public void setOwnOp(PhyOpt ownOp)
  {
    this.ownOp = ownOp;
  }

  /**
   * @param stwstore
   *          The stwstore to set.
   */
  public void setStwstore(PhyStore stwstore)
  {
    this.stwstore = stwstore;
  }

  /**
   * @param syn
   *          The syn to set.
   */
  public void setSyn(ExecSynopsis syn)
  {
    this.syn = syn;
    syn.setPhyId(id);
  }

  public List<PhyIndex> getIndexes() 
  {
    if (indexes == null && syn != null)
    {
      List<Index> ixs = syn.getIndexes();
      if (ixs != null) {
        indexes = new ArrayList<PhyIndex>(ixs.size());
        for (Index i : ixs) {
          indexes.add(new PhyIndex(i));
        }
      }
    }
    return indexes;
  }
  
  public void makeStub(PhyStore store)
  {
    assert store != null;
    stwstore = store;
  }

  // toString method override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("<PhysicalSynopsis>");
    sb.append("<Id synopsisId=\"" + id + "\" />");
    sb.append("<Kind synopsisKind=\"" + kind + "\" />");

    // More things can be added if needed later
    sb.append("</PhysicalSynopsis>");
    return sb.toString();
  }
  
  //Generate and return visualiser compatible XML Plan
  public String getXMLPlan2() {
    StringBuilder xml = new StringBuilder();
    xml.append("<synopsis id = \"");
    xml.append(id);
    xml.append("\">\n");
    
    xml.append("<owner> ");
    xml.append(ownOp.getId());
    xml.append(" </owner>\n");
    
    xml.append("<source> ");
    xml.append(stwstore.getId());
    xml.append(" </source>\n");
    
    xml.append("<name> ");
    xml.append(kind.getName());
    xml.append(" </name>\n");
    
    xml.append("<pos> ");
    xml.append(getSynPos());
    xml.append(" </pos>");
    
    xml.append("</synopsis>\n");

    return xml.toString();
  }
  
  //Get the position string for the synopsis
  private String getSynPos() {
    StringBuilder pos = new StringBuilder();
    if(kind == SynopsisKind.WIN_SYN) {
      pos.append(PhySynPos.CENTER.getName());
    }
    else if(kind == SynopsisKind.PARTN_WIN_SYN || kind == SynopsisKind.LIN_SYN ||
            kind == SynopsisKind.PRIVATE_PARTN_WIN_SYN) {
      pos.append(PhySynPos.OUTPUT.getName());
    }
    else if(kind == SynopsisKind.REL_SYN) {
      pos.append(ownOp.getRelnSynPos(this));
    }
    else if(kind == SynopsisKind.BIND_SYN) {
      pos.append(PhySynPos.CENTER.getName());
    }
    else if(kind == SynopsisKind.EXT_SYN) {
      pos.append(PhySynPos.RIGHT.getName());
    }
    else
      assert(false);
    return pos.toString();
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
